@echo off
echo ========================================
echo  DIAGNOSTICO RAPIDO - NEXUSPOS BACKEND
echo ========================================
echo.

echo [1] Verificando Java...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java no esta instalado o no esta en PATH
    pause
    exit /b 1
)
echo.

echo [2] Verificando PostgreSQL...
psql --version 2>nul
if %errorlevel% neq 0 (
    echo ADVERTENCIA: psql no esta en PATH, pero puede estar instalado
) else (
    echo PostgreSQL encontrado
)
echo.

echo [3] Verificando puerto 8080...
netstat -ano | findstr :8080
if %errorlevel% equ 0 (
    echo ADVERTENCIA: Puerto 8080 esta en uso
) else (
    echo Puerto 8080 esta libre
)
echo.

echo [4] Verificando archivos del proyecto...
if exist "pom.xml" (
    echo OK: pom.xml encontrado
) else (
    echo ERROR: pom.xml NO encontrado
)

if exist "src\main\java\com\nexuspos\backend\NexusPosBackendApplication.java" (
    echo OK: Clase principal encontrada
) else (
    echo ERROR: Clase principal NO encontrada
)
echo.

echo [5] Compilando proyecto...
call mvnw.cmd clean compile -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Compilacion fallo
    pause
    exit /b 1
)
echo.

echo ========================================
echo  DIAGNOSTICO COMPLETADO
echo ========================================
pause
