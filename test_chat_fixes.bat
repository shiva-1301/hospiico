@echo off
echo Testing "Near Me" Logic...
curl -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d "{\"messages\":[{\"role\":\"user\",\"content\":\"find hospitals near me\"}],\"latitude\":17.3850,\"longitude\":78.4867}"
echo.
echo.
echo Testing "Specialization" Logic (Expect JSON, no hallucinations)...
curl -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d "{\"messages\":[{\"role\":\"user\",\"content\":\"hospitals specialised in ENT\"}]}"
echo.
pause
