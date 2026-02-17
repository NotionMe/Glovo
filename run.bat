@echo off
setlocal

set APP_NAME=Delivery Dispatch Core
set DEFAULT_PORT=8080

echo === %APP_NAME% ===
echo.

:: Check Java
where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed. Java 21+ is required.
    exit /b 1
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%~i
echo Java version: %JAVA_VER%

:: Parse argument
set ACTION=%1
if "%ACTION%"=="" set ACTION=run

if "%ACTION%"=="run" goto :run
if "%ACTION%"=="build" goto :build
if "%ACTION%"=="test" goto :test
if "%ACTION%"=="jar" goto :jar
goto :usage

:run
echo Starting application on port %DEFAULT_PORT%...
echo Swagger UI: http://localhost:%DEFAULT_PORT%/swagger-ui.html
echo.
call mvnw.cmd spring-boot:run -q
goto :end

:build
echo Building project...
call mvnw.cmd clean package -DskipTests
echo.
echo Build complete: target\delivery-dispatch-core-1.0.0-SNAPSHOT.jar
goto :end

:test
echo Running tests...
call mvnw.cmd test
goto :end

:jar
set JAR_FILE=target\delivery-dispatch-core-1.0.0-SNAPSHOT.jar
if not exist "%JAR_FILE%" (
    echo JAR not found. Building first...
    call mvnw.cmd clean package -DskipTests
)
echo Starting from JAR on port %DEFAULT_PORT%...
echo Swagger UI: http://localhost:%DEFAULT_PORT%/swagger-ui.html
echo.
java -jar "%JAR_FILE%"
goto :end

:usage
echo Usage: run.bat [run^|build^|test^|jar]
echo.
echo   run    - Start the application with Maven (default)
echo   build  - Build the JAR without running tests
echo   test   - Run all tests
echo   jar    - Build (if needed) and run the JAR directly
exit /b 1

:end
endlocal
