@echo off
echo ========================================================
echo   STARTING BANKING MANAGEMENT SYSTEM (SWING GUI + WEB)
echo ========================================================

start "" http://localhost:8080
java -cp "bin;lib\*" com.bank.Main 8080
pause
