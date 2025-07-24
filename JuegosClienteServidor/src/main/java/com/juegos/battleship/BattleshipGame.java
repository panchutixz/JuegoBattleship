package com.juegos.battleship;

import com.juegos.common.GameConstants;
import com.juegos.common.Player;

/**
 * Implementa la lógica completa del juego Batalla Naval.
 * Maneja dos tableros (jugador y oponente) y coordina las fases del juego.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0
 */
public class BattleshipGame {
    
    private Board playerBoard;      // Tablero del jugador
    private Board opponentBoard;    // Tablero del oponente
    private Player player1;         // Primer jugador
    private Player player2;         // Segundo jugador (puede ser IA)
    private Player currentPlayer;   // Jugador con el turno actual
    
    private String gamePhase;       // SETUP, BATTLE, FINISHED
    private boolean gameEnded;
    private String winner;
    private int totalTurns;
    
    /**
     * Constructor del juego Batalla Naval.
     */
    public BattleshipGame() {
        this.playerBoard = new Board();
        this.opponentBoard = new Board();
        this.gamePhase = "SETUP";
        this.gameEnded = false;
        this.winner = null;
        this.totalTurns = 0;
    }
    
    /**
     * Configura los jugadores del juego.
     * @param player1 Primer jugador
     * @param player2 Segundo jugador
     */
    public void setPlayers(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1; // El primer jugador comienza
    }
    
    /**
     * Inicia la fase de batalla después de la configuración.
     */
    public void startBattlePhase() {
        if (playerBoard.isSetupComplete() && opponentBoard.isSetupComplete()) {
            gamePhase = "BATTLE";
            currentPlayer = player1; // El primer jugador comienza disparando
        }
    }
    
