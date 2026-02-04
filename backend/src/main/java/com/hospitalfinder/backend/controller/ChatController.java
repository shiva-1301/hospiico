package com.hospitalfinder.backend.controller;

import com.hospitalfinder.backend.dto.ChatRequest;
import com.hospitalfinder.backend.dto.ChatActionRequest;
import com.hospitalfinder.backend.entity.Clinic;
import com.hospitalfinder.backend.entity.ChatSession;
import com.hospitalfinder.backend.entity.Doctor;
import com.hospitalfinder.backend.entity.Appointment;
import com.hospitalfinder.backend.entity.Specialization;
import com.hospitalfinder.backend.repository.ClinicRepository;
import com.hospitalfinder.backend.repository.ChatSessionRepository;
import com.hospitalfinder.backend.repository.DoctorRepository;
import com.hospitalfinder.backend.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*") // Allow all origins - already secured by CorsConfig
public class ChatController {

    @Value("${groq.api.key:}")
    private String apiKey;

    @Autowired
    private ClinicRepository clinicRepository;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Maximum hospitals to return in symptom-based search
    private static final int MAX_HOSPITAL_RESULTS = 5;

    // Pattern to detect "hospital near X" or "hospitals in X" queries
    private static final Pattern HOSPITAL_QUERY_PATTERN = Pattern.compile(
            "(?:hospitals?|clinics?)\\s+(?:near|in|at|around)\\s+(.+)",
            Pattern.CASE_INSENSITIVE);

    // Keywords that indicate health symptoms (for conditional prompt injection)
    private static final List<String> SYMPTOM_KEYWORDS = Arrays.asList(
            "pain", "ache", "aching", "fever", "cough", "cold", "headache", "stomach",
            "breathing", "breath", "chest", "heart", "skin", "rash", "itch", "itching",
            "swelling", "swollen", "injury", "injured", "blood", "bleeding", "vomit",
            "nausea", "dizziness", "dizzy", "fatigue", "tired", "weakness", "weak",
            "infection", "sore", "throat", "ear", "eye", "vision", "hearing", "joint",
            "bone", "muscle", "back", "neck", "leg", "arm", "hand", "foot", "feet",
            "nose", "allergy", "allergic", "pregnant", "pregnancy", "period", "menstrual",
            "diabetes", "sugar", "pressure", "bp", "anxiety", "depression", "sleep",
            "insomnia", "cancer", "tumor", "lump", "burn", "cut", "wound", "fracture",
            "sprain", "symptom", "symptoms", "problem", "issue", "suffering", "hurts",
            "hurt", "hurting", "uncomfortable", "discomfort", "unwell", "sick", "ill",
            "disease", "condition", "diagnosis", "treatment", "doctor", "specialist",
            "specialised", "specialized", "hospitals", "hospital", "clinic", "clinics");

    // Valid specializations on the platform
    private static final List<String> VALID_SPECIALIZATIONS = Arrays.asList(
            "Cardiology", "Orthopedics", "Pediatrics", "Dermatology", "Neurology",
            "Gynecology", "ENT", "General Medicine", "Surgery", "Ophthalmology",
            "Pulmonology", "Oncology");

    // Symptom analysis prompt (only injected when symptoms detected)
    private static final String SYMPTOM_ANALYSIS_PROMPT = """
            IMPORTANT: The user is describing health symptoms. You must respond ONLY with valid JSON in this exact format:
            {"type":"specialization_match","symptom":"<brief symptom summary>","inferred_issue":"<simple non-diagnostic explanation>","specializations":["<spec1>","<spec2>"],"confidence":"low|medium|high","disclaimer":"This is not a medical diagnosis. Please consult a qualified doctor."}

            RULES:
            - Choose 1-3 specializations ONLY from: Cardiology, Orthopedics, Pediatrics, Dermatology, Neurology, Gynecology, ENT, General Medicine, Surgery, Ophthalmology, Pulmonology, Oncology
            - If unsure, include "General Medicine"
            - Do NOT diagnose or prescribe
            - Use simple, non-alarming language
            - Use simple, non-alarming language
            - Keep "inferred_issue" brief and general
            - Respond ONLY with the JSON, nothing else
            - DO NOT invent or mention specific hospital names in the text/JSON unless you are specifically returning them in a different format (which you are not). The frontend will handle displaying hospitals. Your job is only to identify the SPECIALIZATION.
            """;

