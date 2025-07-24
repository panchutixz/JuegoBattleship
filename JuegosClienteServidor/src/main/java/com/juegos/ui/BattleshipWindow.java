package com.juegos.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * Ventana del juego Battleship con interfaz Swing
 */
public class BattleshipWindow extends JFrame {
    
    private JButton[][] playerBoard;
    private JButton[][] enemyBoard;
    private JLabel statusLabel;
    private JLabel hitsLabel;
    private JLabel missesLabel;
    private boolean[][] playerShips;
    private boolean[][] enemyShips;
    private boolean[][] playerHits;
    private boolean isPlayerTurn;
    private int playerHitCount;
    private int enemyHitCount;
    private int playerMissCount;
    
    private static final int BOARD_SIZE = 8;
    private static final int SHIPS_TO_SINK = 10;
    
    public BattleshipWindow() {
        this.isPlayerTurn = true;
        this.playerHitCount = 0;
        this.enemyHitCount = 0;
        this.playerMissCount = 0;
        
        initializeArrays();
        placeShipsRandomly();
        initializeGUI();
    }
    
    private void initializeArrays() {
        playerBoard = new JButton[BOARD_SIZE][BOARD_SIZE];
        enemyBoard = new JButton[BOARD_SIZE][BOARD_SIZE];
        playerShips = new boolean[BOARD_SIZE][BOARD_SIZE];
        enemyShips = new boolean[BOARD_SIZE][BOARD_SIZE];
        playerHits = new boolean[BOARD_SIZE][BOARD_SIZE];
    }
    
    private void placeShipsRandomly() {
        Random random = new Random();
        
        // Colocar barcos del jugador
        int playerShipsPlaced = 0;
        while (playerShipsPlaced < SHIPS_TO_SINK) {
            int row = random.nextInt(BOARD_SIZE);
            int col = random.nextInt(BOARD_SIZE);
            if (!playerShips[row][col]) {
                playerShips[row][col] = true;
                playerShipsPlaced++;
            }
        }
        
        // Colocar barcos del enemigo
        int enemyShipsPlaced = 0;
        while (enemyShipsPlaced < SHIPS_TO_SINK) {
            int row = random.nextInt(BOARD_SIZE);
            int col = random.nextInt(BOARD_SIZE);
            if (!enemyShips[row][col]) {
                enemyShips[row][col] = true;
                enemyShipsPlaced++;
            }
        }
    }
    
