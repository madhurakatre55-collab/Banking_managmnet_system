@echo off
echo ===================================================
echo   COMPILING BANKING MANAGEMENT SYSTEM (JAVA OOP)
echo ===================================================

if not exist bin mkdir bin

javac -d bin -cp "lib\*" src\com\bank\model\*.java src\com\bank\exception\*.java src\com\bank\dao\*.java src\com\bank\service\*.java src\com\bank\gui\*.java src\com\bank\web\*.java src\com\bank\Main.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation completed successfully into bin/ directory!
) else (
    echo [ERROR] Compilation failed. Please check the error output above.
)
pause
