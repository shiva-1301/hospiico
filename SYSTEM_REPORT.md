# 🏥 LIFELINK - COMPLETE SYSTEM REPORT
*Current Working Features & User Capabilities*

---

## 📊 DATABASE STRUCTURE (MongoDB Atlas)

### Collections:

#### 1. `users`
```
- id (String, Primary Key)
- name (String)
- email (String, Unique)
- phone (String)
- password (String, BCrypt hashed)
- age (Integer)
- gender (String)
- role (Enum: USER, HOSPITAL, ADMIN)
```

#### 2. `clinics` (Hospitals)
```
- id (String, Primary Key)
- name (String)
- address (String)
- city (String)
- latitude (Double)
- longitude (Double)
- specializations (Array of Specialization objects)
- phone (String)
- website (String)
- timings (String)
- rating (Double)
- reviews (Integer count)
- doctors (Array of embedded Doctor objects)
- imageUrl (String)
```

#### 3. `doctors`
```
- id (String, Primary Key)
- name (String)
- qualifications (String)
- specialization (String)
- experience (String)
- biography (String)
- clinicId (String, Foreign Key to clinics)
- imageUrl (String)
```

#### 4. `appointments`
```
- id (String, Primary Key)
- userId (String, Foreign Key to users)
- clinicId (String, Foreign Key to clinics)
- doctorId (String, Foreign Key to doctors)
- appointmentTime (LocalDateTime)
- status (String: BOOKED, CANCELLED)
- patientName (String)
- patientAge (Integer)
- patientGender (String)
- patientPhone (String)
- patientEmail (String)
- reason (String)
```

#### 5. `reviews`
```
- id (String, Primary Key)
- rating (Integer, 1-5)
- comment (String)
- createdAt (LocalDateTime)
- userId (String, Foreign Key to users)
- hospitalId (String, Foreign Key to clinics)
- doctorId (String, Foreign Key to doctors)
```

#### 6. `medical_records`
```
- id (String, Primary Key)
- name (String, filename)
- type (String, file MIME type)
- size (Long, bytes)
- category (String: Diagnostics, Scanning, Prescriptions, Bills)
- data (byte[], file content)
- uploadDate (LocalDateTime)
- userId (String, Foreign Key to users)
```

#### 7. `specializations`
```
- id (String, Primary Key)
- specialization (String, e.g., "Cardiology")
```

---

## 🔐 AUTHENTICATION & SECURITY

### Public Endpoints (No Login Required):
```
✅ GET  /                          - Health check
✅ GET  /api/health                - System health
✅ GET  /actuator/health           - Spring actuator
✅ POST /api/auth/signup           - User registration
✅ POST /api/auth/login            - User login
✅ GET  /api/clinics/**            - Browse hospitals
✅ GET  /api/specializations/**    - View specializations
✅ POST /api/chat                  - AI medical chatbot
✅ OPTIONS /**                     - CORS preflight
```

### Protected Endpoints (Login Required):
```
🔒 ALL /api/users/**               - User management
🔒 ALL /api/appointments/**        - Appointment management
🔒 ALL /api/medical-records/**     - Medical records
🔒 ALL /api/reviews/**             - Review submission
```

### Authentication Method:
- **JWT tokens** (JSON Web Tokens)
- Token stored in: localStorage OR cookies
- Header format: `Authorization: Bearer <token>`
- Token contains: userId, email, role

---

## 👥 USER ROLES & ACTUAL WORKING CAPABILITIES

### 🌐 PUBLIC USERS (Not Logged In)

#### ✅ CAN DO:

**1. Browse Hospitals**
- View all hospitals with pagination
- Search by name, address
- Filter by city
- Filter by specialization(s)
- Sort by distance from location
- View "Nearby" hospitals (within 5km radius)

**2. Hospital Details**
- View full hospital profile
- See ratings and review count
- View contact info (phone, website)
- See timings, address, location map
- View available specializations
- See list of doctors at hospital

