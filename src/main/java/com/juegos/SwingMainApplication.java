package com.juegos;


import com.juegos.ui.BattleshipWindow;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.io.*;

/**
 * Aplicación principal con interfaz Swing para el sistema de juegos cliente-servidor.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0
 */
public class SwingMainApplication extends JFrame {
    
    private JTextField serverField;
    private JTextField portField;
    private JButton connectButton;
    private JButton battleshipButton;
    private JTextArea statusArea;
    
    private Socket socket;
    private PrintWriter out;
    
    public SwingMainApplication() {
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Juegos Cliente-Servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel de conexión
        JPanel connectionPanel = createConnectionPanel();
        mainPanel.add(connectionPanel, BorderLayout.NORTH);
        
        // Panel de juegos
        JPanel gamePanel = createGamePanel();
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        
        // Panel de estado
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Configurar listeners
        setupListeners();
    }
    
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Conexión al Servidor"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Servidor
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Servidor:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        serverField = new JTextField("localhost", 15);
        panel.add(serverField, gbc);
        
        // Puerto
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Puerto:"), gbc);
        
        gbc.gridx = 3; gbc.gridy = 0;
        portField = new JTextField("5432", 8);
        panel.add(portField, gbc);
        
        // Botón conectar
        gbc.gridx = 4; gbc.gridy = 0;
        connectButton = new JButton("Conectar");
        panel.add(connectButton, gbc);
        
        return panel;
    }
    
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Seleccionar Juego"));
        
        // Botón Battleship
        battleshipButton = new JButton("Battleship");
        battleshipButton.setFont(new Font("Arial", Font.BOLD, 16));
        battleshipButton.setEnabled(false);
        panel.add(battleshipButton);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Estado"));
        
        statusArea = new JTextArea(8, 50);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(statusArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupListeners() {
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (socket == null || socket.isClosed()) {
                    connectToServer();
                } else {
                    disconnectFromServer();
                }
            }
        });
        
        battleshipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startBattleship();
            }
        });
    }
    
    private void connectToServer() {
        try {
            String server = serverField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            
            addStatus("Conectando a " + server + ":" + port + "...");
            
            socket = new Socket(server, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            
            addStatus("✓ Conectado exitosamente al servidor!");
            
            connectButton.setText("Desconectar");
            battleshipButton.setEnabled(true);
            serverField.setEnabled(false);
            portField.setEnabled(false);
            
        } catch (Exception e) {
            addStatus("✗ Error al conectar: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "No se pudo conectar al servidor.\n" + 
                "Asegúrate de que el servidor esté ejecutándose.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void disconnectFromServer() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            addStatus("Desconectado del servidor.");
            
            connectButton.setText("Conectar");
            battleshipButton.setEnabled(false);
            serverField.setEnabled(true);
            portField.setEnabled(true);
            
        } catch (Exception e) {
            addStatus("Error al desconectar: " + e.getMessage());
        }
    }
    
    private void startBattleship() {
        addStatus("Iniciando Battleship...");
        if (out != null) {
            out.println("START_BATTLESHIP");
        }
        
        // Abrir ventana del juego Battleship
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BattleshipWindow gameWindow = new BattleshipWindow();
                gameWindow.setVisible(true);
            }
        });
    }
    
    private void addStatus(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                statusArea.append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
                statusArea.setCaretPosition(statusArea.getDocument().getLength());
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SwingMainApplication().setVisible(true);
            }
        });
    }
}
