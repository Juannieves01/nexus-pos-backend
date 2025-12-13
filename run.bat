@echo off
echo ========================================
echo  NEXUSPOS BACKEND - INICIANDO...
echo ========================================
echo.
echo Verificando Java...
java -version
echo.
echo Iniciando Spring Boot...
echo.
cd %~dp0
mvnw.cmd spring-boot:run
pause
