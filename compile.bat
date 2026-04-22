@echo off
echo ========================================
echo   Compiling Real Estate India Server
echo ========================================
echo.

if not exist "build" mkdir build

echo Compiling Java source files...
javac -d build -sourcepath src src\utils\JsonUtil.java src\models\Property.java src\models\Inquiry.java src\data\PropertyDAO.java src\data\InquiryDAO.java src\handlers\StaticFileHandler.java src\handlers\PropertyHandler.java src\handlers\InquiryHandler.java src\server\PropertyServer.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Compilation successful!
    echo Output directory: build\
    echo.
    echo Run 'run.bat' to start the server.
) else (
    echo.
    echo [ERROR] Compilation failed!
    echo Check the error messages above.
)
echo.
pause