    private void initializeGUI() {
        setTitle("Battleship - Batalla Naval");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel de información
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Panel de juego (ambos tableros)
        JPanel gamePanel = createGamePanel();
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        
        // Panel de controles
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3));
        panel.setBorder(BorderFactory.createTitledBorder("Estado del Juego"));
        
        statusLabel = new JLabel("Tu turno - Ataca el tablero enemigo", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(statusLabel);
        
        hitsLabel = new JLabel("Aciertos: 0/" + SHIPS_TO_SINK, JLabel.CENTER);
        hitsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(hitsLabel);
        
        missesLabel = new JLabel("Fallos: 0", JLabel.CENTER);
        missesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(missesLabel);
        
        return panel;
    }
    
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Tablero del jugador
        JPanel playerPanel = createPlayerBoard();
        panel.add(playerPanel);
        
        // Tablero del enemigo
        JPanel enemyPanel = createEnemyBoard();
        panel.add(enemyPanel);
        
        return panel;
    }
    
    private JPanel createPlayerBoard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tu Flota"));
        
        JPanel boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE, 1, 1));
        boardPanel.setBackground(Color.BLACK);
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                playerBoard[i][j] = new JButton("");
                playerBoard[i][j].setPreferredSize(new Dimension(40, 40));
                playerBoard[i][j].setEnabled(false);
                
                if (playerShips[i][j]) {
                    playerBoard[i][j].setBackground(Color.BLUE);
                    playerBoard[i][j].setToolTipText("Tu barco");
                } else {
                    playerBoard[i][j].setBackground(Color.CYAN);
                }
                
                boardPanel.add(playerBoard[i][j]);
            }
        }
        
        panel.add(boardPanel, BorderLayout.CENTER);
        
        JLabel instruction = new JLabel("Tus barcos (azul)", JLabel.CENTER);
        instruction.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(instruction, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createEnemyBoard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tablero Enemigo"));
        
        JPanel boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE, 1, 1));
        boardPanel.setBackground(Color.BLACK);
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                final int row = i;
                final int col = j;
                
                enemyBoard[i][j] = new JButton("");
                enemyBoard[i][j].setPreferredSize(new Dimension(40, 40));
                enemyBoard[i][j].setBackground(Color.GRAY);
                
                enemyBoard[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        attackEnemy(row, col);
                    }
                });
                
                boardPanel.add(enemyBoard[i][j]);
            }
        }
        
        panel.add(boardPanel, BorderLayout.CENTER);
        
        JLabel instruction = new JLabel("Haz clic para atacar", JLabel.CENTER);
        instruction.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(instruction, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton newGameButton = new JButton("Nuevo Juego");
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        
        JButton showShipsButton = new JButton("Mostrar Barcos Enemigos");
        showShipsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showEnemyShips();
            }
        });
        
        JButton exitButton = new JButton("Salir");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        panel.add(newGameButton);
        panel.add(showShipsButton);
        panel.add(exitButton);
        
        return panel;
    }
    
    private void attackEnemy(int row, int col) {
        if (!isPlayerTurn) {
            statusLabel.setText("¡Espera tu turno!");
            return;
        }
        
        if (enemyBoard[row][col].getBackground() == Color.RED || 
            enemyBoard[row][col].getBackground() == Color.WHITE) {
            statusLabel.setText("¡Ya atacaste esa posición!");
            return;
        }
        
        enemyBoard[row][col].setEnabled(false);
        
        if (enemyShips[row][col]) {
            // ¡Acierto!
            enemyBoard[row][col].setBackground(Color.RED);
            enemyBoard[row][col].setText("X");
            enemyBoard[row][col].setForeground(Color.WHITE);
            playerHitCount++;
            
            statusLabel.setText("¡Acierto! Hundiste un barco enemigo.");
            
            if (playerHitCount >= SHIPS_TO_SINK) {
                statusLabel.setText("¡FELICIDADES! ¡Has ganado!");
                disableAllEnemyButtons();
                return;
            }
        } else {
            // Fallo
            enemyBoard[row][col].setBackground(Color.WHITE);
            enemyBoard[row][col].setText("O");
            enemyBoard[row][col].setForeground(Color.BLUE);
            playerMissCount++;
            
            statusLabel.setText("Agua... turno del enemigo.");
        }
        
        updateLabels();
        
        // Cambiar turno
        isPlayerTurn = false;
        
        // Simular ataque enemigo
        Timer timer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enemyAttack();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void enemyAttack() {
        Random random = new Random();
        int row, col;
        
        // Buscar una posición no atacada
        do {
            row = random.nextInt(BOARD_SIZE);
            col = random.nextInt(BOARD_SIZE);
        } while (playerHits[row][col]);
        
        playerHits[row][col] = true;
        
        if (playerShips[row][col]) {
            // El enemigo acertó
            playerBoard[row][col].setBackground(Color.RED);
            playerBoard[row][col].setText("X");
            playerBoard[row][col].setForeground(Color.WHITE);
            enemyHitCount++;
            
            statusLabel.setText("El enemigo te atacó y acertó en (" + (row+1) + "," + (col+1) + ")");
            
            if (enemyHitCount >= SHIPS_TO_SINK) {
                statusLabel.setText("El enemigo hundió toda tu flota. ¡Has perdido!");
                disableAllEnemyButtons();
                return;
            }
        } else {
            // El enemigo falló
            playerBoard[row][col].setBackground(Color.WHITE);
            playerBoard[row][col].setText("O");
            playerBoard[row][col].setForeground(Color.BLUE);
            
            statusLabel.setText("El enemigo atacó (" + (row+1) + "," + (col+1) + ") y falló. Tu turno.");
        }
        
        updateLabels();
        isPlayerTurn = true;
    }
    
    private void updateLabels() {
        hitsLabel.setText("Aciertos: " + playerHitCount + "/" + SHIPS_TO_SINK);
        missesLabel.setText("Fallos: " + playerMissCount);
    }
    
    private void disableAllEnemyButtons() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                enemyBoard[i][j].setEnabled(false);
            }
        }
    }
    
    private void showEnemyShips() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (enemyShips[i][j] && enemyBoard[i][j].getBackground() != Color.RED) {
                    enemyBoard[i][j].setBackground(Color.GREEN);
                }
            }
        }
    }
    
    private void resetGame() {
        // Reiniciar contadores
        playerHitCount = 0;
        enemyHitCount = 0;
        playerMissCount = 0;
        isPlayerTurn = true;
        
        // Reiniciar arrays
        initializeArrays();
        placeShipsRandomly();
        
        // Reiniciar tableros
        remove(getContentPane());
        initializeGUI();
        revalidate();
        repaint();
        
        statusLabel.setText("Tu turno - Ataca el tablero enemigo");
        updateLabels();
    }
}
