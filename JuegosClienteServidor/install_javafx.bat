@echo off
echo ================================================
echo    INSTALADOR DE JAVAFX 8
echo ================================================
echo.

set JAVAFX_URL=https://download2.gluonhq.com/openjfx/8u372/openjfx-8u372-sdk-windows.zip
set JAVAFX_ZIP=javafx-8.zip
set JAVAFX_DIR=C:\javafx-8

echo Descargando JavaFX 8...
echo URL: %JAVAFX_URL%
echo.

REM Verificar si ya existe
if exist "%JAVAFX_DIR%" (
    echo JavaFX ya está instalado en %JAVAFX_DIR%
    echo.
    echo ¿Desea reinstalar? (S/N)
    set /p REINSTALL=
    if /i not "%REINSTALL%"=="S" goto :END
    rmdir /s /q "%JAVAFX_DIR%"
)

REM Crear directorio temporal
if not exist "temp" mkdir temp
cd temp

echo Descargando JavaFX...
REM Usar PowerShell para descargar
powershell -Command "& {Invoke-WebRequest -Uri '%JAVAFX_URL%' -OutFile '%JAVAFX_ZIP%'}"

if not exist "%JAVAFX_ZIP%" (
    echo ERROR: No se pudo descargar JavaFX
    echo.
    echo Descarga manual:
    echo 1. Ve a: https://gluonhq.com/products/javafx/
    echo 2. Descarga JavaFX 8 SDK
    echo 3. Extrae a C:\javafx-8\
    echo.
    pause
    goto :CLEANUP
)

echo Extrayendo JavaFX...
REM Usar PowerShell para extraer
powershell -Command "& {Expand-Archive -Path '%JAVAFX_ZIP%' -DestinationPath '.' -Force}"

REM Buscar el directorio extraído
for /d %%d in (openjfx-*) do (
    echo Moviendo %%d a %JAVAFX_DIR%...
    move "%%d" "%JAVAFX_DIR%"
    goto :VERIFY
)

:VERIFY
cd ..
if exist "%JAVAFX_DIR%\lib\*.jar" (
    echo.
    echo ================================================
    echo    INSTALACIÓN EXITOSA
    echo ================================================
    echo JavaFX 8 instalado correctamente en:
    echo %JAVAFX_DIR%
    echo.
    echo Ahora puedes ejecutar:
    echo - run_server.bat para el servidor
    echo - run_client.bat para el cliente
    echo.
) else (
    echo ERROR: La instalación falló
    echo.
    echo Instalación manual:
    echo 1. Descarga JavaFX 8 desde: https://gluonhq.com/products/javafx/
    echo 2. Extrae el contenido a C:\javafx-8\
    echo 3. Asegúrate de que exista C:\javafx-8\lib\
)

:CLEANUP
if exist "temp" rmdir /s /q "temp"

:END
echo.
pause
