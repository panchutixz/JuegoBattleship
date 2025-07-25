package com.juegos.servidor;

import com.juegos.common.GameConstants;
import com.juegos.battleship.BattleshipGame;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Representa una sala de juego donde los jugadores pueden competir.
 * Maneja la lógica específica de cada tipo de juego y coordina los turnos.
 * 
 * 
 */
public class GameRoom {
    
    private static final Logger LOGGER = Logger.getLogger(GameRoom.class.getName());
    
    private final String roomId;
    private final String gameType;
    private final String mode;
    private final List<ClientHandler> players;
    private final int maxPlayers;
    
    private Object gameInstance;    // Instancia del juego específico
    private String currentPlayerId; // ID del jugador con el turno actual
    private String gameState;       // Estado actual del juego
    private boolean gameStarted;
    private boolean gameFinished;
    
    /**
     * Constructor de la sala de juego.
     * @param roomId ID único de la sala
     * @param gameType Tipo de juego (BATTLESHIP)
     * @param mode Modo de juego (VS_HUMAN, VS_AI)
     */
    public GameRoom(String roomId, String gameType, String mode) {
        this.roomId = roomId;
        this.gameType = gameType;
        this.mode = mode;
        this.players = new ArrayList<>();
        this.maxPlayers = GameConstants.MODE_VS_AI.equals(mode) ? 1 : 2;
        this.gameState = GameConstants.STATE_WAITING;
        this.gameStarted = false;
        this.gameFinished = false;
    }
    
    /**
     * Agrega un jugador a la sala.
     * @param player Manejador del cliente
     * @return true si se agregó exitosamente
     */
    public boolean addPlayer(ClientHandler player) {
        if (players.size() < maxPlayers && !hasPlayer(player.getClientId())) {
            players.add(player);
            player.setCurrentRoomId(roomId);
            
            LOGGER.info("Jugador agregado a sala " + roomId + ": " + player.getClientId());
            
            // Si es el primer jugador, asignarle el turno
            if (players.size() == 1) {
                currentPlayerId = player.getClientId();
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Remueve un jugador de la sala.
     * @param playerId ID del jugador
     * @return true si se removió exitosamente
     */
    public boolean removePlayer(String playerId) {
        ClientHandler toRemove = null;
        for (ClientHandler player : players) {
            if (player.getClientId().equals(playerId)) {
                toRemove = player;
                break;
            }
        }
        
        if (toRemove != null) {
            players.remove(toRemove);
            toRemove.setCurrentRoomId(null);
            
            LOGGER.info("Jugador removido de sala " + roomId + ": " + playerId);
            
            // Si el juego estaba en progreso, terminarlo
            if (gameStarted && !gameFinished) {
                endGame("ABANDONED");
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Verifica si un jugador está en la sala.
     * @param playerId ID del jugador
     * @return true si está en la sala
     */
    public boolean hasPlayer(String playerId) {
        return players.stream().anyMatch(p -> p.getClientId().equals(playerId));
    }
    
    /**
     * Verifica si la sala tiene espacio disponible.
     * @return true si hay espacio
     */
    public boolean hasSpace() {
        return players.size() < maxPlayers;
    }
    
    /**
     * Verifica si la sala está llena.
     * @return true si está llena
     */
    public boolean isFull() {
        return players.size() == maxPlayers;
    }
    
    /**
     * Verifica si la sala está vacía.
     * @return true si está vacía
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }
    
    /**
     * Inicializa el juego según el tipo.
     */
    public void initializeGame() {
        switch (gameType) {
            case GameConstants.GAME_BATTLESHIP:
                gameInstance = new BattleshipGame();
                break;
            default:
                LOGGER.warning("Tipo de juego no reconocido: " + gameType);
                return;
        }
        
        gameState = GameConstants.STATE_PLAYING;
        gameStarted = true;
        
        LOGGER.info("Juego inicializado en sala " + roomId + ": " + gameType);
        
        // Notificar a todos los jugadores
        broadcastToAll("GAME_INITIALIZED:" + gameType);
    }
    
    /**
     * Procesa un movimiento de un jugador.
     * @param playerId ID del jugador
     * @param moveData Datos del movimiento
     */
    public void processMove(String playerId, String moveData) {
        if (!gameStarted || gameFinished) {
            sendToPlayer(playerId, "ERROR:El juego no está activo");
            return;
        }
        
        if (!currentPlayerId.equals(playerId)) {
            sendToPlayer(playerId, "ERROR:No es tu turno");
            return;
        }
        
        boolean validMove = false;
        String result = null;
        
        try {
            switch (gameType) {
                    
                case GameConstants.GAME_BATTLESHIP:
                    BattleshipGame battleship = (BattleshipGame) gameInstance;
                    validMove = battleship.makeMove(playerId, moveData);
                    if (validMove) {
                        result = battleship.checkGameEnd();
                    }
                    break;
            }
            
            if (validMove) {
                // Notificar el movimiento a todos los jugadores
                broadcastToAll("MOVE_MADE:" + playerId + ":" + moveData);
                
                // Verificar si el juego terminó
                if (result != null) {
                    endGame(result);
                } else {
                    // Cambiar turno
                    switchTurn();
                }
            } else {
                sendToPlayer(playerId, "ERROR:Movimiento inválido");
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error procesando movimiento: " + e.getMessage());
            sendToPlayer(playerId, "ERROR:Error interno del servidor");
        }
    }
    
    /**
     * Cambia el turno al siguiente jugador.
     */
    private void switchTurn() {
        if (GameConstants.MODE_VS_AI.equals(mode)) {
            // En modo vs IA, el turno siempre vuelve al jugador humano
            // (la IA responderá automáticamente)
            return;
        }
        
        // En modo vs humano, alternar entre jugadores
        for (ClientHandler player : players) {
            if (!player.getClientId().equals(currentPlayerId)) {
                currentPlayerId = player.getClientId();
                break;
            }
        }
        
        broadcastToAll("TURN_CHANGED:" + currentPlayerId);
    }
    
    /**
     * Termina el juego con un resultado específico.
     * @param result Resultado del juego
     */
    private void endGame(String result) {
        gameFinished = true;
        gameState = GameConstants.STATE_FINISHED;
        
        broadcastToAll("GAME_ENDED:" + result);
        
        LOGGER.info("Juego terminado en sala " + roomId + " con resultado: " + result);
    }
    
    /**
     * Envía un mensaje a todos los jugadores en la sala.
     * @param message Mensaje a enviar
     */
    public void broadcastToAll(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Envía un mensaje a un jugador específico.
     * @param playerId ID del jugador
     * @param message Mensaje a enviar
     */
    public void sendToPlayer(String playerId, String message) {
        for (ClientHandler player : players) {
            if (player.getClientId().equals(playerId)) {
                player.sendMessage(message);
                break;
            }
        }
    }
    
    // Getters
    
    public String getRoomId() {
        return roomId;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    public String getMode() {
        return mode;
    }
    
    public List<ClientHandler> getPlayers() {
        return new ArrayList<>(players);
    }
    
    public String getCurrentPlayerId() {
        return currentPlayerId;
    }
    
    public String getGameState() {
        return gameState;
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public boolean isGameFinished() {
        return gameFinished;
    }
    
    public Object getGameInstance() {
        return gameInstance;
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
}
