package com.juegos.battleship;

import com.juegos.common.GameConstants;
import java.util.*;

/**
 * Inteligencia Artificial para el juego Batalla Naval.
 * Implementa estrategias inteligentes de ataque usando algoritmos de búsqueda
 * y análisis de patrones para ser un oponente desafiante.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0
 */
public class BattleshipAI {
    
    private final Board enemyBoard;           // Referencia al tablero enemigo para atacar
    private final Board ownBoard;             // Tablero propio de la IA
    private final List<String> targetQueue;   // Cola de objetivos prioritarios
    private final Set<String> processedHits;  // Impactos ya procesados
    private final Random random;              // Generador de números aleatorios
    
    // Estados de la IA
    private AIMode currentMode;
    private String lastHit;                   // Último impacto exitoso
    private boolean huntingMode;              // true si está cazando un barco
    private String huntDirection;             // Dirección de caza actual
    private int consecutiveHits;              // Impactos consecutivos en la misma dirección
    
    // Estrategias de disparo
    private static final String[] DIRECTIONS = {"NORTH", "SOUTH", "EAST", "WEST"};
    private static final int[][] DIRECTION_VECTORS = {{-1,0}, {1,0}, {0,1}, {0,-1}};
    
    /**
     * Modos de operación de la IA
     */
    public enum AIMode {
        HUNT,      // Búsqueda aleatoria de barcos
        TARGET,    // Ataque dirigido después de un impacto
        FINISH     // Terminando de hundir un barco encontrado
    }
    
    /**
     * Constructor de la IA de Batalla Naval.
     * @param ownBoard Tablero propio de la IA
     * @param enemyBoard Tablero enemigo para atacar
     */
    public BattleshipAI(Board ownBoard, Board enemyBoard) {
        this.ownBoard = ownBoard;
        this.enemyBoard = enemyBoard;
        this.targetQueue = new ArrayList<>();
        this.processedHits = new HashSet<>();
        this.random = new Random();
        this.currentMode = AIMode.HUNT;
        this.huntingMode = false;
        this.consecutiveHits = 0;
        
        // Configurar automáticamente los barcos de la IA
        setupAIShips();
    }
    
    /**
     * Configura automáticamente los barcos de la IA de forma inteligente.
     */
    private void setupAIShips() {
        // Usar backtracking para colocar barcos de manera óptima
        if (!placeShipsWithBacktracking()) {
            // Si falla el backtracking, usar colocación aleatoria como respaldo
            ownBoard.autoPlaceShips();
        }
    }
    
    /**
     * Coloca los barcos usando backtracking para optimizar su distribución.
     * @return true si se colocaron todos los barcos exitosamente
     */
    private boolean placeShipsWithBacktracking() {
        Ship[] ships = ownBoard.getShips();
        return backtrackPlaceShips(ships, 0);
    }
    
    /**
     * Algoritmo de backtracking para colocar barcos.
     * @param ships Array de barcos a colocar
     * @param shipIndex Índice del barco actual
     * @return true si se pueden colocar todos los barcos restantes
     */
    private boolean backtrackPlaceShips(Ship[] ships, int shipIndex) {
        if (shipIndex >= ships.length) {
            return true; // Todos los barcos colocados
        }
        
        List<int[]> positions = generateOptimalPositions();
        
        for (int[] pos : positions) {
            int row = pos[0];
            int col = pos[1];
            boolean horizontal = pos[2] == 1;
            
            if (ownBoard.placeShip(shipIndex, row, col, horizontal)) {
                // Colocación exitosa, intentar siguiente barco
                if (backtrackPlaceShips(ships, shipIndex + 1)) {
                    return true;
                }
                // Backtrack: remover barco y probar siguiente posición
                ownBoard.removeShip(shipIndex);
            }
        }
        
        return false; // No se pudo colocar este barco
    }
    
    /**
     * Genera posiciones óptimas para colocar barcos, priorizando distribución estratégica.
     * @return Lista de posiciones [row, col, horizontal] ordenadas por prioridad
     */
    private List<int[]> generateOptimalPositions() {
        List<int[]> positions = new ArrayList<>();
        
        // Generar todas las posiciones posibles
        for (int row = 0; row < GameConstants.BATTLESHIP_BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.BATTLESHIP_BOARD_SIZE; col++) {
                positions.add(new int[]{row, col, 1}); // Horizontal
                positions.add(new int[]{row, col, 0}); // Vertical
            }
        }
        
        // Barajar para agregar aleatoriedad
        Collections.shuffle(positions, random);
        