    @jakarta.annotation.PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Warning: @Value injection failed for API Key. Attempting fallback...");
            apiKey = System.getProperty("GROQ_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                apiKey = System.getenv("GROQ_API_KEY");
            }
        }

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("CRITICAL: Groq API Key is NOT loaded!");
        } else {
            System.out.println("Groq API Key loaded successfully. Length: " + apiKey.length());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Collections.singletonMap("error",
                            "Chat service is not configured. Please set the GROQ_API_KEY."));
        }

        // Get the latest user message
        List<ChatRequest.Message> messages = request.getMessages();
        if (messages != null && !messages.isEmpty()) {
            ChatRequest.Message lastMessage = messages.get(messages.size() - 1);
            String content = lastMessage.getContent();

            if (content != null) {
                // Check if it's a hospital search query (explicit)
                Matcher matcher = HOSPITAL_QUERY_PATTERN.matcher(content.trim());
                if (matcher.find()) {
                    String placeName = matcher.group(1).trim();
                    System.out.println("Hospital search detected for place: " + placeName);

                    // Check for "near me" intent
                    if (placeName.equalsIgnoreCase("me") || placeName.equalsIgnoreCase("my location")) {
                        if (request.getLatitude() != null && request.getLongitude() != null) {
                            return handleNearbySearch(request.getLatitude(), request.getLongitude());
                        } else {
                            // Fallback if no location
                            return returnAsNormalText(
                                    "I need access to your location to find hospitals near you. Please enable location services or specify a city name (e.g., 'Hospital in Hyderabad').");
                        }
                    }

                    return handleHospitalCitySearch(placeName);
                }
            }
        }

        // Get latest message
        String latestMessage = messages != null && !messages.isEmpty() 
                ? messages.get(messages.size() - 1).getContent() 
                : "";
        String lowerMessage = latestMessage.toLowerCase();

        // Check if user is responding to symptom explanation (wants hospitals or appointment)
        boolean wantsHospitals = lowerMessage.contains("show") || lowerMessage.contains("hospitals") || 
                                lowerMessage.contains("nearby") || lowerMessage.contains("find hospital");
        boolean wantsAppointment = lowerMessage.contains("book") || lowerMessage.contains("appointment") ||
                                  lowerMessage.contains("see a doctor") || lowerMessage.contains("doctor");
        
        if (wantsHospitals || wantsAppointment) {
            // Check if there's a recent session with symptom data
            Optional<ChatSession> recentSession = chatSessionRepository
                .findFirstByOrderByCreatedAtDesc();
            
            if (recentSession.isPresent() && recentSession.get().getSymptom() != null) {
                ChatSession session = recentSession.get();
                return showHospitalsFromSession(session, request.getLatitude(), request.getLongitude());
            }
        }

        // Check if message contains symptom keywords
        boolean containsSymptoms = containsSymptomKeywords(latestMessage);

        // Proceed with AI chat
        String url = "https://api.groq.com/openai/v1/chat/completions";

        System.out.println("Received chat request with "
                + (request.getMessages() != null ? request.getMessages().size() : 0) + " messages.");
        System.out.println("Contains symptoms: " + containsSymptoms);
        System.out.println("Using API Key: "
                + (apiKey != null && apiKey.length() > 5 ? apiKey.substring(0, 5) + "..." : "NULL/EMPTY"));

        // Get language from request (default to English)
        String language = request.getLanguage();
        String languageName = getLanguageName(language != null ? language : "en");

        // 1. Prepare Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Prepare System Message with conditional symptom prompt
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");

        String systemPrompt;
        if (containsSymptoms) {
            // Use symptom analysis prompt
            systemPrompt = SYMPTOM_ANALYSIS_PROMPT;
        } else {
            // Normal healthcare assistant prompt
            systemPrompt = "You are a helpful healthcare assistant. Maintain conversational context. Provide general possible causes for symptoms. Limit responses to 4-6 lines. Do NOT diagnose. Always advise consulting a doctor. If user asks about hospitals near a place, tell them to use the format 'hospital near [city name]' for better results. CRITICAL: DO NOT invent hospital names. If asked for hospitals, say you can help check the database but do not list random real-world names.";
        }

        // Add language instruction if not English
        if (language != null && !language.equals("en") && !containsSymptoms) {
            systemPrompt += " IMPORTANT: You MUST respond in " + languageName
                    + " language. All your responses should be written in " + languageName + ".";
        }
        systemMessage.put("content", systemPrompt);

        // 3. Combine Messages
        List<Object> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        if (request.getMessages() != null) {
            allMessages.addAll(request.getMessages());
        }

        // 4. Request Body
        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.1-8b-instant");
        body.put("messages", allMessages);
        body.put("temperature", containsSymptoms ? 0.1 : 0.3); // Lower temperature for symptom analysis
        body.put("max_tokens", 350);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // 5. Call Groq API
            System.out.println("Sending request to Groq API...");
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            System.out.println("Groq Response Status: " + response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();

            // 6. Extract Content
            if (responseBody != null && responseBody.containsKey("choices")) {
                List choices = (List) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map choice = (Map) choices.get(0);
                    Map message = (Map) choice.get("message");
                    String replyContent = (String) message.get("content");

                    // If symptoms were detected, try to parse JSON response
                    if (containsSymptoms && replyContent != null) {
                        return handleSymptomResponse(replyContent, request.getLatitude(), request.getLongitude());
                    }

                    // Normal text response
                    Map<String, Object> result = new HashMap<>();
                    result.put("type", "text");
                    result.put("reply", replyContent);
                    return ResponseEntity.ok(result);
                }
            }
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("type", "text");
            emptyResult.put("reply", "No response from AI (Empty choices)");
            return ResponseEntity.ok(emptyResult);

        } catch (HttpClientErrorException e) {
            System.err.println("Groq API Error: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Collections.singletonMap("error", "Groq API Error: " + e.getResponseBodyAsString()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    /**
     * Check if the message contains symptom-related keywords
     */
    private boolean containsSymptomKeywords(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        String lowerMessage = message.toLowerCase();
        return SYMPTOM_KEYWORDS.stream().anyMatch(keyword -> lowerMessage.contains(keyword.toLowerCase()));
    }

    /**
     * Handle symptom-based AI response - parse JSON safely and fetch hospitals
     */
    private ResponseEntity<?> handleSymptomResponse(String aiResponse, Double userLat, Double userLng) {
        try {
            // Clean the response - extract JSON if wrapped in text
            String jsonContent = extractJson(aiResponse);
            if (jsonContent == null) {
                // Couldn't extract JSON, return as normal text
                return returnAsNormalText(aiResponse);
            }

            // Parse JSON safely
            Map<String, Object> parsed = objectMapper.readValue(jsonContent,
                    new TypeReference<Map<String, Object>>() {
                    });

            // Validate it has the expected type
            if (!"specialization_match".equals(parsed.get("type"))) {
                return returnAsNormalText(aiResponse);
            }

            // Extract specializations and normalize
            @SuppressWarnings("unchecked")
            List<String> specializations = (List<String>) parsed.get("specializations");
            if (specializations == null || specializations.isEmpty()) {
                specializations = Arrays.asList("General Medicine");
            }

            // Normalize specializations for DB query
            List<String> normalizedSpecs = normalizeSpecializations(specializations);
            System.out.println("Normalized specializations: " + normalizedSpecs);

            // Fetch hospitals by specializations
            List<Clinic> clinics = clinicRepository.findBySpecializationsIn(normalizedSpecs);

            // Sort by distance if user location is available
            List<Clinic> sortedClinics;
            if (userLat != null && userLng != null) {
                System.out.println("Sorting hospitals by distance from user location: " + userLat + ", " + userLng);
                sortedClinics = clinics.stream()
                        .sorted((c1, c2) -> {
                            double dist1 = calculateDistance(userLat, userLng,
                                    c1.getLatitude() != null ? c1.getLatitude() : 0,
                                    c1.getLongitude() != null ? c1.getLongitude() : 0);
                            double dist2 = calculateDistance(userLat, userLng,
                                    c2.getLatitude() != null ? c2.getLatitude() : 0,
                                    c2.getLongitude() != null ? c2.getLongitude() : 0);
                            return Double.compare(dist1, dist2);
                        })
                        .limit(MAX_HOSPITAL_RESULTS)
                        .collect(Collectors.toList());
            } else {
                // Fallback: sort by rating if no location
                sortedClinics = clinics.stream()
                        .limit(MAX_HOSPITAL_RESULTS)
                        .collect(Collectors.toList());
            }

            // Build hospital cards with distance info
            List<Map<String, Object>> hospitalList = new ArrayList<>();
            for (Clinic clinic : sortedClinics) {
                Map<String, Object> hospital = new HashMap<>();
                hospital.put("id", clinic.getId());
                hospital.put("name", clinic.getName());
                hospital.put("imageUrl", clinic.getImageUrl() != null ? clinic.getImageUrl() : "");
                hospital.put("city", clinic.getCity());
                hospital.put("rating", clinic.getRating() != null ? clinic.getRating() : 0.0);
                hospital.put("address", clinic.getAddress() != null ? clinic.getAddress() : "");
                hospital.put("latitude", clinic.getLatitude());
                hospital.put("longitude", clinic.getLongitude());

                // Add distance if location is available
                if (userLat != null && userLng != null && clinic.getLatitude() != null
                        && clinic.getLongitude() != null) {
                    double distance = calculateDistance(userLat, userLng, clinic.getLatitude(), clinic.getLongitude());
                    hospital.put("distance", Math.round(distance * 10.0) / 10.0); // Round to 1 decimal
                }

                hospitalList.add(hospital);
            }

            // Build response
            Map<String, Object> result = new HashMap<>();
            result.put("type", "specialization_match");
            result.put("symptom", parsed.get("symptom"));
            result.put("inferredIssue", parsed.get("inferred_issue"));
            result.put("specializations", specializations);
            result.put("confidence", parsed.get("confidence"));
            result.put("disclaimer", parsed.get("disclaimer") != null
                    ? parsed.get("disclaimer")
                    : "This is not a medical diagnosis. Please consult a qualified doctor.");
            // Build explanation message with causes
            String explanation = buildSymptomExplanation(parsed);
            
            result.put("reply", explanation);
            result.put("type", "symptom_explanation");
            result.put("step", "symptom_explanation");

            // Store data in session for when user chooses
            ChatSession session = new ChatSession();
            session.setSessionId(UUID.randomUUID().toString());
            session.setSymptom((String) parsed.get("symptom"));
            session.setSpecialization(normalizedSpecs.get(0)); // Use primary specialization
            session.setCurrentStep("symptom_explanation");
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            session.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            chatSessionRepository.save(session);
            
            result.put("sessionId", session.getSessionId());
            result.put("specialty", String.join(", ", normalizedSpecs));
            result.put("hospitalCount", sortedClinics.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("Failed to parse symptom JSON: " + e.getMessage());
            // Fall back to normal text response
            return returnAsNormalText(aiResponse);
        }
    }

    /**
     * Build symptom explanation with possible causes
     */
    private String buildSymptomExplanation(Map<String, Object> parsed) {
        String symptom = (String) parsed.get("symptom");
        String condition = (String) parsed.get("condition");
        
        StringBuilder explanation = new StringBuilder();
        explanation.append("Based on your symptoms (").append(symptom).append("), ");
        explanation.append("this could be related to ").append(condition).append(".\n\n");
        explanation.append("Common causes may include:\n");
        explanation.append("• Minor irritation or inflammation\n");
        explanation.append("• Stress or lifestyle factors\n");
        explanation.append("• Underlying medical conditions\n\n");
        explanation.append("💡 If you'd like, I can:\n");
        explanation.append("→ Show nearby hospitals\n");
        explanation.append("→ Help book an appointment");
        
        return explanation.toString();
    }

    /**
     * Show hospitals from saved session when user chooses to see them
     */
    private ResponseEntity<?> showHospitalsFromSession(ChatSession session, Double userLat, Double userLng) {
        try {
            String specialty = session.getSpecialization();
            
            // Fetch all hospitals - we'll filter manually
            List<Clinic> clinics = clinicRepository.findAll();
            
            // Filter by specialty if available
            if (specialty != null && !specialty.isEmpty()) {
                clinics = clinics.stream()
                    .filter(c -> c.getSpecializations() != null && 
                                c.getSpecializations().stream()
                                .anyMatch(s -> s.getSpecialization().equalsIgnoreCase(specialty)))
                    .collect(Collectors.toList());
            }

            // Sort by distance if coordinates provided
            List<Clinic> sortedClinics = new ArrayList<>(clinics);
            if (userLat != null && userLng != null) {
                sortedClinics.sort((a, b) -> {
                    double distA = calculateDistance(userLat, userLng, a.getLatitude(), a.getLongitude());
                    double distB = calculateDistance(userLat, userLng, b.getLatitude(), b.getLongitude());
                    return Double.compare(distA, distB);
                });
            }

            // Take top results
            List<Clinic> topClinics = sortedClinics.stream().limit(5).collect(Collectors.toList());
            
            // Build response
            Map<String, Object> result = new HashMap<>();
            result.put("type", "hospitals");
            result.put("step", "hospital_selection");
            result.put("specialty", specialty);
            
            List<Map<String, Object>> hospitalList = topClinics.stream().map(clinic -> {
                Map<String, Object> hospitalMap = new HashMap<>();
                hospitalMap.put("id", clinic.getId());
                hospitalMap.put("clinicId", clinic.getId());
                hospitalMap.put("name", clinic.getName());
                hospitalMap.put("city", clinic.getCity());
                hospitalMap.put("state", clinic.getState());
                hospitalMap.put("rating", clinic.getRating() != null ? clinic.getRating() : 0.0);
                hospitalMap.put("latitude", clinic.getLatitude());
                hospitalMap.put("longitude", clinic.getLongitude());
                hospitalMap.put("address", clinic.getAddress());
                hospitalMap.put("phoneNumber", clinic.getPhone());
                hospitalMap.put("image", clinic.getImageUrl());
                
                // Convert specializations to string list
                List<String> specialtyNames = clinic.getSpecializations() != null 
                    ? clinic.getSpecializations().stream()
                        .map(Specialization::getSpecialization)
                        .collect(Collectors.toList())
                    : new ArrayList<>();
                hospitalMap.put("specialties", specialtyNames);

                if (userLat != null && userLng != null) {
                    double distance = calculateDistance(userLat, userLng, clinic.getLatitude(), clinic.getLongitude());
                    hospitalMap.put("distance", Math.round(distance * 10.0) / 10.0);
                }
                return hospitalMap;
            }).collect(Collectors.toList());

            result.put("hospitals", hospitalList);
            result.put("reply", "I recommend consulting a " + specialty + " specialist. Here are " + 
                       topClinics.size() + " hospital(s) that may help:");
            result.put("sessionId", session.getSessionId());
            
            // Update session step
            session.setCurrentStep("hospital_selection");
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionRepository.save(session);

            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("Error showing hospitals from session: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("type", "text");
            error.put("reply", "I encountered an error fetching hospitals. Please try again.");
            return ResponseEntity.ok(error);
        }
    }

    /**
     * Extract JSON from possibly wrapped text
     */
    private String extractJson(String text) {
        if (text == null)
            return null;
        text = text.trim();

        // If it starts with {, assume it's JSON
        if (text.startsWith("{")) {
            int lastBrace = text.lastIndexOf("}");
            if (lastBrace > 0) {
                return text.substring(0, lastBrace + 1);
            }
        }

        // Try to find JSON in the text
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }

        return null;
    }

    /**
     * Normalize specialization names to match database values
     */
    private List<String> normalizeSpecializations(List<String> specializations) {
        return specializations.stream()
                .map(spec -> {
                    String lower = spec.toLowerCase().trim();
                    // Find matching valid specialization
                    for (String valid : VALID_SPECIALIZATIONS) {
                        if (valid.equals(lower) ||
                                valid.replace(" ", "").equals(lower.replace(" ", ""))) {
                            return valid;
                        }
                    }
                    // Special case handling
                    if (lower.contains("general") || lower.contains("medicine")) {
                        return "General Medicine";
                    }
                    if (lower.equals("ent") || lower.contains("ear") ||
                            lower.contains("nose") || lower.contains("throat")) {
                        return "ENT";
                    }
                    // Default to General Medicine if unrecognized
                    return "General Medicine";
                })
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Build friendly reply message for symptom match
     */
    private String buildSymptomReplyMessage(Map<String, Object> parsed, int hospitalCount) {
        String symptom = (String) parsed.get("symptom");
        String issue = (String) parsed.get("inferred_issue");
        @SuppressWarnings("unchecked")
        List<String> specs = (List<String>) parsed.get("specializations");

        StringBuilder sb = new StringBuilder();
        sb.append("Based on your symptoms (").append(symptom != null ? symptom : "described issue").append("), ");
        sb.append("this could be related to ").append(issue != null ? issue : "a health concern").append(". ");
        sb.append("I recommend consulting a ").append(String.join(" or ", specs)).append(" specialist. ");

        if (hospitalCount > 0) {
            sb.append("Here are ").append(hospitalCount).append(" hospital(s) that may help:");
        } else {
            sb.append("Unfortunately, no matching hospitals were found in our database.");
        }

        return sb.toString();
    }

    /**
     * Return AI response as normal text (fallback)
     */
    private ResponseEntity<?> returnAsNormalText(String content) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "text");
        result.put("reply", content);
        return ResponseEntity.ok(result);
    }

    /**
     * Handle explicit hospital city search
     */
    private ResponseEntity<?> handleHospitalCitySearch(String placeName) {
        List<Clinic> clinics = clinicRepository.findByCityIgnoreCase(placeName);

        if (clinics.isEmpty()) {
            // Fuzzy search for city names
            List<String> allCities = clinicRepository.findAllDistinctCities();
            List<String> suggestions = allCities.stream()
                    .filter(city -> calculateLevenshteinDistance(placeName.toLowerCase(), city.toLowerCase()) <= 3)
                    .sorted(Comparator.comparingInt(
                            city -> calculateLevenshteinDistance(placeName.toLowerCase(), city.toLowerCase())))
                    .limit(4)
                    .collect(Collectors.toList());

            StringBuilder reply = new StringBuilder("Sorry, couldn't find any hospitals in " + placeName + ".");
            if (!suggestions.isEmpty()) {
                reply.append(" Did you mean: ").append(String.join(", ", suggestions)).append("?");
            } else {
                reply.append(" Try searching for a different city.");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("type", "text");
            response.put("reply", reply.toString());
            return ResponseEntity.ok(response);
        }

        // Limit results
        List<Clinic> limitedClinics = clinics.stream()
                .limit(MAX_HOSPITAL_RESULTS)
                .collect(Collectors.toList());

        // Build hospital cards response
        List<Map<String, Object>> hospitalList = new ArrayList<>();
        for (Clinic clinic : limitedClinics) {
            Map<String, Object> hospital = new HashMap<>();
            hospital.put("id", clinic.getId());
            hospital.put("name", clinic.getName());
            hospital.put("imageUrl", clinic.getImageUrl() != null ? clinic.getImageUrl() : "");
            hospital.put("city", clinic.getCity());
            hospital.put("rating", clinic.getRating() != null ? clinic.getRating() : 0.0);
            hospital.put("address", clinic.getAddress() != null ? clinic.getAddress() : "");
            hospital.put("latitude", clinic.getLatitude());
            hospital.put("longitude", clinic.getLongitude());
            hospitalList.add(hospital);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("type", "hospitals");
        response.put("hospitals", hospitalList);
        response.put("reply", "Found " + limitedClinics.size() + " hospital(s) in " + placeName + ":");
        response.put("step", "hospital_selection");
        return ResponseEntity.ok(response);
    }

    // Helper method to map language codes to full language names
    private String getLanguageName(String langCode) {
        Map<String, String> languageNames = new HashMap<>();
        languageNames.put("en", "English");
        languageNames.put("hi", "Hindi");
        languageNames.put("te", "Telugu");
        languageNames.put("ta", "Tamil");
        languageNames.put("kn", "Kannada");
        languageNames.put("ml", "Malayalam");
        languageNames.put("mr", "Marathi");
        languageNames.put("gu", "Gujarati");
        languageNames.put("bn", "Bengali");
        languageNames.put("pa", "Punjabi");
        languageNames.put("or", "Odia");
        languageNames.put("as", "Assamese");
        languageNames.put("ur", "Urdu");
        languageNames.put("kok", "Konkani");
        languageNames.put("ks", "Kashmiri");
        languageNames.put("mni", "Manipuri");

        return languageNames.getOrDefault(langCode, "English");
    }

    /**
     * Calculate distance between two points using Haversine formula
     * 
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Earth radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Calculate Levenshtein distance for fuzzy string matching
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(Math.min(
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1));
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    /**
     * Handle "near me" search using user coordinates
     */
    private ResponseEntity<?> handleNearbySearch(Double lat, Double lng) {
        // Fetch nearest clinics (using existing repo method or explicit distance calc)
        // Since we don't have a geo-spatial query in simple repo, we fetch all (or by
        // wide area) and sort in memory
        // For efficiency, let's fetch all and sort. (Assuming small dataset < 1000
        // clinics)
        List<Clinic> allClinics = clinicRepository.findAll();

        List<Clinic> sortedClinics = allClinics.stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .sorted((c1, c2) -> {
                    double dist1 = calculateDistance(lat, lng, c1.getLatitude(), c1.getLongitude());
                    double dist2 = calculateDistance(lat, lng, c2.getLatitude(), c2.getLongitude());
                    return Double.compare(dist1, dist2);
                })
                .limit(MAX_HOSPITAL_RESULTS)
                .collect(Collectors.toList());

        if (sortedClinics.isEmpty()) {
            return returnAsNormalText("No hospitals found near your current location.");
        }

        // Build response
        List<Map<String, Object>> hospitalList = new ArrayList<>();
        for (Clinic clinic : sortedClinics) {
            Map<String, Object> hospital = new HashMap<>();
            hospital.put("id", clinic.getId());
            hospital.put("name", clinic.getName());
            hospital.put("imageUrl", clinic.getImageUrl() != null ? clinic.getImageUrl() : "");
            hospital.put("city", clinic.getCity());
            hospital.put("rating", clinic.getRating() != null ? clinic.getRating() : 0.0);
            hospital.put("address", clinic.getAddress() != null ? clinic.getAddress() : "");
            hospital.put("latitude", clinic.getLatitude());
            hospital.put("longitude", clinic.getLongitude());

            double dist = calculateDistance(lat, lng, clinic.getLatitude(), clinic.getLongitude());
            hospital.put("distance", Math.round(dist * 10.0) / 10.0);

            hospitalList.add(hospital);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("type", "hospitals");
        response.put("hospitals", hospitalList);
        response.put("reply", "Here are the hospitals closest to your location:");
        response.put("step", "hospital_selection");
        return ResponseEntity.ok(response);
    }

    /**
     * NEW: Handle step-by-step booking actions from chatbot
     */
    @PostMapping("/chat/action")
    public ResponseEntity<?> handleChatAction(@RequestBody ChatActionRequest request) {
        try {
            System.out.println("Chat action received: " + request.getAction() + " with value: " + request.getValue());
            
            // Get or create session
            ChatSession session = chatSessionRepository.findBySessionId(request.getSessionId())
                    .orElseGet(() -> {
                        ChatSession newSession = new ChatSession();
                        newSession.setSessionId(request.getSessionId());
                        newSession.setCreatedAt(LocalDateTime.now());
                        newSession.setExpiresAt(LocalDateTime.now().plusMinutes(30));
                        newSession.setCurrentStep("symptom_classification");
                        return newSession;
                    });

            // Handle actions based on currentStep and action type
            switch (request.getAction()) {
                case "select_hospital":
                    return handleHospitalSelection(session, request.getValue());
                    
                case "select_doctor":
                    return handleDoctorSelection(session, request.getValue());
                    
                case "select_date":
                    return handleDateSelection(session, request.getValue());
                    
                case "select_time":
                    return handleTimeSelection(session, request.getValue());
                    
                case "confirm_booking":
                    return handleBookingConfirmation(session, request);
                    
                default:
                    return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Unknown action: " + request.getAction())
                    );
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Collections.singletonMap("error", "Failed to process action: " + e.getMessage())
            );
        }
    }

    private ResponseEntity<?> handleHospitalSelection(ChatSession session, String clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId).orElse(null);
        if (clinic == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Hospital not found"));
        }

        session.setClinicId(clinicId);
        session.setClinicName(clinic.getName());
        session.setCurrentStep("doctor_selection");
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        // Fetch doctors for this hospital with the required specialization
        List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);
        
        // Filter by specialization if available
        if (session.getSpecialization() != null && !session.getSpecialization().isEmpty()) {
            doctors = doctors.stream()
                    .filter(d -> d.getSpecialization() != null && 
                            d.getSpecialization().equalsIgnoreCase(session.getSpecialization()))
                    .collect(Collectors.toList());
        }

        // Build doctor list
        List<Map<String, Object>> doctorList = new ArrayList<>();
        for (Doctor doctor : doctors) {
            Map<String, Object> doctorInfo = new HashMap<>();
            doctorInfo.put("id", doctor.getId());
            doctorInfo.put("name", doctor.getName());
            doctorInfo.put("specialization", doctor.getSpecialization());
            doctorInfo.put("qualifications", doctor.getQualifications());
            doctorInfo.put("experience", doctor.getExperience());
            doctorInfo.put("imageUrl", doctor.getImageUrl() != null ? doctor.getImageUrl() : "");
            doctorList.add(doctorInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("step", "doctor_selection");
        response.put("message", "Great! Please select a doctor from " + clinic.getName());
        response.put("doctors", doctorList);
        response.put("sessionId", session.getSessionId());
        
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> handleDoctorSelection(ChatSession session, String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Doctor not found"));
        }

        session.setDoctorId(doctorId);
        session.setDoctorName(doctor.getName());
        session.setCurrentStep("date_selection");
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("step", "date_selection");
        response.put("message", "When would you like to book an appointment with Dr. " + doctor.getName() + "?");
        response.put("sessionId", session.getSessionId());
        
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> handleDateSelection(ChatSession session, String date) {
        try {
            // Validate date format (YYYY-MM-DD)
            LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Check if date is in the past
            if (selectedDate.isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Cannot book appointments in the past")
                );
            }

            session.setSelectedDate(date);
            session.setCurrentStep("time_selection");
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionRepository.save(session);

            // Fetch booked appointments for this doctor on this date
            LocalDateTime startOfDay = selectedDate.atStartOfDay();
            LocalDateTime endOfDay = selectedDate.atTime(23, 59, 59);
            List<Appointment> bookedAppointments = appointmentRepository.findByDoctorAndDate(
                session.getDoctorId(), startOfDay, endOfDay
            );

            Set<String> bookedTimes = bookedAppointments.stream()
                    .filter(apt -> "BOOKED".equalsIgnoreCase(apt.getStatus()))
                    .map(apt -> apt.getAppointmentTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .collect(Collectors.toSet());

            // Generate available time slots
            List<String> availableSlots = generateTimeSlots(selectedDate, bookedTimes);

            Map<String, Object> response = new HashMap<>();
            response.put("step", "time_selection");
            response.put("message", "Please select a time slot:");
            response.put("availableSlots", availableSlots);
            response.put("sessionId", session.getSessionId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "Invalid date format. Please use YYYY-MM-DD")
            );
        }
    }

    private ResponseEntity<?> handleTimeSelection(ChatSession session, String time) {
        session.setSelectedTime(time);
        session.setCurrentStep("patient_details");
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("step", "patient_details");
        response.put("message", "Please provide patient details to confirm the booking:");
        response.put("sessionId", session.getSessionId());
        response.put("appointmentDetails", Map.of(
            "hospital", session.getClinicName(),
            "doctor", session.getDoctorName(),
            "date", session.getSelectedDate(),
            "time", session.getSelectedTime()
        ));
        
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> handleBookingConfirmation(ChatSession session, ChatActionRequest request) {
        try {
            // Validate all required fields
            if (session.getClinicId() == null || session.getDoctorId() == null || 
                session.getSelectedDate() == null || session.getSelectedTime() == null) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Incomplete booking information")
                );
            }

            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setUserId(request.getValue()); // userId if logged in
            appointment.setClinicId(session.getClinicId());
            appointment.setDoctorId(session.getDoctorId());
            
            LocalDateTime appointmentTime = LocalDateTime.of(
                LocalDate.parse(session.getSelectedDate()),
                java.time.LocalTime.parse(session.getSelectedTime())
            );
            appointment.setAppointmentTime(appointmentTime);
            appointment.setStatus("BOOKED");
            
            // Set patient details
            appointment.setPatientName(request.getPatientName());
            appointment.setPatientAge(request.getPatientAge());
            appointment.setPatientGender(request.getPatientGender());
            appointment.setPatientPhone(request.getPatientPhone());
            appointment.setPatientEmail(request.getPatientEmail());
            appointment.setReason(request.getReason());

            // Check for double booking
            if (appointmentRepository.existsByDoctorIdAndAppointmentTime(
                    session.getDoctorId(), appointmentTime)) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "This time slot has just been booked. Please select another time.")
                );
            }

            appointment = appointmentRepository.save(appointment);

            // Update session
            session.setCurrentStep("booking_confirmed");
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionRepository.save(session);

            Map<String, Object> response = new HashMap<>();
            response.put("step", "booking_confirmed");
            response.put("message", "✅ Appointment booked successfully!");
            response.put("appointmentId", appointment.getId());
            response.put("details", Map.of(
                "hospital", session.getClinicName(),
                "doctor", session.getDoctorName(),
                "date", session.getSelectedDate(),
                "time", session.getSelectedTime(),
                "patient", request.getPatientName()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Collections.singletonMap("error", "Failed to create appointment: " + e.getMessage())
            );
        }
    }

    private List<String> generateTimeSlots(LocalDate selectedDate, Set<String> bookedTimes) {
        List<String> slots = new ArrayList<>();
        LocalDate today = LocalDate.now();
        boolean isToday = selectedDate.equals(today);
        int currentHour = isToday ? LocalDateTime.now().getHour() : 0;
        int currentMinute = isToday ? LocalDateTime.now().getMinute() : 0;
        
        boolean isSunday = selectedDate.getDayOfWeek().getValue() == 7;
        
        // Morning slots: 9:00 AM - 1:00 PM
        for (int hour = 9; hour <= 13; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                if (hour == 13 && minute > 0) break;
                
                if (isToday && (hour < currentHour || (hour == currentHour && minute <= currentMinute))) {
                    continue;
                }
                
                String timeSlot = String.format("%02d:%02d", hour, minute);
                if (!bookedTimes.contains(timeSlot)) {
                    slots.add(timeSlot);
                }
            }
        }
        
        // Afternoon slots: 2:00 PM - 8:00 PM (6:00 PM on Sunday)
        int afternoonEnd = isSunday ? 18 : 20;
        for (int hour = 14; hour <= afternoonEnd; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                if (hour == afternoonEnd && minute > 0) break;
                
                if (isToday && (hour < currentHour || (hour == currentHour && minute <= currentMinute))) {
                    continue;
                }
                
                String timeSlot = String.format("%02d:%02d", hour, minute);
                if (!bookedTimes.contains(timeSlot)) {
                    slots.add(timeSlot);
                }
            }
        }
        
        return slots;
    }
}
