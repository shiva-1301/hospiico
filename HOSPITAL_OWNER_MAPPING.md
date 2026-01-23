# Hospital-Owner Mapping Report

## ✅ Successfully Created Hospital

### Hospital Details
- **Hospital Name:** City General Hospital
- **Hospital ID:** (Retrieved from database)
- **Location:** Plot No. 45, Banjara Hills Road, Hyderabad, Telangana - 500034
- **Specializations:** Cardiology, Orthopedics, General Medicine
- **Contact:** 040-23456789 | info@citygeneralhospital.com

### Owner Details
- **Owner Name:** Dr. Rajesh Kumar
- **Owner ID:** `6973db4a8d6c5155576aaafa`
- **Email:** rajesh.kumar@cityhospital.com
- **Role:** HOSPITAL
- **Phone:** 9876543210

### Ownership Relationship
```
User (HOSPITAL role)          Hospital/Clinic
┌─────────────────────┐      ┌──────────────────────┐
│ ID: 6973db4a...     │◄────►│ City General Hospital│
│ Name: Dr. Rajesh    │      │ ownerId: 6973db4a... │
│ Role: HOSPITAL      │      │ Location: Hyderabad  │
│ hospitalId: [ID]    │      └──────────────────────┘
└─────────────────────┘
```

## Ownership Rules Enforced

1. ✅ **One User → One Hospital**
   - Each HOSPITAL role user can own exactly 1 hospital
   - `User.hospitalId` links to their owned hospital

2. ✅ **One Hospital → One Owner**
   - Each hospital has exactly 1 owner
   - `Clinic.ownerId` links to the owner user

3. ✅ **Unique Ownership**
   - No two users can own the same hospital
   - No user can own multiple hospitals (enforced by single `hospitalId` field)

## Current System Users

### Admin User
- **Email:** shiva@gmail.com
- **Role:** ADMIN
- **Owns Hospital:** No (admins don't own hospitals, they manage the platform)

### Hospital Owner
- **Email:** rajesh.kumar@cityhospital.com
- **Role:** HOSPITAL
- **Owns Hospital:** Yes - City General Hospital
- **Can Do:**
  - Edit hospital profile
  - Manage appointments
  - Add/remove doctors
  - Respond to reviews

## How to Login as Hospital Owner

```bash
# Login credentials
Email: rajesh.kumar@cityhospital.com
Password: hospital123
```

Navigate to: `http://localhost:5173/partner-login`

After login, you'll be redirected to `/hospital-dashboard` (which needs to be built).
