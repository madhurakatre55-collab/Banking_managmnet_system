@echo off
echo ========================================================
echo   STARTING BANKING MANAGEMENT SYSTEM - WEB PROTOTYPE
echo ========================================================

start "" http://localhost:8080
java -cp "bin;lib\*" com.bank.Main 8080 --no-gui
pause
