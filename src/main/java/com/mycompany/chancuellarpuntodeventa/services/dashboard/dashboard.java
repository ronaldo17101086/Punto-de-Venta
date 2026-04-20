package com.mycompany.chancuellarpuntodeventa.services.dashboard;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.mycompany.chancuellarpuntodeventa.chancuellarpuntodeventa;
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
    }

    @jakarta.annotation.PostConstruct
    public void setup() {
        initUI();
    }

    private void initUI() {
        // ===== BARRA SUPERIOR =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, 40));
        topBar.setBackground(new Color(30, 30, 30));

        // ===== IZQUIERDA: BOTON MENU + "Punto de Venta" =====
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setBackground(new Color(30, 30, 30));

        // Botón de menú hamburguesa
        JButton menuToggle = new JButton("☰");
        menuToggle.setFocusPainted(false);
        menuToggle.setBorderPainted(false);
        menuToggle.setForeground(Color.WHITE);
        menuToggle.setBackground(new Color(30, 30, 30));
        menuToggle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        menuToggle.setPreferredSize(new Dimension(50, 30));

        leftPanel.add(menuToggle);

        // Etiqueta "Punto de Venta"
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

        // ===== DERECHA: BOTONES DE VENTANA =====
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

        // ---- BOTONES DEL MENU ----
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

        // ===== PANEL DE CONTENIDO (PANTALLAS) =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        // PANTALLAS
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

        // ===== EVENTO HAMBURGUESA =====
        menuToggle.addActionListener(e -> toggleMenu());

        // ===== MOVER VENTANA =====
        final Point[] mouseDownCompCoords = {null};
        topBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (fullscreen) {
                    device.setFullScreenWindow(null);
                    dispose();
                    setResizable(true);
                    setBounds(100, 100, 1200, 700);
                    setVisible(true);
                    fullscreen = false;
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

        // ESC para salir
        getRootPane().registerKeyboardAction(e -> System.exit(0),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // abrir maximizada REAL
        SwingUtilities.invokeLater(() -> toggleMaximize());
    }

// ---- METODO PARA CREAR BOTONES DEL MENU ----
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
        sideMenu.add(Box.createRigidArea(new Dimension(0, 5))); // espacio entre botones
    }

// ---- METODO PARA MOSTRAR/OCULTAR EL MENU ----
    private void toggleMenu() {
        sideMenu.setVisible(!sideMenu.isVisible());
    }

    // ===== MAXIMIZADO REAL =====
    private GraphicsDevice device = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();

    private boolean fullscreen = false;

    private void toggleMaximize() {
        if (!fullscreen) {
            dispose();
            setVisible(false);
            setResizable(false);
            device.setFullScreenWindow(this);
            setVisible(true);
        } else {
            device.setFullScreenWindow(null);
            dispose();
            setVisible(false);
            setResizable(true);
            setBounds(100, 100, 1200, 700);
            setVisible(true);
        }
        fullscreen = !fullscreen;
    }

    private JButton createWindowButton(String text, Color normal, Color hover) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setForeground(Color.WHITE);
        btn.setBackground(normal);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(45, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(normal);
            }
        });

        return btn;
    }

    private JPanel createScreen(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 28));
        panel.add(label);
        return panel;
    }

    private JButton createMenuButton(String text, String panel) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(45, 45, 45));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addActionListener(e -> cardLayout.show(contentPanel, panel));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(70, 70, 70));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(45, 45, 45));
            }
        });

        return btn;
    }

}
