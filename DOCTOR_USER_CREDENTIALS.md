# 👨‍⚕️ DOCTOR USER CREDENTIALS

## Doctor Account Created Successfully!

### Login Credentials:
- **Email:** `dr.sarah@cityhospital.com`
- **Password:** `doctor123`
- **Role:** `DOCTOR`

---

## Account Details:

- **Name:** The doctor's name from the database (auto-assigned)
- **Phone:** 9876543220
- **Age:** 35
- **Gender:** Female

---

## What This Account Can Do:

### ✅ Permitted Actions:
1. **View Own Appointments**
   - Endpoint: `GET /api/dashboard/appointments/my-patients`
   - See all patients who have booked appointments with this doctor

2. **View Own Statistics**
   - Endpoint: `GET /api/dashboard/stats`
   - Get patient count and appointment statistics

3. **View Patient Medical Records** (future feature)
   - Access medical history of their patients

### ❌ Restricted Actions:
- Cannot manage hospital settings
- Cannot view other doctors' appointments
- Cannot access admin endpoints
- Dashboard button is NOT visible on hospital profile pages (by design)

---

## Testing the Doctor Account:

### 1. Login via API
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "dr.sarah@cityhospital.com",
  "password": "doctor123"
}
```

### 2. View Your Patients
```bash
GET /api/dashboard/appointments/my-patients
Authorization: Bearer <your-token>
```

### 3. View Your Stats
```bash
GET /api/dashboard/stats
Authorization: Bearer <your-token>
```

---

## How It Works:

1. The doctor user is **automatically linked** to an existing doctor profile in the database
2. The `doctorId` field in the User entity points to the Doctor entity
3. All appointments with that doctor will appear in the doctor's dashboard

---

## Creating More Doctor Users:

To create additional doctor users, you can:

1. **Manually add through the database:**
   - Create a User with `role: DOCTOR`
   - Set `doctorId` to match an existing Doctor's ID

2. **Create a doctor signup endpoint** (future enhancement)
   - Add `/api/auth/doctor-signup` endpoint
   - Validate doctor credentials
   - Link to existing or new doctor profile

3. **Admin panel** (future enhancement)
   - Admin can create doctor accounts
   - Assign doctors to hospital clinics

---

## Important Notes:

> [!NOTE]
> The doctor is linked to an existing doctor profile from the `doctors` collection. If you want to link to a specific doctor, you'll need to know their doctor ID.

> [!TIP]
> To find all doctors in the database, query:
> ```bash
> GET /api/clinics/{hospitalId}/doctors
> ```

---

## Password Security:

The password is hashed using BCrypt before storing in the database. Never store plain-text passwords!

**Current Password:** `doctor123` (for testing only - change in production!)

---

## Next Steps:

1. ✅ Login with the credentials above
2. ✅ Test the doctor dashboard endpoints
3. 🔄 Create a Doctor Dashboard UI component (optional)
4. 🔄 Add doctor profile management features

Restart your backend server to run the seeder and create the doctor user!
