@echo off
title Apex Banking Management System - Java OOP Mini Project
echo ========================================================
echo   APEX BANKING MANAGEMENT SYSTEM (BTech OOP in Java)
echo ========================================================

if not exist bin mkdir bin

echo [1/2] Compiling Java source files...
javac -d bin -cp "lib\*" src\com\bank\model\*.java src\com\bank\exception\*.java src\com\bank\dao\*.java src\com\bank\service\*.java src\com\bank\gui\*.java src\com\bank\web\*.java src\com\bank\Main.java

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching Banking System and opening Web Prototype...
start "" http://localhost:8080
java -cp "bin;lib\*" com.bank.Main 8080
pause
