package com.juegos.battleship;

import com.juegos.common.GameConstants;

/**
 * Representa un barco en el juego de Batalla Naval.
 * Maneja la posición, orientación y estado del barco.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0
 */
public class Ship {
    
    private final int size;          // Tamaño del barco
    private final String name;       // Nombre del barco
    private int startRow;            // Fila inicial
    private int startCol;            // Columna inicial
    private boolean isHorizontal;    // true = horizontal, false = vertical
    private boolean[] hits;          // Partes del barco que han sido tocadas
    private boolean isPlaced;        // Si el barco está colocado en el tablero
    
    /**
     * Constructor del barco.
     * @param size Tamaño del barco
     * @param name Nombre del barco
     */
    public Ship(int size, String name) {
        this.size = size;
        this.name = name;
        this.hits = new boolean[size];
        this.isPlaced = false;
        this.isHorizontal = true; // Por defecto horizontal
        
        // Inicializar hits como false
        for (int i = 0; i < size; i++) {
            hits[i] = false;
        }
    }
    
    /**
     * Coloca el barco en el tablero.
     * @param startRow Fila inicial
     * @param startCol Columna inicial
     * @param isHorizontal Orientación del barco
     */
    public void place(int startRow, int startCol, boolean isHorizontal) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.isHorizontal = isHorizontal;
        this.isPlaced = true;
    }
    
    /**
     * Verifica si el barco ocupa una posición específica.
     * @param row Fila a verificar
     * @param col Columna a verificar
     * @return true si el barco ocupa esa posición
     */
    public boolean occupiesPosition(int row, int col) {
        if (!isPlaced) {
            return false;
        }
        
        if (isHorizontal) {
            return row == startRow && col >= startCol && col < startCol + size;
        } else {
            return col == startCol && row >= startRow && row < startRow + size;
        }
    }
    
    /**
     * Registra un impacto en el barco.
     * @param row Fila del impacto
     * @param col Columna del impacto
     * @return true si el impacto fue exitoso
     */
    public boolean hit(int row, int col) {
        if (!occupiesPosition(row, col)) {
            return false;
        }
        
        int hitIndex;
        if (isHorizontal) {
            hitIndex = col - startCol;
        } else {
            hitIndex = row - startRow;
        }
        
        if (hitIndex >= 0 && hitIndex < size) {
            hits[hitIndex] = true;
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica si el barco está completamente hundido.
     * @return true si está hundido
     */
    public boolean isSunk() {
        for (boolean hit : hits) {
            if (!hit) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Obtiene todas las posiciones que ocupa el barco.
     * @return Array de posiciones en formato "row,col"
     */
    public String[] getPositions() {
        if (!isPlaced) {
            return new String[0];
        }
        
        String[] positions = new String[size];
        for (int i = 0; i < size; i++) {
            if (isHorizontal) {
                positions[i] = startRow + "," + (startCol + i);
            } else {
                positions[i] = (startRow + i) + "," + startCol;
            }
        }
        return positions;
    }
    
    /**
     * Verifica si el barco cabe en el tablero con la posición y orientación dadas.
     * @param startRow Fila inicial
     * @param startCol Columna inicial
     * @param isHorizontal Orientación
     * @param boardSize Tamaño del tablero
     * @return true si cabe
     */
    public boolean fitsInBoard(int startRow, int startCol, boolean isHorizontal, int boardSize) {
        if (isHorizontal) {
            return startRow >= 0 && startRow < boardSize && 
                   startCol >= 0 && startCol + size <= boardSize;
        } else {
            return startCol >= 0 && startCol < boardSize && 
                   startRow >= 0 && startRow + size <= boardSize;
        }
    }
    
    /**
     * Verifica si el barco se superpone con otro.
     * @param otherShip Otro barco a verificar
     * @return true si se superponen
     */
    public boolean overlapsWith(Ship otherShip) {
        if (!this.isPlaced || !otherShip.isPlaced) {
            return false;
        }
        
        String[] myPositions = this.getPositions();
        String[] otherPositions = otherShip.getPositions();
        
        for (String myPos : myPositions) {
            for (String otherPos : otherPositions) {
                if (myPos.equals(otherPos)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Remueve el barco del tablero.
     */
    public void remove() {
        this.isPlaced = false;
        // Resetear hits
        for (int i = 0; i < size; i++) {
            hits[i] = false;
        }
    }
    
    /**
     * Rota el barco (cambia orientación).
     */
    public void rotate() {
        this.isHorizontal = !this.isHorizontal;
    }
    
    /**
     * Crea una copia del barco.
     * @return Nueva instancia del barco con las mismas propiedades
     */
    public Ship copy() {
        Ship copy = new Ship(this.size, this.name);
        if (this.isPlaced) {
            copy.place(this.startRow, this.startCol, this.isHorizontal);
        }
        
        // Copiar estado de hits
        System.arraycopy(this.hits, 0, copy.hits, 0, this.size);
        
        return copy;
    }
    
    /**
     * Crea los barcos estándar de Batalla Naval.
     * @return Array con todos los barcos
     */
    public static Ship[] createStandardFleet() {
        Ship[] fleet = new Ship[GameConstants.SHIP_SIZES.length];
        
        for (int i = 0; i < GameConstants.SHIP_SIZES.length; i++) {
            fleet[i] = new Ship(GameConstants.SHIP_SIZES[i], GameConstants.SHIP_NAMES[i]);
        }
        
        return fleet;
    }
    
    // Getters y Setters
    
    public int getSize() {
        return size;
    }
    
    public String getName() {
        return name;
    }
    
    public int getStartRow() {
        return startRow;
    }
    
    public int getStartCol() {
        return startCol;
    }
    
    public boolean isHorizontal() {
        return isHorizontal;
    }
    
    public void setHorizontal(boolean horizontal) {
        isHorizontal = horizontal;
    }
    
    public boolean isPlaced() {
        return isPlaced;
    }
    
    public boolean[] getHits() {
        return hits.clone();
    }
    
    public int getHitCount() {
        int count = 0;
        for (boolean hit : hits) {
            if (hit) count++;
        }
        return count;
    }
    
    public int getRemainingHealth() {
        return size - getHitCount();
    }
    
    @Override
    public String toString() {
        return String.format("Ship{name='%s', size=%d, placed=%s, sunk=%s, hits=%d/%d}", 
                           name, size, isPlaced, isSunk(), getHitCount(), size);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Ship ship = (Ship) obj;
        return size == ship.size && name.equals(ship.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode() + size * 31;
    }
}
