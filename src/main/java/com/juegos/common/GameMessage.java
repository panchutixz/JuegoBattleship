package com.juegos.common;

/**
 * Representa un mensaje que se envía entre cliente y servidor.
 * Contiene toda la información necesaria para la comunicación en red.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0
 */
public class GameMessage {
    
    private String type;           // Tipo de mensaje (ver GameConstants.MSG_*)
    private String gameType;       // Tipo de juego (TICTACTOE, BATTLESHIP)
    private String mode;           // Modo de juego (VS_HUMAN, VS_AI)
    private String playerId;       // ID del jugador que envía el mensaje
    private Object data;           // Datos específicos del mensaje
    private String timestamp;      // Marca de tiempo del mensaje
    private String sessionId;      // ID de la sesión de juego
    
    /**
     * Constructor por defecto.
     */
    public GameMessage() {
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Constructor con tipo de mensaje.
     * @param type Tipo del mensaje
     */
    public GameMessage(String type) {
        this();
        this.type = type;
    }
    
    /**
     * Constructor completo.
     * @param type Tipo del mensaje
     * @param gameType Tipo de juego
     * @param playerId ID del jugador
     * @param data Datos del mensaje
     */
    public GameMessage(String type, String gameType, String playerId, Object data) {
        this(type);
        this.gameType = gameType;
        this.playerId = playerId;
        this.data = data;
    }
    
    // Getters y Setters
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * Crea un mensaje de conexión.
     * @param playerId ID del jugador
     * @return Mensaje de conexión
     */
    public static GameMessage createConnectMessage(String playerId) {
        GameMessage message = new GameMessage(GameConstants.MSG_CONNECT);
        message.setPlayerId(playerId);
        return message;
    }
    
    /**
     * Crea un mensaje de desconexión.
     * @param playerId ID del jugador
     * @return Mensaje de desconexión
     */
    public static GameMessage createDisconnectMessage(String playerId) {
        GameMessage message = new GameMessage(GameConstants.MSG_DISCONNECT);
        message.setPlayerId(playerId);
        return message;
    }
    
    /**
     * Crea un mensaje de selección de juego.
     * @param playerId ID del jugador
     * @param gameType Tipo de juego
     * @param mode Modo de juego
     * @return Mensaje de selección
     */
    public static GameMessage createGameSelectMessage(String playerId, String gameType, String mode) {
        GameMessage message = new GameMessage(GameConstants.MSG_GAME_SELECT);
        message.setPlayerId(playerId);
        message.setGameType(gameType);
        message.setMode(mode);
        return message;
    }
    
    /**
     * Crea un mensaje de movimiento.
     * @param playerId ID del jugador
     * @param gameType Tipo de juego
     * @param moveData Datos del movimiento
     * @return Mensaje de movimiento
     */
    public static GameMessage createMoveMessage(String playerId, String gameType, Object moveData) {
        GameMessage message = new GameMessage(GameConstants.MSG_MOVE);
        message.setPlayerId(playerId);
        message.setGameType(gameType);
        message.setData(moveData);
        return message;
    }
    
    /**
     * Crea un mensaje de estado del juego.
     * @param gameType Tipo de juego
     * @param gameState Estado del juego
     * @return Mensaje de estado
     */
    public static GameMessage createGameStateMessage(String gameType, Object gameState) {
        GameMessage message = new GameMessage(GameConstants.MSG_GAME_STATE);
        message.setGameType(gameType);
        message.setData(gameState);
        return message;
    }
    
    /**
     * Crea un mensaje de resultado del juego.
     * @param gameType Tipo de juego
     * @param result Resultado del juego
     * @return Mensaje de resultado
     */
    public static GameMessage createGameResultMessage(String gameType, Object result) {
        GameMessage message = new GameMessage(GameConstants.MSG_GAME_RESULT);
        message.setGameType(gameType);
        message.setData(result);
        return message;
    }
    
    /**
     * Crea un mensaje de error.
     * @param errorMessage Mensaje de error
     * @return Mensaje de error
     */
    public static GameMessage createErrorMessage(String errorMessage) {
        GameMessage message = new GameMessage(GameConstants.MSG_ERROR);
        message.setData(errorMessage);
        return message;
    }
    
    @Override
    public String toString() {
        return String.format("GameMessage{type='%s', gameType='%s', playerId='%s', data=%s}", 
                           type, gameType, playerId, data);
    }
}
