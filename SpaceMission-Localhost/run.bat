@echo off
rem One-click launcher for Windows: double-click run.bat
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
  echo Java JDK 17 or newer was not found. Install a JDK from https://adoptium.net and run this again.
  pause
  exit /b 1
)

if not exist out mkdir out
echo Compiling...
javac -cp "lib/*" -d out *.java
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)

rem Opens the browser 4 seconds after the server starts.
start "" cmd /c "timeout /t 4 >nul & start http://localhost:8080"

java -cp "out;lib/*" MissionWebApp
pause
