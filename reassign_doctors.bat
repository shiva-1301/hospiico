@echo off

echo Reassigning Dr. Vikram Patil to Kamineni...
curl -X POST http://localhost:8080/api/clinics/697392d8c6ba8a775ec4dc0a/doctors ^
-H "Content-Type: application/json" ^
-d "{\"id\": \"697393c4f86f6b62e6eb33f8\", \"name\": \"Dr. Vikram Patil\", \"qualifications\": \"MBBS, MD\", \"specialization\": \"General Medicine\", \"experience\": \"10 years\", \"biography\": \"experienced...\"}"

echo.
echo Reassigning Dr. Deepa Patil to Kamineni...
curl -X POST http://localhost:8080/api/clinics/697392d8c6ba8a775ec4dc0a/doctors ^
-H "Content-Type: application/json" ^
-d "{\"id\": \"697393c4f86f6b62e6eb33f9\", \"name\": \"Dr. Deepa Patil\", \"qualifications\": \"MBBS, MD\", \"specialization\": \"ENT\", \"experience\": \"11 years\", \"biography\": \"experienced...\"}"

echo.
echo Reassigning Dr. Deepa Rao to Supraja Hospitals...
curl -X POST http://localhost:8080/api/clinics/697392fdc6ba8a775ec4dc0c/doctors ^
-H "Content-Type: application/json" ^
-d "{\"id\": \"697393c4f86f6b62e6eb33fa\", \"name\": \"Dr. Deepa Rao\", \"qualifications\": \"MBBS, MD\", \"specialization\": \"General Medicine\", \"experience\": \"16 years\", \"biography\": \"experienced...\"}"

echo.
echo Reassigning Dr. Priya Nair to Supraja Hospitals...
curl -X POST http://localhost:8080/api/clinics/697392fdc6ba8a775ec4dc0c/doctors ^
-H "Content-Type: application/json" ^
-d "{\"id\": \"697393c4f86f6b62e6eb33fb\", \"name\": \"Dr. Priya Nair\", \"qualifications\": \"MBBS, MD\", \"specialization\": \"General Medicine\", \"experience\": \"10 years\", \"biography\": \"experienced...\"}"

echo.
echo Reassigning Dr. Divya Kumar to ANSH Hospital...
curl -X POST http://localhost:8080/api/clinics/6973b9726e827f23cc703029/doctors ^
-H "Content-Type: application/json" ^
-d "{\"id\": \"697393c4f86f6b62e6eb33fc\", \"name\": \"Dr. Divya Kumar\", \"qualifications\": \"MBBS, MD\", \"specialization\": \"General Medicine\", \"experience\": \"15 years\", \"biography\": \"experienced...\"}"

echo.
echo Done.
