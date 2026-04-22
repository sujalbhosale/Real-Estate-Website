@echo off
echo ========================================
echo   Starting Real Estate India Server
echo ========================================
echo.

if not exist "build\server\PropertyServer.class" (
    echo [ERROR] Server not compiled yet!
    echo Run compile.bat first.
    echo.
    pause
    exit /b 1
)

echo Starting server on http://localhost:8080 ...
echo Press Ctrl+C to stop.
echo.

java -cp build server.PropertyServer
