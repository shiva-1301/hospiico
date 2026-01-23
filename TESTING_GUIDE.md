# 🔍 Testing Guide - Correct Credentials

## What You Did (with issues):

1. ✅ Logged in as **teja** (patient) - CORRECT
2. ✅ Booked appointment at Kamineni Hospital with Dr. Vikram Patil - CORRECT
3. ❌ Tried to login as doctor using **`rajesh.mehta@kamineniac.com`** - WRONG! This is the HOSPITAL OWNER

---

## 🎭 Understanding the Roles:

### Kamineni Hospital Has 3 Types of Users:

#### 1. 🏥 Hospital Owner (Rajesh Mehta)
- **Email:** `rajesh.mehta@kamineniac.com`
- **Password:** `hospital123`
- **Can Do:**
  - Manage the entire hospital
  - See ALL appointments at Kamineni
  - Add/edit doctors
  - View hospital dashboard
- **Dashboard Shows:** All appointments + all doctors at Kamineni

#### 2. 👨‍⚕️ Doctor (Dr. Vikram Patil)
- **Email:** `dr.vikram.patil@hospico.com`
- **Password:** `doctor123`
- **Can Do:**
  - View only THEIR OWN patient appointments
  - See their schedule
- **Dashboard Shows:** Only appointments booked with Dr. Vikram Patil

#### 3. 🧑 Patient (Teja)
- **Email:** `teja@gmail.com`
- **Password:** `user123`
- **Can Do:**
  - Book appointments
  - View hospitals
  - Cannot see dashboard

---

## ✅ Correct Testing Steps:

### Test 1: Hospital Owner Dashboard
```
1. Login as: rajesh.mehta@kamineniac.com / hospital123
2. Go to: Kamineni Hospital page
3. Click: Dashboard button
4. Should see: Your appointment with Dr. Vikram Patil + 3 doctors
```

### Test 2: Doctor Dashboard (Future Feature)
```
1. Login as: dr.vikram.patil@hospico.com / doctor123
2. Should see: Only appointments booked with Dr. Vikram Patil
3. Note: Doctor dashboard UI not yet created
```

### Test 3: Patient View
```
1. Login as: teja@gmail.com / user123
2. Go to: Any hospital
3. Dashboard button: Should NOT be visible
4. Can: Book appointments only
```

---

## 🔧 What I Just Fixed:

Updated `HospitalDashboard.tsx` to use the new role-based API endpoint:
- **Old:** `/api/appointments/clinic/{hospitalId}` (didn't work with auth)
- **New:** `/api/dashboard/appointments/my-hospital` (uses role-based access)

Now when you login as **rajesh.mehta@kamineniac.com**, you should see:
- ✅ The appointment you booked
- ✅ 3 doctors at Kamineni Hospital

---

## 🧪 Try Again:

1. **Refresh the frontend** (or restart if needed)
2. **Login as:** `rajesh.mehta@kamineniac.com` / `hospital123`
3. **Go to:** Kamineni Hospital page
4. **Click:** Dashboard button
5. **You should now see:** Your appointment and doctor count!

---

## 📋 All Kamineni Hospital Credentials:

| Name | Email | Password | Role |
|------|-------|----------|------|
| Dr. Rajesh Mehta | `rajesh.mehta@kamineniac.com` | `hospital123` | Hospital Owner |
| Dr. Vikram Patil | `dr.vikram.patil@hospico.com` | `doctor123` | Doctor |
| Dr. Deepa Patil | `dr.deepa.patil@hospico.com` | `doctor123` | Doctor |
| Dr. Rajesh Prasad | `dr.rajesh.prasad@hospico.com` | `doctor123` | Doctor |
