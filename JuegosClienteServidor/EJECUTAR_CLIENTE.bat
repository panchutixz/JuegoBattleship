@echo off
echo ================================================
echo    JUEGOS CLIENTE-SERVIDOR - CLIENTE
echo ================================================
echo.

REM Verificar si está compilado
if not exist "out" (
    echo ERROR: El proyecto no está compilado.
    echo Ejecuta EJECUTAR_SERVIDOR.bat primero para compilar.
    pause
    exit /b 1
)

echo.
echo ================================================
echo    INICIANDO CLIENTE DE JUEGOS
echo ================================================
echo.

REM Ejecutar cliente
java -cp "out" com.juegos.SwingMainApplication

pause
