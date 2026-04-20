package com.mycompany.chancuellarpuntodeventa.services.dashboard;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.springframework.beans.factory.annotation.Autowired;
import ventas.ventas;
import producto.productInterface;

@org.springframework.stereotype.Component
public class dashboard extends JFrame {

    private boolean maximized = true;
    private Rectangle normalBounds;
    private String companyName = "Chancuellar Jalisco";

    @org.springframework.beans.factory.annotation.Autowired
    private ventas ventasPanel;

    @Autowired
    private productInterface productosPanel;

    private JPanel sideMenu;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private boolean menuVisible = true;

    public dashboard() {
        // Constructor vacío
    }

    @jakarta.annotation.PostConstruct
    public void setup() {
        initUI();
    }

    private void initUI() {
        // ===== CONFIGURACIÓN INICIAL DE LA VENTANA =====
        // IMPORTANTE: No llamar a setVisible(true) aquí
        setTitle("Dashboard - " + companyName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // ===== BARRA SUPERIOR =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, 40));
        topBar.setBackground(new Color(30, 30, 30));

        // ===== IZQUIERDA: BOTON MENU + "Punto de Venta" =====
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setBackground(new Color(30, 30, 30));

        JButton menuToggle = new JButton("☰");
        menuToggle.setFocusPainted(false);
        menuToggle.setBorderPainted(false);
        menuToggle.setForeground(Color.WHITE);
        menuToggle.setBackground(new Color(30, 30, 30));
        menuToggle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        menuToggle.setPreferredSize(new Dimension(50, 30));
        leftPanel.add(menuToggle);

        JLabel leftLabel = new JLabel("Punto de Venta");
        leftLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftLabel.setForeground(Color.WHITE);
        leftPanel.add(leftLabel);
        topBar.add(leftPanel, BorderLayout.WEST);

        // ===== CENTRO: "CHANCUELLAR" =====
        JLabel centerLabel = new JLabel("CHANCUELLAR", SwingConstants.CENTER);
        centerLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        centerLabel.setForeground(Color.WHITE);
        topBar.add(centerLabel, BorderLayout.CENTER);

        // ===== DERECHA: BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttons.setBackground(new Color(30, 30, 30));
        topBar.add(buttons, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ===== CONTENEDOR PRINCIPAL =====
        JPanel mainContainer = new JPanel(new BorderLayout());
        add(mainContainer, BorderLayout.CENTER);

        // ===== MENU LATERAL =====
        sideMenu = new JPanel();
        sideMenu.setPreferredSize(new Dimension(220, 0));
        sideMenu.setBackground(new Color(45, 45, 45));
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        mainContainer.add(sideMenu, BorderLayout.WEST);

        // BOTONES DEL MENU
        addMenuButton("Ventas", "ventas");
        addMenuButton("Cotizaciones", "cotizaciones");
        addMenuButton("Compras", "compras");
        addMenuButton("Productos", "productos");
        addMenuButton("Clientes", "clientes");
        addMenuButton("Usuarios", "usuarios");
        addMenuButton("Proveedores", "proveedores");
        addMenuButton("Reportes", "reportes");
        addMenuButton("Consultas", "consultas");
        addMenuButton("Configuraciones", "configuraciones");
        addMenuButton("Suscripción ", "suscripción");

        // ===== PANEL DE CONTENIDO =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(ventasPanel, "ventas");
        contentPanel.add(productosPanel, "productos");
        contentPanel.add(createScreen("COTIZACIONES"), "cotizaciones");
        contentPanel.add(createScreen("COMPRAS"), "compras");
        contentPanel.add(createScreen("CLIENTES"), "clientes");
        contentPanel.add(createScreen("USUARIOS"), "usuarios");
        contentPanel.add(createScreen("PROVEEDORES"), "proveedores");
        contentPanel.add(createScreen("REPORTES"), "reportes");
        contentPanel.add(createScreen("CONSULTAS"), "consultas");
        contentPanel.add(createScreen("CONFIGURACIONES"), "configuraciones");
        contentPanel.add(createScreen("SUSCRIPCION"), "suscripción");

        menuToggle.addActionListener(e -> toggleMenu());

        // MOVER VENTANA
        final Point[] mouseDownCompCoords = {null};
        topBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (fullscreen) {
                    toggleMaximize(); // Salir de pantalla completa para mover
                }
                mouseDownCompCoords[0] = e.getPoint();
            }
        });

        topBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                setLocation(currCoords.x - mouseDownCompCoords[0].x,
                        currCoords.y - mouseDownCompCoords[0].y);
            }
        });

        // ESC para cerrar
        getRootPane().registerKeyboardAction(e -> System.exit(0),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // ELIMINADO: SwingUtilities.invokeLater(() -> toggleMaximize());
        // En su lugar, simplemente preparamos la ventana
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
    }

    private void addMenuButton(String name, String card) {
        JButton btn = new JButton(name);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 60, 60));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.addActionListener(e -> cardLayout.show(contentPanel, card));
        sideMenu.add(btn);
        sideMenu.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void toggleMenu() {
        sideMenu.setVisible(!sideMenu.isVisible());
    }

    private GraphicsDevice device = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();

    private boolean fullscreen = false;

    // Método corregido para no forzar visibilidad si no es necesario
    public void toggleMaximize() {
        if (!fullscreen) {
            dispose();
            setUndecorated(true);
            setResizable(false);
            device.setFullScreenWindow(this);
        } else {
            device.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            setResizable(true);
            setBounds(100, 100, 1200, 700);
            setLocationRelativeTo(null);
        }
        fullscreen = !fullscreen;
        // Solo mostramos si ya era visible previamente
        if (this.isDisplayable()) {
            setVisible(true);
        }
    }

    private JPanel createScreen(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 28));
        panel.add(label);
        return panel;
    }
}