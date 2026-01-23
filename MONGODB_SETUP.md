# MongoDB Conversion - Setup Guide

## ✅ What Has Been Changed

Your Hospico project has been successfully converted from **PostgreSQL** to **MongoDB**. Here's what was updated:

### 1. **Backend Dependencies (pom.xml)**
- ❌ Removed: `spring-boot-starter-data-jpa`
- ❌ Removed: `org.postgresql:postgresql`
- ✅ Added: `spring-boot-starter-data-mongodb`

### 2. **Entity Classes (All MongoDB Documents)**
All JPA `@Entity` classes have been converted to MongoDB `@Document` annotations:
- `User.java` - User collection
- `Clinic.java` - Clinic collection
- `Doctor.java` - Doctor collection
- `Appointment.java` - Appointment collection
- `Review.java` - Review collection
- `Specialization.java` - Specialization collection
- `MedicalRecord.java` - MedicalRecord collection

**ID Change:** All Long IDs changed to String (MongoDB uses ObjectId as strings)

### 3. **Repository Classes**
All repositories now extend `MongoRepository<Entity, String>` instead of `JpaRepository<Entity, Long>`
- `UserRepository.java`
- `ClinicRepository.java`
- `DoctorRepository.java`
- `AppointmentRepository.java`
- `ReviewRepository.java`
- `SpecializationRepository.java`
- `MedicalRecordRepository.java`

### 4. **Configuration Files**
- **application.yml**: Updated to use MongoDB URI instead of PostgreSQL datasource
- **docker-compose.yml**: Replaced PostgreSQL with MongoDB service

### 5. **MongoDB Configuration**
- Created: `MongoDbConfig.java`
  - Auto-creates indexes on startup
  - Seeds admin user with default credentials
  - Handles collection initialization

---

## 🚀 Getting Started

### **Option 1: Using Docker Compose (Recommended)**

```bash
# Navigate to project root
cd Hospico

# Start all services (MongoDB + Backend + Frontend)
docker-compose up --build

# Services will be available at:
# - Frontend: http://localhost:3000
# - Backend: http://localhost:8080
# - MongoDB: localhost:27017
```

### **Option 2: Local MongoDB Setup**

#### Step 1: Install MongoDB
- **Windows**: Download from https://www.mongodb.com/try/download/community
- **Mac**: `brew install mongodb-community`
- **Linux**: `sudo apt-get install mongodb`

#### Step 2: Start MongoDB Service
```bash
# Windows
mongod

# Mac/Linux
brew services start mongodb-community
# or
sudo systemctl start mongod
```

#### Step 3: Configure Application
Create or update `src/main/resources/application-local.yml`:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/hospital_booking
      auto-index-creation: true
```

#### Step 4: Run Backend
```bash
# Option 1: Using Maven
cd backend
mvn spring-boot:run

# Option 2: Using IDE (IntelliJ/Eclipse)
# Right-click HospitalBookingBackendApplication.java → Run
```

#### Step 5: Run Frontend
```bash
cd hospico-frontend-main
npm install
npm run dev
```

---

## 📊 Default Admin User

When the backend starts, it automatically creates an admin user:

| Field | Value |
|-------|-------|
| **Email** | `admin@hospico.com` |
| **Password** | `admin@123` |
| **Role** | ADMIN |
| **Phone** | +1234567890 |

**⚠️ Important**: Change this password in production!

---

## 🔧 Troubleshooting

### Issue: "Cannot connect to MongoDB"
```bash
# Check if MongoDB is running
mongosh  # Should connect to local MongoDB

# If not running:
# Windows: mongod
# Mac: brew services start mongodb-community
# Linux: sudo systemctl start mongod
```

### Issue: "Indexes not created"
MongoDB's auto-index-creation should handle this. If issues persist:
```bash
# Connect to MongoDB shell
mongosh
use hospital_booking

# Check existing indexes
db.users.getIndexes()

# Restart backend to trigger MongoDbConfig
```

### Issue: "Admin user not created"
The MongoDbConfig.java automatically creates admin user on first startup.
If it doesn't appear:
1. Check backend logs for error messages
2. Manually verify collections exist: `show collections` in mongosh
3. Restart backend

### Issue: Port Already in Use
If port 8080 is busy:
```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process (Windows)
taskkill /PID <PID> /F

