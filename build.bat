@echo off
echo Building E-Commerce Microservices...

REM Build parent project and all modules
call mvn clean install -DskipTests

echo Build completed!
pause
