package com.juegos.common;

/**
 * Constantes utilizadas en todo el sistema de juegos.
 * Centraliza valores como puertos, configuraciones y mensajes.
 * 
 * 
 */
public class GameConstants {
    
    // Configuración de red
    public static final String DEFAULT_SERVER_HOST = "localhost";
    public static final int DEFAULT_SERVER_PORT = 5432; // Puerto por defecto para el servidor de juegos
    public static final int CONNECTION_TIMEOUT = 5000; // 5 segundos
    
    // Tipos de juego
    public static final String GAME_BATTLESHIP = "BATTLESHIP";
    
    // Modos de juego
    public static final String MODE_VS_HUMAN = "VS_HUMAN";
    public static final String MODE_VS_AI = "VS_AI";
    
    // Estados del juego
    public static final String STATE_WAITING = "WAITING";
    public static final String STATE_PLAYING = "PLAYING";
    public static final String STATE_FINISHED = "FINISHED";
    public static final String STATE_PAUSED = "PAUSED";
    
    // Tipos de mensaje de red
    public static final String MSG_CONNECT = "CONNECT";
    public static final String MSG_DISCONNECT = "DISCONNECT";
    public static final String MSG_GAME_SELECT = "GAME_SELECT";
    public static final String MSG_MOVE = "MOVE";
    public static final String MSG_GAME_STATE = "GAME_STATE";
    public static final String MSG_GAME_RESULT = "GAME_RESULT";
    public static final String MSG_ERROR = "ERROR";
    public static final String MSG_HEARTBEAT = "HEARTBEAT";
    
    // Resultados de juego
    public static final String RESULT_WIN = "WIN";
    public static final String RESULT_LOSE = "LOSE";
    public static final String RESULT_DRAW = "DRAW";
    public static final String RESULT_ABANDONED = "ABANDONED";

    // Configuración de Batalla Naval
    public static final int BATTLESHIP_BOARD_SIZE = 10;
    public static final char BATTLESHIP_WATER = '~';
    public static final char BATTLESHIP_SHIP = 'S';
    public static final char BATTLESHIP_HIT = 'X';
    public static final char BATTLESHIP_MISS = 'O';
    
    // Tamaños de barcos para Batalla Naval
    public static final int[] SHIP_SIZES = {5, 4, 3, 3, 2}; // Portaaviones, Acorazado, Crucero, Submarino, Destructor
    public static final String[] SHIP_NAMES = {"Portaaviones", "Acorazado", "Crucero", "Submarino", "Destructor"};
    
    // Configuración de interfaz
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final int GAME_WINDOW_WIDTH = 1000;
    public static final int GAME_WINDOW_HEIGHT = 700;
    
    // Configuración de IA
    public static final int AI_THINKING_DELAY = 1000; // 1 segundo
    public static final int AI_MAX_DEPTH = 9; // Para minimax en Tic-Tac-Toe
    
    // Mensajes de usuario
    public static final String MSG_CONNECTION_SUCCESS = "Conectado al servidor exitosamente";
    public static final String MSG_CONNECTION_FAILED = "Error al conectar con el servidor";
    public static final String MSG_GAME_STARTED = "¡Juego iniciado!";
    public static final String MSG_YOUR_TURN = "Es tu turno";
    public static final String MSG_OPPONENT_TURN = "Turno del oponente";
    public static final String MSG_GAME_WON = "¡Has ganado!";
    public static final String MSG_GAME_LOST = "Has perdido";
    public static final String MSG_GAME_DRAW = "¡Empate!";
    
    // Evitar instanciación
    private GameConstants() {
        throw new AssertionError("Esta clase no debe ser instanciada");
    }
}