**3. Location-Based Search**
- Get hospitals near GPS coordinates
- Calculate distance in km
- Estimate travel time (based on distance)
- Advanced: Variable speed calculation (20-40 km/h based on distance)

**4. AI Medical Chatbot**
- Ask health questions
- Get symptom analysis
- Receive hospital recommendations based on symptoms
- Search "hospitals near [city]" via chat
- Powered by Groq AI

**5. View Specializations**
- See all 12 medical specializations:
  - Cardiology, Neurology, Orthopedics, Pediatrics
  - Gynecology, Dermatology, ENT, Ophthalmology
  - General Surgery, Emergency Medicine, Oncology, Radiology

#### ❌ CANNOT DO:
- Book appointments
- Upload medical records
- Submit reviews
- View/manage profile

---

### 👤 REGULAR USER (Patients)
*After signing up & logging in*

#### ✅ CAN DO (Everything Public + Below):

**1. Account Management**
- Update profile: name, phone, age, gender
- Change password
- View own profile (`GET /api/users/me`)
- Delete account

**2. Appointment Booking**
- Book appointment with specific doctor at hospital
- Choose date & time slot
- Provide patient details (can book for someone else):
  - Patient name, age, gender
  - Contact: phone, email
  - Reason for visit
- View all their appointments (`GET /api/appointments/user/{userId}`)
- Cancel appointments (`PUT /api/appointments/{id}/cancel`)
- Reschedule appointments (`PUT /api/appointments/{id}/reschedule`)
- Delete appointments (`DELETE /api/appointments/{id}`)

**3. Medical Records Management**
- Upload files (PDF, images)
- Categorize: Diagnostics, Scanning, Prescriptions, Bills
- View all their medical records
- Download individual files
- Delete their records

**4. Reviews & Ratings**
- Submit reviews for hospitals
- Submit reviews for doctors
- Rate on 1-5 scale
- Write detailed comments
- View their own reviews

**5. Multi-Patient Booking**
- Book appointments for family members
- Store different patient details per appointment

#### ❌ CANNOT DO:
- Modify hospital information
- Add/edit doctors
- Access other users' data
- View other users' appointments
- Delete other users' reviews
- Access admin functions

#### ⚠️ CURRENT LIMITATION:
- No ownership validation - technically can modify ANY user's data if they know the ID
- No role-based access control on endpoints
- Any authenticated user can access protected endpoints

---

### 🏥 HOSPITAL USER
*Role exists but NOT fully implemented*

#### Current Status: ❌ **NON-FUNCTIONAL**

**Why:**
- No `ownerId` field linking User to Clinic
- No ownership verification in ClinicController
- Any logged-in user can create/modify hospitals
- HOSPITAL role is just a label with no enforcement

**What SHOULD Work (if implemented):**
- Hospital partner signs up with role=HOSPITAL
- Gets linked to ONE specific clinic
- Can only edit THEIR hospital's info
- Can add/manage doctors at their facility
- View appointments at their hospital

**Current Reality:**
- Role exists in database
- No functional difference from USER role
- Not protecting any endpoints

---

### 👑 ADMIN
*Fully privileged account*

#### ✅ CAN DO (Everything + Below):

**1. User Management**
- View ALL users (`GET /api/users`)
- View any user details
- Update any user account
- Delete any user
- Change user roles

**2. Hospital Management**
- Create new hospitals
- Edit any hospital details
- Delete hospitals
- Add specializations to hospitals

**3. Doctor Management**
- Add doctors to any hospital
- Edit doctor profiles
- Remove doctors

**4. Appointment Oversight**
- View appointments by clinic
- View appointments by doctor
- View appointments by date
- Access all booking data

**5. Content Moderation**
- View all reviews
- Delete inappropriate reviews
- Monitor user activity

**6. System Administration**
- Seed initial data (via MongoDbConfig)
- Manage specializations list
- Database health monitoring
- Keep-alive scheduler management

**Current Admin Account:**
- Email: `shiva@gmail.com`
- Password: `shiva`

#### ⚠️ WARNING:
Admin privileges not enforced with `@PreAuthorize` - relies on manual checks only.

