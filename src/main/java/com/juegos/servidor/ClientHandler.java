package com.juegos.servidor;

import com.juegos.common.Player;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Maneja la comunicación con un cliente específico.
 * Cada cliente conectado tiene su propio ClientHandler ejecutándose en un hilo separado.
 * 
 * 
 */
public class ClientHandler implements Runnable {
    
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    
    private final Socket clientSocket;
    private final GameServer server;
    private final String clientId;
    
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isConnected;
    
    private Player player;
    private String currentRoomId;
    
    /**
     * Constructor del manejador de cliente.
     * @param clientSocket Socket del cliente
     * @param server Referencia al servidor principal
     */
    public ClientHandler(Socket clientSocket, GameServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.clientId = "CLIENT_" + System.currentTimeMillis() + "_" + 
                       clientSocket.getInetAddress().toString().replace("/", "");
        this.isConnected = false;
    }
    
    @Override
    public void run() {
        try {
            // Configurar streams de comunicación
            setupStreams();
            
            // Registrar cliente en el servidor
            server.registerClient(this);
            isConnected = true;
            
            // Crear jugador asociado
            player = new Player(clientId, "Jugador_" + clientId.substring(7, 11));
            player.setConnected(true);
            player.setIpAddress(clientSocket.getInetAddress().toString());
            
            // Enviar confirmación de conexión
            sendMessage("CONNECTED_TO_SERVER");
            
            // Bucle principal de comunicación
            String inputLine;
            while (isConnected && (inputLine = reader.readLine()) != null) {
                processMessage(inputLine);
            }
            
        } catch (IOException e) {
            if (isConnected) {
                LOGGER.log(Level.WARNING, "Error en comunicación con cliente " + clientId, e);
            }
        } finally {
            disconnect();
        }
    }
    
    /**
     * Configura los streams de entrada y salida.
     * @throws IOException Si hay error en la configuración
     */
    private void setupStreams() throws IOException {
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    
    /**
     * Procesa un mensaje recibido del cliente.
     * @param messageText Texto del mensaje en formato JSON
     */
    private void processMessage(String messageText) {
        try {
            // En una implementación real, aquí se deserializaría JSON
            // Por simplicidad, procesamos mensajes de texto
            
            if (messageText.startsWith("CONNECT:")) {
                handleConnect(messageText);
            } else if (messageText.startsWith("GAME_SELECT:")) {
                handleGameSelect(messageText);
            } else if (messageText.startsWith("MOVE:")) {
                handleMove(messageText);
            } else if (messageText.startsWith("DISCONNECT")) {
                handleDisconnect();
            } else {
                LOGGER.warning("Mensaje no reconocido: " + messageText);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error procesando mensaje: " + messageText, e);
            sendErrorMessage("Error procesando mensaje");
        }
    }
    
    /**
     * Maneja solicitud de conexión del cliente.
     * @param message Mensaje de conexión
     */
    private void handleConnect(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            String playerName = parts[1];
            player.setName(playerName);
            
            LOGGER.info("Cliente conectado: " + playerName + " (" + clientId + ")");
            sendMessage("CONNECT_OK:" + clientId);
        }
    }
    
    /**
     * Maneja selección de juego del cliente.
     * @param message Mensaje de selección
     */
    private void handleGameSelect(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 3) {
            String gameType = parts[1];
            String mode = parts[2];
            
            // Buscar o crear sala de juego
            String roomId = server.findOrCreateRoom(clientId, gameType, mode);
            if (roomId != null) {
                currentRoomId = roomId;
                GameRoom room = server.getGameRoom(roomId);
                
                if (room != null) {
                    room.initializeGame();
                    sendMessage("GAME_STARTED:" + roomId + ":" + gameType + ":" + mode);
                    
                    // Si la sala está llena, notificar a todos los jugadores
                    if (room.isFull()) {
                        room.broadcastToAll("GAME_READY");
                    }
                }
            } else {
                sendErrorMessage("No se pudo crear/encontrar sala de juego");
            }
        }
    }
    
    /**
     * Maneja movimiento del jugador.
     * @param message Mensaje con el movimiento
     */
    private void handleMove(String message) {
        if (currentRoomId != null) {
            GameRoom room = server.getGameRoom(currentRoomId);
            if (room != null) {
                // Extraer datos del movimiento
                String[] parts = message.split(":");
                if (parts.length >= 2) {
                    String moveData = parts[1];
                    room.processMove(clientId, moveData);
                }
            }
        } else {
            sendErrorMessage("No estás en ninguna sala de juego");
        }
    }
    
    /**
     * Maneja desconexión del cliente.
     */
    private void handleDisconnect() {
        disconnect();
    }
    
    /**
     * Envía un mensaje al cliente.
     * @param message Mensaje a enviar
     */
    public void sendMessage(String message) {
        if (writer != null && isConnected) {
            writer.println(message);
            LOGGER.fine("Mensaje enviado a " + clientId + ": " + message);
        }
    }
    
    /**
     * Envía un mensaje de error al cliente.
     * @param errorMessage Mensaje de error
     */
    public void sendErrorMessage(String errorMessage) {
        sendMessage("ERROR:" + errorMessage);
    }
    
    /**
     * Desconecta el cliente.
     */
    public void disconnect() {
        if (isConnected) {
            isConnected = false;
            
            // Desregistrar del servidor
            server.unregisterClient(clientId);
            
            // Cerrar streams
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error cerrando conexión con " + clientId, e);
            }
            
            LOGGER.info("Cliente desconectado: " + clientId);
        }
    }
    
    // Getters
    
    public String getClientId() {
        return clientId;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public String getCurrentRoomId() {
        return currentRoomId;
    }
    
    public void setCurrentRoomId(String roomId) {
        this.currentRoomId = roomId;
    }
    
    /**
     * Obtiene la dirección IP del cliente.
     * @return Dirección IP
     */
    public String getClientIP() {
        return clientSocket.getInetAddress().toString();
    }
    
    /**
     * Verifica si el cliente está activo.
     * @return true si está activo
     */
    public boolean isActive() {
        return isConnected && !clientSocket.isClosed();
    }
}
