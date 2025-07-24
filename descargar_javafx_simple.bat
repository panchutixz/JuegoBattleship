@echo off
echo ================================================
echo    SOLUCIÓN SIMPLE PARA JAVAFX
echo ================================================
echo.

echo MÉTODO SIMPLE Y DIRECTO:
echo.
echo 1. Ve a esta URL EXACTA:
echo    https://repo1.maven.org/maven2/org/openjfx/javafx-controls/8.0.271/
echo.
echo 2. Descarga estos 3 archivos JAR:
echo    - javafx-controls-8.0.271.jar
echo    - javafx-fxml-8.0.271.jar  
echo    - javafx-base-8.0.271.jar
echo.
echo 3. Crea la carpeta: C:\javafx-8\lib\
echo.
echo 4. Copia los 3 archivos JAR a esa carpeta
echo.
echo 5. Ejecuta run_server.bat
echo.

set /p CONTINUAR="¿Quieres que abra las URLs para descargar? (S/N): "

if /i "%CONTINUAR%"=="S" (
    echo Abriendo URLs de descarga...
    start https://repo1.maven.org/maven2/org/openjfx/javafx-controls/8.0.271/javafx-controls-8.0.271.jar
    timeout /t 2 /nobreak >nul
    start https://repo1.maven.org/maven2/org/openjfx/javafx-fxml/8.0.271/javafx-fxml-8.0.271.jar
    timeout /t 2 /nobreak >nul
    start https://repo1.maven.org/maven2/org/openjfx/javafx-base/8.0.271/javafx-base-8.0.271.jar
    
    echo.
    echo URLs abiertas en tu navegador.
    echo Descarga los 3 archivos JAR y ponlos en C:\javafx-8\lib\
)

echo.
echo ALTERNATIVA MÁS SIMPLE:
echo Si esto es demasiado complicado, puedo crear una versión
echo del proyecto que use solo Swing en lugar de JavaFX.
echo.

pause
