package com.juegos.battleship;

import com.juegos.common.GameConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el tablero de Batalla Naval.
 * Maneja la colocación de barcos, registro de disparos y estado del juego.
 * 
 *
 */
public class Board {
    
    private char[][] grid;           // Tablero principal
    private char[][] enemyView;      // Vista del tablero enemigo (solo disparos)
    private Ship[] ships;            // Barcos en el tablero
    private List<String> shots;      // Registro de disparos realizados
    private List<String> hits;       // Registro de impactos recibidos
    private List<String> misses;     // Registro de fallos recibidos
    private boolean setupComplete;   // Si la configuración de barcos está completa
    
    /**
     * Constructor del tablero.
     */
    public Board() {
        initializeGrid();
        this.ships = Ship.createStandardFleet();
        this.shots = new ArrayList<>();
        this.hits = new ArrayList<>();
        this.misses = new ArrayList<>();
        this.setupComplete = false;
    }
    
    /**
     * Inicializa el tablero vacío.
     */
    private void initializeGrid() {
        int size = GameConstants.BATTLESHIP_BOARD_SIZE;
        grid = new char[size][size];
        enemyView = new char[size][size];
        
        // Llenar con agua
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = GameConstants.BATTLESHIP_WATER;
                enemyView[i][j] = GameConstants.BATTLESHIP_WATER;
            }
        }
    }
    
    /**
     * Intenta colocar un barco en el tablero.
     * @param shipIndex Índice del barco a colocar
     * @param startRow Fila inicial
     * @param startCol Columna inicial
     * @param isHorizontal Orientación del barco
     * @return true si se colocó exitosamente
     */
    public boolean placeShip(int shipIndex, int startRow, int startCol, boolean isHorizontal) {
        if (shipIndex < 0 || shipIndex >= ships.length) {
            return false;
        }
        
        Ship ship = ships[shipIndex];
        
        // Verificar si el barco ya está colocado
        if (ship.isPlaced()) {
            return false;
        }
        
        // Verificar si cabe en el tablero
        if (!ship.fitsInBoard(startRow, startCol, isHorizontal, GameConstants.BATTLESHIP_BOARD_SIZE)) {
            return false;
        }
        
        // Crear copia temporal para verificar superposición
        Ship tempShip = ship.copy();
        tempShip.place(startRow, startCol, isHorizontal);
        
        // Verificar superposición con otros barcos
        for (Ship otherShip : ships) {
            if (otherShip != ship && otherShip.isPlaced() && tempShip.overlapsWith(otherShip)) {
                return false;
            }
        }
        
        // Colocar el barco
        ship.place(startRow, startCol, isHorizontal);
        updateGridWithShip(ship);
        
        // Verificar si se completó la configuración
        checkSetupComplete();
        
        return true;
    }
    
    /**
     * Actualiza el tablero con la posición de un barco.
     * @param ship Barco a colocar en el grid
     */
    private void updateGridWithShip(Ship ship) {
        String[] positions = ship.getPositions();
        for (String pos : positions) {
            String[] coords = pos.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);
            grid[row][col] = GameConstants.BATTLESHIP_SHIP;
        }
    }
    
    /**
     * Remueve un barco del tablero.
     * @param shipIndex Índice del barco a remover
     * @return true si se removió exitosamente
     */
    public boolean removeShip(int shipIndex) {
        if (shipIndex < 0 || shipIndex >= ships.length) {
            return false;
        }
        
        Ship ship = ships[shipIndex];
        if (!ship.isPlaced()) {
            return false;
        }
        
        // Limpiar posiciones del grid
        String[] positions = ship.getPositions();
        for (String pos : positions) {
            String[] coords = pos.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);
            grid[row][col] = GameConstants.BATTLESHIP_WATER;
        }
        
        // Remover el barco
        ship.remove();
        setupComplete = false;
        
        return true;
    }
    
    /**
     * Procesa un disparo en el tablero.
     * @param row Fila del disparo
     * @param col Columna del disparo
     * @return Resultado del disparo ("HIT", "MISS", "SUNK", "INVALID")
     */
    public String receiveShot(int row, int col) {
        // Validar coordenadas
        if (row < 0 || row >= GameConstants.BATTLESHIP_BOARD_SIZE || 
            col < 0 || col >= GameConstants.BATTLESHIP_BOARD_SIZE) {
            return "INVALID";
        }
        
        String position = row + "," + col;
        
        // Verificar si ya se disparó en esta posición
        if (hits.contains(position) || misses.contains(position)) {
            return "INVALID";
        }
        
        // Verificar si hay un barco
        boolean hitShip = false;
        Ship hitShipRef = null;
        
        for (Ship ship : ships) {
            if (ship.isPlaced() && ship.occupiesPosition(row, col)) {
                ship.hit(row, col);
                hitShip = true;
                hitShipRef = ship;
                break;
            }
        }
        
        if (hitShip) {
            hits.add(position);
            grid[row][col] = GameConstants.BATTLESHIP_HIT;
            
            // Verificar si el barco se hundió
            if (hitShipRef.isSunk()) {
                return "SUNK:" + hitShipRef.getName();
            } else {
                return "HIT";
            }
        } else {
            misses.add(position);
            grid[row][col] = GameConstants.BATTLESHIP_MISS;
            return "MISS";
        }
    }
    
    /**
     * Realiza un disparo al tablero enemigo.
     * @param row Fila del disparo
     * @param col Columna del disparo
     * @param result Resultado del disparo
     */
    public void recordShot(int row, int col, String result) {
        String position = row + "," + col;
        shots.add(position);
        
        if (result.startsWith("HIT") || result.startsWith("SUNK")) {
            enemyView[row][col] = GameConstants.BATTLESHIP_HIT;
        } else if ("MISS".equals(result)) {
            enemyView[row][col] = GameConstants.BATTLESHIP_MISS;
        }
    }
    
    /**
     * Verifica si todos los barcos están hundidos.
     * @return true si todos los barcos están hundidos
     */
    public boolean allShipsSunk() {
        for (Ship ship : ships) {
            if (ship.isPlaced() && !ship.isSunk()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Verifica si la configuración de barcos está completa.
     */
    private void checkSetupComplete() {
        for (Ship ship : ships) {
            if (!ship.isPlaced()) {
                setupComplete = false;
                return;
            }
        }
        setupComplete = true;
    }
    
    /**
     * Coloca los barcos automáticamente de forma aleatoria.
     * @return true si se colocaron todos los barcos
     */
    public boolean autoPlaceShips() {
        // Limpiar barcos existentes
        for (int i = 0; i < ships.length; i++) {
            if (ships[i].isPlaced()) {
                removeShip(i);
            }
        }
        
        // Intentar colocar cada barco
        for (int i = 0; i < ships.length; i++) {
            boolean placed = false;
            int attempts = 0;
            
            while (!placed && attempts < 100) { // Máximo 100 intentos por barco
                int row = (int) (Math.random() * GameConstants.BATTLESHIP_BOARD_SIZE);
                int col = (int) (Math.random() * GameConstants.BATTLESHIP_BOARD_SIZE);
                boolean horizontal = Math.random() < 0.5;
                
                if (placeShip(i, row, col, horizontal)) {
                    placed = true;
                }
                attempts++;
            }
            
            if (!placed) {
                return false; // No se pudo colocar el barco
            }
        }
        
        return true;
    }
    
    /**
     * Obtiene las posiciones válidas para disparar.
     * @return Lista de posiciones no atacadas
     */
    public List<String> getValidTargets() {
        List<String> targets = new ArrayList<>();
        
        for (int row = 0; row < GameConstants.BATTLESHIP_BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.BATTLESHIP_BOARD_SIZE; col++) {
                String position = row + "," + col;
                if (!shots.contains(position)) {
                    targets.add(position);
                }
            }
        }
        
        return targets;
    }
    
    /**
     * Obtiene una representación del tablero para el jugador.
     * @return Grid del tablero propio
     */
    public char[][] getOwnGrid() {
        return copyGrid(grid);
    }
    
    /**
     * Obtiene una representación del tablero enemigo.
     * @return Grid de la vista enemiga
     */
    public char[][] getEnemyGrid() {
        return copyGrid(enemyView);
    }
    
    /**
     * Crea una copia del grid.
     * @param source Grid fuente
     * @return Copia del grid
     */
    private char[][] copyGrid(char[][] source) {
        int size = GameConstants.BATTLESHIP_BOARD_SIZE;
        char[][] copy = new char[size][size];
        
        for (int i = 0; i < size; i++) {
            System.arraycopy(source[i], 0, copy[i], 0, size);
        }
        
        return copy;
    }
    
    /**
     * Reinicia el tablero para una nueva partida.
     */
    public void reset() {
        initializeGrid();
        
        // Recrear barcos
        ships = Ship.createStandardFleet();
        
        // Limpiar listas
        shots.clear();
        hits.clear();
        misses.clear();
        
        setupComplete = false;
    }
    
    // Getters
    
    public Ship[] getShips() {
        return ships.clone();
    }
    
    public Ship getShip(int index) {
        if (index >= 0 && index < ships.length) {
            return ships[index];
        }
        return null;
    }
    
    public boolean isSetupComplete() {
        return setupComplete;
    }
    
    public List<String> getShots() {
        return new ArrayList<>(shots);
    }
    
    public List<String> getHits() {
        return new ArrayList<>(hits);
    }
    
    public List<String> getMisses() {
        return new ArrayList<>(misses);
    }
    
    public int getShipsRemaining() {
        int count = 0;
        for (Ship ship : ships) {
            if (ship.isPlaced() && !ship.isSunk()) {
                count++;
            }
        }
        return count;
    }
    
    public int getTotalHits() {
        return hits.size();
    }
    
    public int getTotalShots() {
        return shots.size();
    }
    
    public double getAccuracy() {
        if (shots.isEmpty()) return 0.0;
        return (double) hits.size() / shots.size() * 100.0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Board Status:\n");
        sb.append("Setup Complete: ").append(setupComplete).append("\n");
        sb.append("Ships Remaining: ").append(getShipsRemaining()).append("/").append(ships.length).append("\n");
        sb.append("Total Shots: ").append(getTotalShots()).append("\n");
        sb.append("Accuracy: ").append(String.format("%.1f%%", getAccuracy())).append("\n");
        
        return sb.toString();
    }
}