---

## 🚀 WORKING FEATURES

### 1. Hospital Search & Discovery
```
✅ Browse all hospitals
✅ Nearby search (5km radius)
✅ Distance calculation (Haversine formula)
✅ Travel time estimation (variable speed: 20-40 km/h)
✅ Filter by city
✅ Filter by specialization(s) with match scoring
✅ Text search (name, address)
✅ Sort by distance, rating, or specialization match
✅ Geolocation-based queries
```

### 2. Appointment System
```
✅ Book appointment (user + clinic + doctor + time)
✅ Prevent past bookings
✅ Prevent double-booking (same doctor + time)
✅ Store patient details for each appointment
✅ View user's appointments
✅ View clinic's appointments
✅ View doctor's schedule
✅ Get available slots for specific date
✅ Cancel appointments
✅ Reschedule appointments
✅ Delete appointments
```

### 3. Medical Records Storage
```
✅ Upload files (stored as byte[] in MongoDB)
✅ Categorize by type
✅ View user's records list
✅ Download individual files
✅ Delete records
✅ File metadata tracking (name, size, type, date)
```

### 4. Review & Rating System
```
✅ Submit reviews for hospitals
✅ Submit reviews for doctors
✅ Rate 1-5 stars
✅ Add text comments
✅ Timestamp reviews
✅ Link reviews to user, hospital, doctor
✅ View reviews by hospital
✅ View reviews by user
```

### 5. AI Medical Chatbot
```
✅ Powered by Groq AI (LLM)
✅ Symptom analysis
✅ Medical advice (educational only)
✅ Hospital recommendations based on symptoms
✅ Location-based hospital search via chat
✅ Specialization matching for symptoms
✅ Natural language processing
✅ Context-aware responses
```

### 6. User Authentication
```
✅ Email/password signup
✅ BCrypt password hashing
✅ JWT token generation
✅ Token validation
✅ Role-based user types (USER, HOSPITAL, ADMIN)
✅ Persistent login via token
✅ Logout (client-side token removal)
```

### 7. Geolocation Features
```
✅ GPS coordinate-based search
✅ Distance calculation (kilometers)
✅ Nearby hospitals (configurable radius)
✅ Sort by proximity
✅ Multi-factor sorting (distance + specialization match)
✅ Travel time estimation
```

### 8. Data Seeding
```
✅ Auto-seed 12 specializations on startup
✅ Seed 3 sample clinics in Hyderabad
✅ Create admin user if not exists
✅ MongoDB indexes creation
✅ Only runs on first startup (checks if data exists)
```

---

## ⚠️ KNOWN LIMITATIONS & MISSING FEATURES

### ❌ NOT IMPLEMENTED:

**1. Role-Based Access Control (RBAC)**
- No `@PreAuthorize` annotations
- HOSPITAL role has no special permissions
- Admin privileges not enforced at endpoint level
- Any user can technically access any endpoint if authenticated

**2. Hospital Ownership**
- No `ownerId` field in Clinic entity
- No link between HOSPITAL user and their clinic
- Cannot restrict "only edit your own hospital"

**3. Data Ownership Validation**
- Users can modify other users' data (if they know IDs)
- No check for "this user owns this appointment"
- No validation on medical record access

**4. Payment Integration**
- No payment gateway
- No booking fees
- No transaction records

**5. Email Notifications**
- No appointment confirmation emails
- No reminders
- No cancellation notifications

**6. Real-time Features**
- No WebSocket for live updates
- No instant notifications
- No chat between patient & hospital

**7. Advanced Booking**
- No recurring appointments
- No waiting list
- No appointment priority/urgency

**8. Hospital Dashboard**
- No analytics for hospitals
- No appointment management UI for hospitals
- No patient history view

**9. Doctor Availability Management**
- No doctor schedule management
- No "doctor is off on X date"
- No automatic slot blocking

---

## 🎯 WHAT ACTUALLY WORKS RIGHT NOW

