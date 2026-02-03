You are building a conversational medical appointment booking chatbot that integrates with an existing hospital booking backend.

The chatbot must guide users step-by-step to book an appointment using natural conversation.

The backend is already implemented using Spring Boot + MongoDB and exposes REST APIs for hospitals, doctors, and appointment booking.

Your job is to implement a chatbot flow that:

1. Understands the user's medical issue
2. Maps the issue to an available medical specialization
3. Suggests hospitals that support that specialization
4. Shows doctors in the selected hospital
5. Lets the user pick a date
6. Shows available time slots
7. Books the appointment
8. Confirms the booking

The chatbot must NEVER invent data. It must always fetch real data from backend APIs.

---

Conversation Flow Requirements:

STEP 1 — Symptom understanding
User describes their problem in natural language.
Example: "I have ear pain"

The chatbot must classify the issue into a specialization available in the database.

Example mapping:
ear pain → ENT
chest pain → Cardiology
skin rash → Dermatology
fever → General Physician

Return JSON:
{
  "intent": "classify_symptom",
  "specialization": "ENT"
}

---

STEP 2 — Hospital suggestions
Call backend:
GET /api/hospitals?specialization=ENT

Sort hospitals by distance ascending.
Return cards showing hospital name + distance.

User selects one.

Store session state:
specialization, clinicId

---

STEP 3 — Doctor selection
Call backend:
GET /api/doctors?clinicId={clinicId}&specialization={specialization}

Return doctor cards.

User selects doctor.

Store doctorId in session.

---

STEP 4 — Date selection
User provides date (natural language allowed: tomorrow, Friday, etc).
Convert to ISO date.

---

STEP 5 — Time slot lookup
Call backend:
GET /api/appointments/doctor/{doctorId}/date/{date}

Return only available time slots.

User selects slot.

---

STEP 6 — Booking
Call backend:
POST /api/appointments

Payload includes:
userId
clinicId
doctorId
appointmentTime
patient details

Return confirmation message.

---

System Rules:

- Chatbot must behave like a state machine
- Track conversation step in session memory
- Never skip steps
- Never hallucinate hospitals/doctors
- Only use backend API data
- Validate user input at each step
- Handle errors gracefully
- If no slots available → suggest next day
- If specialization not found → ask clarifying question

---

Output Requirements:

The chatbot must return structured JSON responses:

{
  "step": "choose_hospital",
  "message": "Here are ENT hospitals near you",
  "options": [ ... ]
}

or

{
  "step": "booking_confirmed",
  "message": "Appointment booked successfully with Dr Sharma at 10:30 AM"
}

---

Goal:

Build a reliable conversational assistant that automates the full appointment booking workflow using real backend APIs.

Do not simulate data.
Do not invent doctors.
Do not assume availability.

Always call APIs to retrieve real information.