    /**
     * Procesa un movimiento del jugador.
     * @param playerId ID del jugador
     * @param moveData Datos del movimiento
     * @return true si el movimiento es válido
     */
    public boolean makeMove(String playerId, String moveData) {
        if (gameEnded) {
            return false;
        }
        
        // Verificar que es el turno del jugador correcto
        if (!currentPlayer.getId().equals(playerId)) {
            return false;
        }
        
        try {
            if ("SETUP".equals(gamePhase)) {
                return processSetupMove(playerId, moveData);
            } else if ("BATTLE".equals(gamePhase)) {
                return processBattleMove(playerId, moveData);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return false;
    }
    
    /**
     * Procesa un movimiento durante la fase de configuración.
     * @param playerId ID del jugador
     * @param moveData Datos del movimiento (formato: "PLACE:shipIndex:row:col:horizontal")
     * @return true si es válido
     */
    private boolean processSetupMove(String playerId, String moveData) {
        String[] parts = moveData.split(":");
        if (parts.length < 5 || !"PLACE".equals(parts[0])) {
            return false;
        }
        
        try {
            int shipIndex = Integer.parseInt(parts[1]);
            int row = Integer.parseInt(parts[2]);
            int col = Integer.parseInt(parts[3]);
            boolean horizontal = Boolean.parseBoolean(parts[4]);
            
            // Determinar en qué tablero colocar
            Board targetBoard = getPlayerBoard(playerId);
            if (targetBoard == null) {
                return false;
            }
            
            boolean placed = targetBoard.placeShip(shipIndex, row, col, horizontal);
            
            // Si ambos tableros están completos, iniciar batalla
            if (placed && playerBoard.isSetupComplete() && opponentBoard.isSetupComplete()) {
                startBattlePhase();
            }
            
            return placed;
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Procesa un movimiento durante la fase de batalla.
     * @param playerId ID del jugador
     * @param moveData Datos del movimiento (formato: "SHOOT:row:col")
     * @return true si es válido
     */
    private boolean processBattleMove(String playerId, String moveData) {
        String[] parts = moveData.split(":");
        if (parts.length < 3 || !"SHOOT".equals(parts[0])) {
            return false;
        }
        
        try {
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            
            // Determinar tablero objetivo (el del oponente)
            Board targetBoard = getOpponentBoard(playerId);
            if (targetBoard == null) {
                return false;
            }
            
            // Procesar disparo
            String result = targetBoard.receiveShot(row, col);
            
            if (!"INVALID".equals(result)) {
                totalTurns++;
                
                // Registrar disparo en el tablero del jugador
                Board playerBoardRef = getPlayerBoard(playerId);
                if (playerBoardRef != null) {
                    playerBoardRef.recordShot(row, col, result);
                }
                
                // Verificar si el juego terminó
                if (targetBoard.allShipsSunk()) {
                    endGame(playerId);
                } else {
                    // Cambiar turno solo si no fue un hit
                    if ("MISS".equals(result)) {
                        switchPlayer();
                    }
                }
                
                return true;
            }
            
        } catch (NumberFormatException e) {
            return false;
        }
        
        return false;
    }
    
    /**
     * Obtiene el tablero del jugador especificado.
     * @param playerId ID del jugador
     * @return Tablero del jugador o null
     */
    private Board getPlayerBoard(String playerId) {
        if (player1 != null && player1.getId().equals(playerId)) {
            return playerBoard;
        } else if (player2 != null && player2.getId().equals(playerId)) {
            return opponentBoard;
        }
        return null;
    }
    
    /**
     * Obtiene el tablero del oponente del jugador especificado.
     * @param playerId ID del jugador
     * @return Tablero del oponente o null
     */
    private Board getOpponentBoard(String playerId) {
        if (player1 != null && player1.getId().equals(playerId)) {
            return opponentBoard;
        } else if (player2 != null && player2.getId().equals(playerId)) {
            return playerBoard;
        }
        return null;
    }
    
    /**
     * Cambia el turno al siguiente jugador.
     */
    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
    
    /**
     * Termina el juego con un ganador.
     * @param winnerId ID del jugador ganador
     */
    private void endGame(String winnerId) {
        gameEnded = true;
        gamePhase = "FINISHED";
        winner = winnerId;
        
        // Actualizar estadísticas
        if (player1 != null && player1.getId().equals(winnerId)) {
            player1.incrementWins();
            if (player2 != null) player2.incrementLosses();
        } else if (player2 != null && player2.getId().equals(winnerId)) {
            player2.incrementWins();
            if (player1 != null) player1.incrementLosses();
        }
    }
    
    /**
     * Configura automáticamente los barcos de un jugador.
     * @param playerId ID del jugador
     * @return true si se configuraron exitosamente
     */
    public boolean autoSetupShips(String playerId) {
        Board board = getPlayerBoard(playerId);
        if (board != null) {
            return board.autoPlaceShips();
        }
        return false;
    }
    
    /**
     * Verifica el estado final del juego.
     * @return Estado del juego o null si continúa
     */
    public String checkGameEnd() {
        if (gameEnded) {
            return GameConstants.RESULT_WIN + ":" + winner;
        }
        return null;
    }
    
    /**
     * Obtiene el estado actual del juego para sincronización.
     * @return Estado del juego como string
     */
    public String getGameState() {
        StringBuilder state = new StringBuilder();
        state.append("PHASE:").append(gamePhase).append(";");
        state.append("CURRENT_PLAYER:").append(currentPlayer != null ? currentPlayer.getId() : "NONE").append(";");
        state.append("TURNS:").append(totalTurns).append(";");
        state.append("ENDED:").append(gameEnded).append(";");
        if (winner != null) {
            state.append("WINNER:").append(winner).append(";");
        }
        return state.toString();
    }
    
    /**
     * Reinicia el juego para una nueva partida.
     */
    public void resetGame() {
        playerBoard.reset();
        opponentBoard.reset();
        gamePhase = "SETUP";
        gameEnded = false;
        winner = null;
        totalTurns = 0;
        currentPlayer = player1;
    }
    
    /**
     * Obtiene información sobre el último disparo.
     * @param row Fila del disparo
     * @param col Columna del disparo
     * @param playerId ID del jugador que disparó
     * @return Información del disparo
     */
    public String getLastShotInfo(int row, int col, String playerId) {
        Board targetBoard = getOpponentBoard(playerId);
        if (targetBoard != null) {
            char[][] enemyGrid = targetBoard.getOwnGrid();
            char cell = enemyGrid[row][col];
            
            if (cell == GameConstants.BATTLESHIP_HIT) {
                return "¡Tocado!";
            } else if (cell == GameConstants.BATTLESHIP_MISS) {
                return "Agua";
            }
        }
        return "Desconocido";
    }
    
    // Getters
    
    public Board getPlayerBoard() {
        return playerBoard;
    }
    
    public Board getOpponentBoard() {
        return opponentBoard;
    }
    
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    public Player getPlayer1() {
        return player1;
    }
    
    public Player getPlayer2() {
        return player2;
    }
    
    public String getGamePhase() {
        return gamePhase;
    }
    
    public boolean isGameEnded() {
        return gameEnded;
    }
    
    public String getWinner() {
        return winner;
    }
    
    public int getTotalTurns() {
        return totalTurns;
    }
    
    public boolean isSetupPhase() {
        return "SETUP".equals(gamePhase);
    }
    
    public boolean isBattlePhase() {
        return "BATTLE".equals(gamePhase);
    }
    
    public boolean isFinished() {
        return "FINISHED".equals(gamePhase);
    }
    
    @Override
    public String toString() {
        return String.format("BattleshipGame{phase='%s', currentPlayer='%s', turns=%d, ended=%s, winner='%s'}", 
                           gamePhase, 
                           currentPlayer != null ? currentPlayer.getName() : "NONE", 
                           totalTurns, 
                           gameEnded, 
                           winner);
    }
}
