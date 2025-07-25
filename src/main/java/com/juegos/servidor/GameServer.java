package com.juegos.servidor;

import com.juegos.common.GameConstants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servidor principal que maneja las conexiones de los clientes y coordina los juegos.
 * Utiliza un pool de hilos para manejar múltiples clientes simultáneamente.
 * 
 * 
 */
public class GameServer {
    
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean isRunning;
    private final int port;
    
    // Gestión de clientes y salas
    private final ConcurrentHashMap<String, ClientHandler> clients;
    private final ConcurrentHashMap<String, GameRoom> gameRooms;
    private int nextRoomId;
    
    /**
     * Constructor con puerto por defecto.
     */
    public GameServer() {
        this(GameConstants.DEFAULT_SERVER_PORT);
    }
    
    /**
     * Constructor con puerto específico.
     * @param port Puerto del servidor
     */
    public GameServer(int port) {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        this.gameRooms = new ConcurrentHashMap<>();
        this.nextRoomId = 1;
        this.isRunning = false;
    }
    
    /**
     * Inicia el servidor.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newCachedThreadPool();
            isRunning = true;
            
            LOGGER.info("Servidor iniciado en puerto " + port);
            System.out.println("🟢 Servidor de Juegos iniciado en puerto " + port);
            System.out.println("📡 Esperando conexiones de clientes...");
            
            // Hilo principal para aceptar conexiones
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    // Crear manejador para el nuevo cliente
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    
                    // Ejecutar en el pool de hilos
                    threadPool.execute(clientHandler);
                    
                    LOGGER.info("Nueva conexión aceptada desde: " + clientSocket.getInetAddress());
                    
                } catch (IOException e) {
                    if (isRunning) {
                        LOGGER.log(Level.SEVERE, "Error aceptando conexión", e);
                    }
                }
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error iniciando servidor", e);
            System.err.println("❌ Error iniciando servidor: " + e.getMessage());
        }
    }
    
    /**
     * Detiene el servidor.
     */
    public void stop() {
        isRunning = false;
        
        try {
            // Cerrar todas las conexiones de clientes
            for (ClientHandler client : clients.values()) {
                client.disconnect();
            }
            clients.clear();
            
            // Cerrar todas las salas de juego
            gameRooms.clear();
            
            // Cerrar el pool de hilos
            if (threadPool != null) {
                threadPool.shutdown();
            }
            
            // Cerrar el socket del servidor
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            LOGGER.info("Servidor detenido");
            System.out.println("🔴 Servidor detenido");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error deteniendo servidor", e);
        }
    }
    
    /**
     * Registra un nuevo cliente.
     * @param client Manejador del cliente
     */
    public void registerClient(ClientHandler client) {
        clients.put(client.getClientId(), client);
        LOGGER.info("Cliente registrado: " + client.getClientId());
        System.out.println("👤 Cliente conectado: " + client.getClientId() + 
                          " (" + clients.size() + " clientes total)");
    }
    
    /**
     * Desregistra un cliente.
     * @param clientId ID del cliente
     */
    public void unregisterClient(String clientId) {
        ClientHandler client = clients.remove(clientId);
        if (client != null) {
            // Remover de sala de juego si está en una
            removeClientFromRoom(clientId);
            
            LOGGER.info("Cliente desregistrado: " + clientId);
            System.out.println("👤 Cliente desconectado: " + clientId + 
                              " (" + clients.size() + " clientes restantes)");
        }
    }
    
    /**
     * Crea una nueva sala de juego para un jugador.
     * @param clientId ID del cliente
     * @param gameType Tipo de juego
     * @param mode Modo de juego
     * @return ID de la sala creada
     */
    public String createGameRoom(String clientId, String gameType, String mode) {
        String roomId = "ROOM_" + nextRoomId++;
        
        ClientHandler client = clients.get(clientId);
        if (client != null) {
            GameRoom room = new GameRoom(roomId, gameType, mode);
            room.addPlayer(client);
            gameRooms.put(roomId, room);
            
            LOGGER.info("Sala de juego creada: " + roomId + " (" + gameType + ", " + mode + ")");
            System.out.println("🎮 Nueva sala: " + roomId + " - " + gameType + " (" + mode + ")");
            
            return roomId;
        }
        
        return null;
    }
    
    /**
     * Busca una sala disponible para unirse o crea una nueva.
     * @param clientId ID del cliente
     * @param gameType Tipo de juego
     * @param mode Modo de juego
     * @return ID de la sala
     */
    public String findOrCreateRoom(String clientId, String gameType, String mode) {
        // Si es modo vs IA, crear sala inmediatamente
        if (GameConstants.MODE_VS_AI.equals(mode)) {
            return createGameRoom(clientId, gameType, mode);
        }
        
        // Buscar sala disponible para vs humano
        for (GameRoom room : gameRooms.values()) {
            if (room.getGameType().equals(gameType) && 
                room.getMode().equals(mode) && 
                room.hasSpace() && 
                !room.hasPlayer(clientId)) {
                
                ClientHandler client = clients.get(clientId);
                if (client != null) {
                    room.addPlayer(client);
                    LOGGER.info("Cliente " + clientId + " se unió a sala " + room.getRoomId());
                    return room.getRoomId();
                }
            }
        }
        
        // No hay salas disponibles, crear una nueva
        return createGameRoom(clientId, gameType, mode);
    }
    
    /**
     * Obtiene una sala de juego por ID.
     * @param roomId ID de la sala
     * @return Sala de juego o null si no existe
     */
    public GameRoom getGameRoom(String roomId) {
        return gameRooms.get(roomId);
    }
    
    /**
     * Remueve un cliente de su sala actual.
     * @param clientId ID del cliente
     */
    public void removeClientFromRoom(String clientId) {
        for (GameRoom room : gameRooms.values()) {
            if (room.hasPlayer(clientId)) {
                room.removePlayer(clientId);
                
                // Si la sala queda vacía, eliminarla
                if (room.isEmpty()) {
                    gameRooms.remove(room.getRoomId());
                    LOGGER.info("Sala eliminada: " + room.getRoomId());
                }
                break;
            }
        }
    }
    
    /**
     * Obtiene un cliente por ID.
     * @param clientId ID del cliente
     * @return Manejador del cliente o null si no existe
     */
    public ClientHandler getClient(String clientId) {
        return clients.get(clientId);
    }
    
    /**
     * Obtiene el número de clientes conectados.
     * @return Número de clientes
     */
    public int getClientCount() {
        return clients.size();
    }
    
    /**
     * Obtiene el número de salas activas.
     * @return Número de salas
     */
    public int getRoomCount() {
        return gameRooms.size();
    }
    
    /**
     * Verifica si el servidor está ejecutándose.
     * @return true si está ejecutándose
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Punto de entrada principal del servidor.
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        System.out.println("🎮 === SERVIDOR DE JUEGO CLIENTE-SERVIDOR ===");
        System.out.println("📅 Proyecto Paradigma De Programacion");
        System.out.println("🎯 Juego: Batalla Naval");
        System.out.println("================================================");
        
        int port = GameConstants.DEFAULT_SERVER_PORT;
        
        // Permitir especificar puerto por parámetro
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("⚠️  Puerto inválido, usando puerto por defecto: " + 
                                 GameConstants.DEFAULT_SERVER_PORT);
            }
        }
        
        GameServer server = new GameServer(port);
        
        // Manejar cierre graceful del servidor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🔄 Cerrando servidor...");
            server.stop();
        }));
        
        // Iniciar servidor
        server.start();
    }
}