### For Patients (Regular Users):
1. ✅ Sign up, log in, manage profile
2. ✅ Search hospitals by location, city, specialization
3. ✅ View hospital details, ratings, doctors
4. ✅ Book appointments with specific doctors
5. ✅ View & manage their appointments
6. ✅ Upload & store medical records
7. ✅ Submit reviews and ratings
8. ✅ Chat with AI for medical advice
9. ✅ Book appointments for family members

### For Admins:
1. ✅ All patient features
2. ✅ View all users
3. ✅ Create/edit/delete hospitals
4. ✅ Add doctors to hospitals
5. ✅ View all appointments system-wide
6. ✅ Manage specializations
7. ✅ Seed initial data

### For Hospitals:
❌ Currently same as regular users (no special features)

---

## 📈 CURRENT DATA (Seeded)

**Specializations (12):**
- Cardiology, Neurology, Orthopedics, Pediatrics
- Gynecology, Dermatology, ENT, Ophthalmology
- General Surgery, Emergency Medicine, Oncology, Radiology

**Sample Hospitals (3 in Hyderabad):**
1. Apollo Hospital (Multi-specialty)
2. CARE Hospital (Cardiology, Neurology, Emergency)
3. Continental Hospital (Orthopedics, General Surgery)

**Admin User:**
- Email: shiva@gmail.com
- Password: shiva
- Role: ADMIN

**Test User:**
- Email: shiva123@gmail.com
- Password: shiva
- Role: USER

---

## 🔧 TECHNICAL STACK

**Backend:**
- Java 17
- Spring Boot 3.4.5
- Spring Data MongoDB
- JWT Authentication
- BCrypt password encryption
- Maven build

**Frontend:**
- React 19.1
- TypeScript 5.9
- Vite 7.1
- Redux Toolkit (state management)
- Tailwind CSS + Material-UI
- Geolocation API
- React Router

**Database:**
- MongoDB Atlas (Cloud)
- Database: `hospicoDB`
- Connection: `mongodb+srv://...@lifelink.iy3cmpk.mongodb.net`

**AI Integration:**
- Groq AI API
- LLM-powered medical chatbot

**Deployment:**
- Local development: localhost:8080 (backend), localhost:5173 (frontend)
- Production-ready but not deployed

---

## 📊 CAPABILITIES COMPARISON TABLE

| Feature | PUBLIC | USER | HOSPITAL | ADMIN |
|---------|--------|------|----------|-------|
| Browse hospitals | ✅ View | ✅ View | ✅ View | ✅ Full |
| Book appointments | ❌ | ✅ Create | ✅ Create | ✅ Full |
| Medical records | ❌ | ✅ Own only | ❌ | ✅ All |
| Submit reviews | ❌ | ✅ Create | ✅ Create | ✅ Full |
| Manage hospitals | ❌ | ❌ | ❌* | ✅ All |
| Manage doctors | ❌ | ❌ | ❌* | ✅ All |
| User accounts | ❌ | ✅ Own profile | ✅ Own profile | ✅ All users |
| Specializations | ❌ View | ❌ View | ❌ View | ✅ Full |
| System settings | ❌ | ❌ | ❌ | ✅ Full |
| AI Chatbot | ✅ Use | ✅ Use | ✅ Use | ✅ Use |

*HOSPITAL role exists but has no special permissions implemented

---

## 📝 SUMMARY

**Working Core Features:**
- Hospital search & filtering ✅
- Appointment booking ✅
- Medical records storage ✅
- Review system ✅
- AI chatbot ✅
- User authentication ✅

**Major Gaps:**
- Hospital user role not functional ❌
- No role-based access control ❌
- No ownership validation ❌
- No payment system ❌
- No notifications ❌

**User Types Actually Working:**
1. **PUBLIC** - Can browse & search hospitals
2. **USER** - Can book appointments, upload records, review
3. **ADMIN** - Full control over system
4. **HOSPITAL** - Same as USER (non-functional as hospital partner)

---

*Report generated for LifeLink Healthcare Platform*  
*Database: MongoDB Atlas*  
*Last Updated: January 23, 2026*
