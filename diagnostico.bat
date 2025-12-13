@echo off
echo ===================================
echo DIAGNOSTICO NEXUSPOS BACKEND
echo ===================================
echo.

echo 1. Verificando si PostgreSQL esta corriendo...
echo.
tasklist /FI "IMAGENAME eq postgres.exe" 2>NUL | find /I "postgres.exe" >NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] PostgreSQL esta corriendo
) else (
    echo [ERROR] PostgreSQL NO esta corriendo
    echo Solucion: Abre "Servicios" de Windows y busca PostgreSQL, inicialo
)
echo.

echo 2. Verificando puerto 8080...
echo.
netstat -ano | findstr :8080 >NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] Puerto 8080 esta en uso (servidor corriendo)
) else (
    echo [ERROR] Puerto 8080 esta libre (servidor NO esta corriendo)
)
echo.

echo 3. Configuracion actual:
echo.
echo Base de datos: nexuspos_db
echo Usuario: postgres
echo Password: 1234
echo Puerto BD: 5432
echo Puerto Server: 8080
echo.

echo 4. Que hacer ahora:
echo.
echo a) Si PostgreSQL NO esta corriendo:
echo    - Abre "Servicios" (services.msc)
echo    - Busca "postgresql-x64-XX"
echo    - Click derecho ^> Iniciar
echo.
echo b) Si PostgreSQL SI esta corriendo:
echo    - Abre pgAdmin
echo    - Verifica que exista la base de datos "nexuspos_db"
echo    - Si no existe, ejecuta: CREATE DATABASE nexuspos_db;
echo.
echo c) Copia el ERROR completo de la consola donde corre el servidor
echo.

pause
