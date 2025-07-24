package com.juegos.cliente;

import com.juegos.battleship.BattleshipAI;
import com.juegos.battleship.Board;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Cliente de consola para conectarse al servidor de juegos
 */
public class ConsoleClient {
    private Socket socket;
    private PrintWriter out;
    private Scanner scanner;
    private boolean connected = false;

    public ConsoleClient() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("================================================");
        System.out.println("    BIENVENIDO A BATTLESHIP");
        System.out.println("================================================");
        
        while (true) {
            showMainMenu();
            int option = getIntInput("Selecciona un modo de juego: ");
            
            switch (option) {
                case 1:
                    connectToServer();
                    break;
                case 2:
                    if (connected) {
                        playBattleshipVsHuman();
                    } else {
                        System.out.println("Debes conectarte al servidor primero.");
                    }
                    break;
                case 3:
                    playBattleshipVsAI();
                    break;
                case 4:
                    disconnect();
                    System.out.println("Te has desconectado exitasamente.");
                    return;
                default:
                    System.out.println("Opción no válida en el menu.");
            }
            
            System.out.println("\nPresiona Enter para continuar...");
            scanner.nextLine();
        }
    }

    private void showMainMenu() {
        System.out.println("\n=== MENÚ PRINCIPAL ===");
        System.out.println("1. Conectar al servidor");
        System.out.println("2. Battleship vs Humano (Online)");
        System.out.println("3. Battleship vs IA (Local)");
        System.out.println("4. Salir");
        System.out.println("Estado: " + (connected ? "Conectado" : "Desconectado"));
    }

    private void connectToServer() {
        if (connected) {
            System.out.println("Ya estás conectado al servidor.");
            return;
        }

        System.out.print("Dirección del servidor (localhost): ");
        String address = scanner.nextLine();
        if (address.trim().isEmpty()) {
            address = "localhost"; //redireccion localhost por defecto
        }

        System.out.print("Puerto del servidor (5432): ");
        String portStr = scanner.nextLine();
        int port = 5432; //port por defecto
        if (!portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                System.out.println("Puerto inválido, usando 12345");
            }
        }

        System.out.print("Ingresa tu nombre de usuario: ");
        String name = scanner.nextLine();
        if (name.trim().isEmpty()) {
            name = "Jugador" + System.currentTimeMillis();
        }

        try {
            socket = new Socket(address, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            
            connected = true;
            
            // Enviar mensaje de conexión con el servidor
            out.println("CONNECT:" + name);
            
            System.out.println("¡Conectado al servidor exitosamente!");
            
        } catch (IOException e) {
            System.out.println("Error al conectar: " + e.getMessage());
            connected = false;
        }
    }

    private void disconnect() {
        if (connected) {
            try {
                if (out != null) {
                    out.println("DISCONNECT");
                }
                if (socket != null) {
                    socket.close();
                }
                connected = false;
                System.out.println("Desconectado del servidor.");
            } catch (IOException e) {
                System.out.println("Error al desconectar: " + e.getMessage());
            }
        }
    }

    private void playBattleshipVsHuman() {
        System.out.println("Funcionalidad en desarrollo...");
    }

    private void playBattleshipVsAI() {
        System.out.println("\n=== BATALLA NAVAL VS IA ===");
        
        // Crear tableros
        Board playerBoard = new Board();
        Board aiBoard = new Board();
        
        // Crear IA
        BattleshipAI ai = new BattleshipAI(aiBoard, playerBoard);
        
        // Configurar barcos del jugador
        System.out.println("Configurando tus barcos automáticamente...");
        playerBoard.autoPlaceShips();
        
        // Los barcos de la IA ya se configuran en el constructor
        System.out.println("IA ha colocado sus barcos.");
        
        System.out.println("\n¡Comienza la batalla!");
        
        boolean gameOver = false;
        boolean playerTurn = true;
        
        while (!gameOver) {
            if (playerTurn) {
                // Turno del jugador
                System.out.println("\n=== TU TURNO ===");
                printBattleshipBoard(aiBoard.getEnemyGrid(), "Tablero Enemigo");
                
                int row = getIntInput("Fila (0-9): ");
                int col = getIntInput("Columna (0-9): ");
                
                String result = aiBoard.receiveShot(row, col);
                
                if ("INVALID".equals(result)) {
                    System.out.println("Disparo inválido. Intenta de nuevo.");
                    continue;
                }
                
                System.out.println("Resultado: " + result);
                
                if (aiBoard.allShipsSunk()) {
                    System.out.println("¡FELICIDADES! Has hundido todos los barcos enemigos.");
                    gameOver = true;
                } else if ("MISS".equals(result)) {
                    playerTurn = false; // Cambiar turno solo si falló
                }
                
            } else {
                // Turno de la IA
                System.out.println("\n=== TURNO DE LA IA ===");
                String attack = ai.getBestAttack();
                String[] coords = attack.split(",");
                int row = Integer.parseInt(coords[0]);
                int col = Integer.parseInt(coords[1]);
                
                String result = playerBoard.receiveShot(row, col);
                ai.processAttackResult(row, col, result);
                
                System.out.println("IA atacó en (" + row + "," + col + ") - Resultado: " + result);
                
                if (playerBoard.allShipsSunk()) {
                    System.out.println("LA IA GANÓ. Ha hundido todos tus barcos.");
                    gameOver = true;
                } else if ("MISS".equals(result)) {
                    playerTurn = true; // Cambiar turno solo si falló
                }
            }
        }
        
        System.out.println("\n=== FINAL DEL JUEGO ===");
        printBattleshipBoard(playerBoard.getOwnGrid(), "Tu Tablero Final");
        printBattleshipBoard(aiBoard.getEnemyGrid(), "Tablero Enemigo Final");
    }


    private void printBattleshipBoard(char[][] board, String title) {
        System.out.println("\n=== " + title + " ===");
        System.out.print("  ");
        for (int j = 0; j < 10; j++) {
            System.out.print(j + " ");
        }
        System.out.println();
        
        for (int i = 0; i < 10; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 10; j++) {
                char cell = board[i][j];
                if (cell == 0 || cell == ' ') {
                    System.out.print("~ "); // Agua
                } else {
                    System.out.print(cell + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingresa un número válido.");
            }
        }
    }

    public static void main(String[] args) {
        new ConsoleClient().start();
    }
}
