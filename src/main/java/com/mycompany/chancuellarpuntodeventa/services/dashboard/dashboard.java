package com.mycompany.chancuellarpuntodeventa.services.dashboard;

import com.mycompany.chancuellarpuntodeventa.login.LoginInterface;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ventas.ventas;
import productoFrom.productInterface;
import clientes.clientInterface;

@org.springframework.stereotype.Component
public class dashboard extends JFrame {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ventas ventasPanel;

    @Autowired
    private productInterface productosPanel;

    @Autowired
    private clientInterface clientesPanel;

    private JPanel sideMenu;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Colores de Paleta
    private final Color COLOR_PRIMARIO = new Color(28, 35, 49);
    private final Color COLOR_BARRA_SUP = new Color(43, 52, 69);
    private final Color COLOR_ACCENTO = new Color(0, 153, 255);
    private final Color COLOR_LOGOUT = new Color(210, 45, 45);
    private final Color COLOR_FONDO = new Color(235, 238, 242);
    private final Color COLOR_EXITO = new Color(40, 167, 69);
    private final Color COLOR_PELIGRO = new Color(220, 53, 69);
    private final Color COLOR_PROVEEDOR = new Color(108, 92, 231);
    private final Color COLOR_CLIENTE = new Color(32, 201, 151);

    public dashboard() {
    }

    @jakarta.annotation.PostConstruct
    public void setup() {
        initUI();
        cardLayout.show(contentPanel, "inicio");
    }

    private void initUI() {
        setTitle("Chancuellar - Gestión de Punto de Venta");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));
        setLayout(new BorderLayout());

        // ===== BARRA SUPERIOR =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, 45));
        topBar.setBackground(COLOR_BARRA_SUP);

        JLabel lblTitle = new JLabel("Punto de Venta CHANCUELLAR", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        topBar.add(lblTitle, BorderLayout.CENTER);

        JPanel leftSpacer = new JPanel();
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(220, 0));
        topBar.add(leftSpacer, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(220, 0));

        JButton btnLogout = new JButton("CERRAR SESIÓN");
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBackground(COLOR_LOGOUT);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        btnLogout.addActionListener(e -> logout());

        rightPanel.add(btnLogout);
        topBar.add(rightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ===== CONTENEDOR PRINCIPAL =====
        JPanel mainContainer = new JPanel(new BorderLayout());
        add(mainContainer, BorderLayout.CENTER);

        // ===== MENÚ LATERAL =====
        sideMenu = new JPanel();
        sideMenu.setPreferredSize(new Dimension(190, 0));
        sideMenu.setBackground(COLOR_PRIMARIO);
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        addMenuHeader("OPERACIONES");
        addMenuButton("\uD83D\uDED2 Ventas", "ventas");
        addMenuButton("\uD83D\uDCE6 Productos", "productos");

        addMenuHeader("CREDITO");
        addMenuButton("\uD83D\uDC65 Clientes", "clientes");

        mainContainer.add(sideMenu, BorderLayout.WEST);

        // ===== PANEL DE CONTENIDO =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_FONDO);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(ventasPanel, "ventas");
        contentPanel.add(productosPanel, "productos");
        contentPanel.add(clientesPanel, "clientes");
    }

    private void addMenuHeader(String text) {
        JLabel header = new JLabel(text);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(new Color(110, 125, 150));
        header.setBorder(BorderFactory.createEmptyBorder(20, 12, 5, 0));
        sideMenu.add(header);
    }

    private void addMenuButton(String name, String card) {
        JButton btn = new JButton(name);
        btn.setFocusPainted(false);
        btn.setForeground(new Color(190, 200, 215));
        btn.setBackground(COLOR_PRIMARIO);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(40, 50, 70));
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, COLOR_ACCENTO));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_PRIMARIO);
                btn.setForeground(new Color(190, 200, 215));
                btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
            }
        });

        btn.addActionListener(e -> cardLayout.show(contentPanel, card));
        sideMenu.add(btn);
        sideMenu.add(Box.createRigidArea(new Dimension(0, 2)));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?", "Chancuellar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            context.getBean(LoginInterface.class).setVisible(true);
        }
    }
}