# Or change port in application.yml
server:
  port: 8081
```

---

## 📝 Database Schema

MongoDB collections are created automatically with the following structure:

### **users** collection
```json
{
  "_id": ObjectId("..."),
  "name": "Admin",
  "email": "admin@hospico.com",
  "phone": "+1234567890",
  "password": "$2a$10$...",
  "age": 30,
  "gender": "Male",
  "role": "ADMIN"
}
```

### **clinics** collection
```json
{
  "_id": ObjectId("..."),
  "name": "City Hospital",
  "address": "123 Main St",
  "city": "Mumbai",
  "latitude": 19.0760,
  "longitude": 72.8777,
  "phone": "+919876543210",
  "website": "https://cityhospital.com",
  "timings": "9:00 AM - 6:00 PM",
  "rating": 4.5,
  "reviews": 128,
  "imageUrl": "https://...",
  "specializations": [...]
}
```

### **doctors** collection
```json
{
  "_id": ObjectId("..."),
  "name": "Dr. John Doe",
  "qualifications": "MBBS, MD",
  "specialization": "Cardiology",
  "experience": "15 years",
  "biography": "...",
  "clinicId": "...",
  "imageUrl": "https://..."
}
```

### **appointments** collection
```json
{
  "_id": ObjectId("..."),
  "userId": "...",
  "clinicId": "...",
  "doctorId": "...",
  "appointmentTime": "2026-02-01T10:30:00",
  "status": "BOOKED",
  "patientName": "John Smith",
  "patientAge": 35,
  "patientGender": "Male",
  "patientPhone": "+919876543210",
  "patientEmail": "john@example.com",
  "reason": "Regular checkup"
}
```

---

## 🔍 Verifying Your Setup

### Check MongoDB Connection
```bash
mongosh
use hospital_booking
show collections
```

Expected output:
```
appointments
clinics
doctors
medical_records
reviews
specializations
users
```

### Check Admin User
```bash
use hospital_booking
db.users.find({ email: "admin@hospico.com" })
```

### Check Backend Startup Logs
Look for these messages in console:
```
✓ MongoDB indexes created successfully!
═══════════════════════════════════════════════════════════
✓ ADMIN USER CREATED SUCCESSFULLY!
═══════════════════════════════════════════════════════════
EMAIL:    admin@hospico.com
PASSWORD: admin@123
═══════════════════════════════════════════════════════════
```

---

## 📚 API Testing

### Login as Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@hospico.com",
    "password": "admin@123"
  }'
```

Expected response:
```json
{
  "message": "Login successful",
  "data": {
    "id": "...",
    "name": "Admin",
    "email": "admin@hospico.com",
    "role": "ADMIN"
  }
}
```

---

## 🌍 Environment Variables (for Docker/Production)

When deploying, set these variables:

```env
# MongoDB Connection
MONGO_DB_URI=mongodb://username:password@mongodb-host:27017/hospital_booking?authSource=admin

# Server Port
PORT=8080

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

---

## 🔐 Important Notes

1. **ID Type Changed**: All IDs are now Strings (MongoDB ObjectId) instead of Long
2. **No SQL Queries**: Any custom SQL queries in services need to be rewritten for MongoDB
3. **Foreign Keys**: MongoDB uses document embedding or references via IDs
4. **Transactions**: MongoDB transactions are available but behave differently than SQL
5. **Indexes**: Auto-created on startup, but can be optimized based on usage patterns

---

## ✨ Next Steps

1. ✅ Run `docker-compose up` to start the application
2. ✅ Verify MongoDB is running
3. ✅ Check backend logs for admin user creation
4. ✅ Login with `admin@hospico.com` / `admin@123`
5. ✅ Start using the application!

---

## 💬 Support

If you encounter any issues:
1. Check the troubleshooting section above
2. Verify MongoDB is running
3. Check backend logs for detailed error messages
4. Ensure ports 27017 (MongoDB), 8080 (Backend), 3000 (Frontend) are available

Happy coding! 🚀
