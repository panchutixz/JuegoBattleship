@echo off
echo ================================================
echo    JUEGOS CLIENTE-SERVIDOR - SERVIDOR
echo ================================================
echo.

echo [1/3] Compilando el proyecto...
if not exist "out" mkdir out

REM Compilar proyecto para Java 8
javac -source 8 -target 8 -d out src\main\java\com\juegos\SwingMainApplication.java src\main\java\com\juegos\ui\TicTacToeWindow.java src\main\java\com\juegos\ui\BattleshipWindow.java src\main\java\com\juegos\battleship\*.java src\main\java\com\juegos\common\*.java src\main\java\com\juegos\servidor\*.java src\main\java\com\juegos\tictactoe\*.java

if %ERRORLEVEL% neq 0 (
    echo ERROR: Falló la compilación
    pause
    exit /b 1
)

echo [2/3] Compilación exitosa!
echo [3/3] Listo para ejecutar!

echo.
echo ================================================
echo    INICIANDO SERVIDOR DE JUEGOS
echo ================================================
echo.

REM Ejecutar servidor
java -cp "out" com.juegos.servidor.GameServer

pause
