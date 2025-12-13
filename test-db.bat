@echo off
echo Probando conexion a PostgreSQL...
echo.

cd /d "%~dp0"

echo Si ves el mensaje "Started NexusPosBackendApplication", el servidor inicio correctamente.
echo Si ves un error con "PSQLException" o "Connection refused", PostgreSQL no esta conectado.
echo.

echo Ejecutando servidor...
call mvnw.cmd spring-boot:run
