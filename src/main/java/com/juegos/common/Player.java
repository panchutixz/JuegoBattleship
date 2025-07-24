package com.juegos.common;

import java.util.UUID;

/**
 * Representa un jugador en el sistema de juegos.
 * Contiene información básica del jugador y su estado.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0
 */
public class Player {
    
    private String id;              // ID único del jugador
    private String name;            // Nombre del jugador
    private String symbol;          // Símbolo del jugador (X, O para Tic-Tac-Toe)
    private boolean isHuman;        // true si es humano, false si es IA
    private boolean isConnected;    // Estado de conexión
    private String ipAddress;       // Dirección IP del cliente
    private int gamesWon;          // Partidas ganadas
    private int gamesLost;         // Partidas perdidas
    private int gamesDrawn;        // Partidas empatadas
    
    /**
     * Constructor para jugador humano.
     * @param name Nombre del jugador
     */
    public Player(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.isHuman = true;
        this.isConnected = false;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.gamesDrawn = 0;
    }
    
    /**
     * Constructor para jugador con ID específico.
     * @param id ID del jugador
     * @param name Nombre del jugador
     */
    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.isHuman = true;
        this.isConnected = false;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.gamesDrawn = 0;
    }
    
    /**
     * Constructor para IA.
     * @param name Nombre de la IA
     * @param isHuman false para IA
     */
    public Player(String name, boolean isHuman) {
        this.id = isHuman ? UUID.randomUUID().toString() : "AI_" + System.currentTimeMillis();
        this.name = name;
        this.isHuman = isHuman;
        this.isConnected = !isHuman; // IA siempre está "conectada"
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.gamesDrawn = 0;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public boolean isHuman() {
        return isHuman;
    }
    
    public void setHuman(boolean human) {
        isHuman = human;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public void setConnected(boolean connected) {
        isConnected = connected;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public int getGamesWon() {
        return gamesWon;
    }
    
    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }
    
    public int getGamesLost() {
        return gamesLost;
    }
    
    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }
    
    public int getGamesDrawn() {
        return gamesDrawn;
    }
    
    public void setGamesDrawn(int gamesDrawn) {
        this.gamesDrawn = gamesDrawn;
    }
    
    /**
     * Incrementa las partidas ganadas.
     */
    public void incrementWins() {
        this.gamesWon++;
    }
    
    /**
     * Incrementa las partidas perdidas.
     */
    public void incrementLosses() {
        this.gamesLost++;
    }
    
    /**
     * Incrementa las partidas empatadas.
     */
    public void incrementDraws() {
        this.gamesDrawn++;
    }
    
    /**
     * Obtiene el total de partidas jugadas.
     * @return Total de partidas
     */
    public int getTotalGames() {
        return gamesWon + gamesLost + gamesDrawn;
    }
    
    /**
     * Calcula el porcentaje de victorias.
     * @return Porcentaje de victorias (0-100)
     */
    public double getWinPercentage() {
        int total = getTotalGames();
        if (total == 0) return 0.0;
        return (double) gamesWon / total * 100.0;
    }
    
    /**
     * Resetea las estadísticas del jugador.
     */
    public void resetStats() {
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.gamesDrawn = 0;
    }
    
    /**
     * Crea un jugador IA con nombre por defecto.
     * @return Jugador IA
     */
    public static Player createAI() {
        return new Player("IA", false);
    }
    
    /**
     * Crea un jugador IA con nombre específico.
     * @param aiName Nombre de la IA
     * @return Jugador IA
     */
    public static Player createAI(String aiName) {
        return new Player(aiName, false);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return id.equals(player.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Player{id='%s', name='%s', symbol='%s', isHuman=%s, connected=%s}", 
                           id, name, symbol, isHuman, isConnected);
    }
}