        return positions;
    }
    
    /**
     * Calcula el mejor movimiento de ataque para la IA.
     * @return String con las coordenadas del ataque en formato "row,col"
     */
    public String getBestAttack() {
        updateAIMode();
        
        String attack = null;
        
        switch (currentMode) {
            case HUNT:
                attack = executeHuntStrategy();
                break;
            case TARGET:
                attack = executeTargetStrategy();
                break;
            case FINISH:
                attack = executeFinishStrategy();
                break;
        }
        
        if (attack == null) {
            attack = getRandomValidAttack();
        }
        
        return attack;
    }
    
    /**
     * Actualiza el modo de operación de la IA basado en el estado actual.
     */
    private void updateAIMode() {
        if (!targetQueue.isEmpty()) {
            currentMode = huntingMode ? AIMode.FINISH : AIMode.TARGET;
        } else {
            currentMode = AIMode.HUNT;
            huntingMode = false;
            huntDirection = null;
            consecutiveHits = 0;
        }
    }
    
    /**
     * Ejecuta la estrategia de caza (búsqueda inicial de barcos).
     * @return Coordenadas del ataque
     */
    private String executeHuntStrategy() {
        // Usar patrón de tablero de ajedrez para maximizar probabilidad de impacto
        List<String> validTargets = enemyBoard.getValidTargets();
        List<String> checkerboardTargets = new ArrayList<>();
        
        for (String target : validTargets) {
            String[] coords = target.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);
            
            // Patrón de tablero de ajedrez: (row + col) % 2 == 0
            if ((row + col) % 2 == 0) {
                checkerboardTargets.add(target);
            }
        }
        
        if (!checkerboardTargets.isEmpty()) {
            return checkerboardTargets.get(random.nextInt(checkerboardTargets.size()));
        }
        
        return getRandomValidAttack();
    }
    
    /**
     * Ejecuta la estrategia de objetivo (ataque dirigido después de un impacto).
     * @return Coordenadas del ataque
     */
    private String executeTargetStrategy() {
        if (targetQueue.isEmpty()) {
            return executeHuntStrategy();
        }
        
        // Tomar el próximo objetivo de la cola
        String target = targetQueue.remove(0);
        
        // Si este objetivo ya fue procesado, buscar el siguiente
        if (processedHits.contains(target)) {
            return executeTargetStrategy();
        }
        
        return target;
    }
    
    /**
     * Ejecuta la estrategia de finalización (completar el hundimiento de un barco).
     * @return Coordenadas del ataque
     */
    private String executeFinishStrategy() {
        if (lastHit == null || huntDirection == null) {
            return executeTargetStrategy();
        }
        
        String[] coords = lastHit.split(",");
        int row = Integer.parseInt(coords[0]);
        int col = Integer.parseInt(coords[1]);
        
        // Continuar en la dirección actual
        String nextTarget = getNextPositionInDirection(row, col, huntDirection);
        
        if (nextTarget != null && isValidTarget(nextTarget)) {
            return nextTarget;
        }
        
        // Si no se puede continuar en esta dirección, buscar otra
        huntDirection = null;
        return executeTargetStrategy();
    }
    
    /**
     * Procesa el resultado de un ataque y actualiza la estrategia de la IA.
     * @param row Fila del ataque
     * @param col Columna del ataque
     * @param result Resultado del ataque ("HIT", "MISS", "SUNK:NombreBarco")
     */
    public void processAttackResult(int row, int col, String result) {
        if (result.startsWith("HIT")) {
            handleHit(row, col);
        } else if (result.startsWith("SUNK")) {
            handleSunk(row, col);
        } else if ("MISS".equals(result)) {
            handleMiss(row, col);
        }
    }
    
    /**
     * Maneja el resultado de un impacto.
     * @param row Fila del impacto
     * @param col Columna del impacto
     */
    private void handleHit(int row, int col) {
        String position = row + "," + col;
        lastHit = position;
        processedHits.add(position);
        
        if (!huntingMode) {
            // Primer impacto: agregar posiciones adyacentes a la cola
            addAdjacentPositions(row, col);
            huntingMode = true;
        } else {
            // Impacto consecutivo: determinar dirección
            if (huntDirection != null) {
                consecutiveHits++;
                // Continuar en la misma dirección
                String nextPos = getNextPositionInDirection(row, col, huntDirection);
                if (nextPos != null && isValidTarget(nextPos)) {
                    targetQueue.add(0, nextPos); // Prioridad alta
                }
            } else {
                // Determinar dirección basada en impactos anteriores
                huntDirection = determineDirection(row, col);
                if (huntDirection != null) {
                    consecutiveHits = 1;
                }
            }
        }
    }
    
    /**
     * Maneja el resultado de hundir un barco.
     * @param row Fila del último impacto
     * @param col Columna del último impacto
     */
    private void handleSunk(int row, int col) {
        String position = row + "," + col;
        processedHits.add(position);
        
        // Limpiar objetivos relacionados con este barco
        cleanupTargetQueue();
        
        // Resetear modo de caza
        huntingMode = false;
        huntDirection = null;
        consecutiveHits = 0;
        lastHit = null;
        currentMode = AIMode.HUNT;
    }
    
    /**
     * Maneja el resultado de un fallo.
     * @param row Fila del fallo
     * @param col Columna del fallo
     */
    private void handleMiss(int row, int col) {
        if (huntingMode && huntDirection != null) {
            // Cambiar dirección o buscar otra estrategia
            huntDirection = getAlternativeDirection();
            if (huntDirection == null) {
                huntingMode = false;
            }
        }
    }
    
    /**
     * Agrega posiciones adyacentes a la cola de objetivos.
     * @param row Fila central
     * @param col Columna central
     */
    private void addAdjacentPositions(int row, int col) {
        for (int[] vector : DIRECTION_VECTORS) {
            int newRow = row + vector[0];
            int newCol = col + vector[1];
            String position = newRow + "," + newCol;
            
            if (isValidTarget(position)) {
                targetQueue.add(position);
            }
        }
        
        // Barajar para agregar aleatoriedad
        Collections.shuffle(targetQueue, random);
    }
    
    /**
     * Determina la dirección de un barco basado en impactos previos.
     * @param row Fila actual
     * @param col Columna actual
     * @return Dirección detectada o null
     */
    private String determineDirection(int row, int col) {
        // Buscar impactos adyacentes en procesedHits
        for (int i = 0; i < DIRECTIONS.length; i++) {
            String direction = DIRECTIONS[i];
            int[] vector = DIRECTION_VECTORS[i];
            String adjacentPos = (row + vector[0]) + "," + (col + vector[1]);
            
            if (processedHits.contains(adjacentPos)) {
                return direction;
            }
        }
        return null;
    }
    
    /**
     * Obtiene la siguiente posición en una dirección específica.
     * @param row Fila actual
     * @param col Columna actual
     * @param direction Dirección a seguir
     * @return Posición siguiente o null si es inválida
     */
    private String getNextPositionInDirection(int row, int col, String direction) {
        int vectorIndex = Arrays.asList(DIRECTIONS).indexOf(direction);
        if (vectorIndex == -1) return null;
        
        int[] vector = DIRECTION_VECTORS[vectorIndex];
        int newRow = row + vector[0];
        int newCol = col + vector[1];
        
        return newRow + "," + newCol;
    }
    
    /**
     * Obtiene una dirección alternativa cuando la actual falla.
     * @return Nueva dirección o null
     */
    private String getAlternativeDirection() {
        // Implementar lógica para encontrar direcciones alternativas
        List<String> availableDirections = new ArrayList<>(Arrays.asList(DIRECTIONS));
        if (huntDirection != null) {
            availableDirections.remove(huntDirection);
        }
        
        if (!availableDirections.isEmpty()) {
            return availableDirections.get(random.nextInt(availableDirections.size()));
        }
        
        return null;
    }
    
    /**
     * Limpia la cola de objetivos removiendo posiciones inválidas.
     */
    private void cleanupTargetQueue() {
        targetQueue.removeIf(target -> !isValidTarget(target) || processedHits.contains(target));
    }
    
    /**
     * Verifica si una posición es un objetivo válido.
     * @param position Posición en formato "row,col"
     * @return true si es válida
     */
    private boolean isValidTarget(String position) {
        try {
            String[] coords = position.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);
            
            // Verificar límites del tablero
            if (row < 0 || row >= GameConstants.BATTLESHIP_BOARD_SIZE ||
                col < 0 || col >= GameConstants.BATTLESHIP_BOARD_SIZE) {
                return false;
            }
            
            // Verificar que no haya sido atacada antes
            List<String> validTargets = enemyBoard.getValidTargets();
            return validTargets.contains(position);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Obtiene un ataque aleatorio válido como respaldo.
     * @return Posición aleatoria válida
     */
    private String getRandomValidAttack() {
        List<String> validTargets = enemyBoard.getValidTargets();
        if (!validTargets.isEmpty()) {
            return validTargets.get(random.nextInt(validTargets.size()));
        }
        return "0,0"; // Fallback (no debería ocurrir)
    }
    
    /**
     * Obtiene estadísticas de rendimiento de la IA.
     * @return Mapa con estadísticas
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("currentMode", currentMode.toString());
        stats.put("huntingMode", huntingMode);
        stats.put("targetQueueSize", targetQueue.size());
        stats.put("processedHits", processedHits.size());
        stats.put("consecutiveHits", consecutiveHits);
        stats.put("currentDirection", huntDirection);
        
        return stats;
    }
    
    /**
     * Reinicia el estado de la IA para una nueva partida.
     */
    public void reset() {
        targetQueue.clear();
        processedHits.clear();
        currentMode = AIMode.HUNT;
        lastHit = null;
        huntingMode = false;
        huntDirection = null;
        consecutiveHits = 0;
        
        // Reconfigurar barcos
        ownBoard.reset();
        setupAIShips();
    }
    
    // Getters
    
    public AIMode getCurrentMode() {
        return currentMode;
    }
    
    public boolean isHuntingMode() {
        return huntingMode;
    }
    
    public int getTargetQueueSize() {
        return targetQueue.size();
    }
    
    public String getHuntDirection() {
        return huntDirection;
    }
    
    public int getConsecutiveHits() {
        return consecutiveHits;
    }
}
