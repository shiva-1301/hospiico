# 🚀 Hospico - Quick Start Guide

## System is RUNNING ✅

Your complete Hospico application stack is now operational!

---

## 🌐 Access Your Application

### Frontend (User Interface)
👉 **http://localhost:5173**

### Backend API
👉 **http://localhost:8080**

### Database
- Address: `localhost:27017`
- Database: `hospital_booking`

---

## 🔓 Login Credentials

```
Email:    admin@hospico.com
Password: admin@123
```

Copy and paste these into the login page.

---

## 📋 What's Running

| Service | Port | Status | Technology |
|---------|------|--------|------------|
| MongoDB | 27017 | ✅ Running | Database |
| Backend API | 8080 | ✅ Running | Spring Boot + Java |
| Frontend | 5173 | ✅ Running | React + Vite |

---

## 🛠️ Development Commands

If you need to restart services:

### Start MongoDB
```powershell
& "C:\Program Files\MongoDB\Server\8.2\bin\mongod.exe" --dbpath "$env:APPDATA\MongoDB\data\db"
```

### Start Backend (from backend directory)
```powershell
cd C:\Users\Shivadhanu\OneDrive\Desktop\Draft\Hospico\backend
mvn spring-boot:run -DskipTests
```

### Start Frontend (from frontend directory)
```powershell
cd C:\Users\Shivadhanu\OneDrive\Desktop\Draft\Hospico\hospico-frontend-main
npm run dev
```

---

## 📊 Database Structure

MongoDB collections automatically created:
- `users` - User accounts
- `clinics` - Hospitals/clinics
- `doctors` - Doctor profiles
- `appointments` - Bookings
- `reviews` - Ratings and feedback
- `specializations` - Medical specializations
- `medical_records` - Patient documents

---

## 🎯 First Things To Do

1. ✅ **Frontend**: Open http://localhost:5173
2. ✅ **Login**: Use admin@hospico.com / admin@123
3. ✅ **Explore**: Browse clinics, doctors, and services
4. ✅ **API**: Test endpoints at http://localhost:8080
5. ✅ **Database**: Query MongoDB collections

---

## 🔗 Related Documentation

- [SYSTEM_STATUS.md](SYSTEM_STATUS.md) - Complete system status
- [SOLUTION_FOUND.md](SOLUTION_FOUND.md) - Migration details
- [QUICKSTART.md](QUICKSTART.md) - Setup instructions

---

## ⚡ Key Features

- **Authentication**: JWT-based security
- **Database**: MongoDB with auto-indexing
- **API**: RESTful endpoints
- **Frontend**: React with TypeScript
- **Mobile**: Capacitor support for mobile apps
- **Styling**: Tailwind CSS + Material-UI

---

**🎉 Everything is ready! Start building!**
