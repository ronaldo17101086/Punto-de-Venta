package com.mycompany.chancuellarpuntodeventa.services.dashboard;

import com.mycompany.chancuellarpuntodeventa.login.LoginInterface;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ventas.ventas;
import productoFrom.productInterface;

@org.springframework.stereotype.Component
public class dashboard extends JFrame {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ventas ventasPanel;

    @Autowired
    private productInterface productosPanel;

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
    private final Color COLOR_PROVEEDOR = new Color(108, 92, 231); // Color distintivo para proveedores

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

        JLabel lblTitle = new JLabel("CHANCUELLAR POS", SwingConstants.CENTER);
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
        addMenuButton("\uD83C\uDFE0 Dashboard", "inicio");
        addMenuButton("\uD83D\uDED2 Ventas", "ventas");
        addMenuButton("\uD83D\uDCE6 Productos", "productos");

        addMenuHeader("GESTIÓN");
        addMenuButton("\uD83D\uDE9B Proveedores", "proveedores");
        addMenuButton("\uD83D\uDCB3 Pagos Realizados", "pagos_prov");

        mainContainer.add(sideMenu, BorderLayout.WEST);

        // ===== PANEL DE CONTENIDO =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_FONDO);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        // Pantallas
        contentPanel.add(createCajaDefaultPanel(), "inicio");
        contentPanel.add(ventasPanel, "ventas");
        contentPanel.add(productosPanel, "productos");
        contentPanel.add(createScreen("GESTIÓN DE PROVEEDORES"), "proveedores");
        contentPanel.add(createScreen("HISTORIAL DE PAGOS"), "pagos_prov");
    }

    private JPanel createCajaDefaultPanel() {
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(COLOR_FONDO);
        main.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Título con Info de CajaDto
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel lblTitulo = new JLabel("Resumen General de Caja");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(COLOR_PRIMARIO);
        
        JLabel lblInfo = new JLabel("Caja: Principal | Usuario: Admin | Estado: ABIERTA");
        lblInfo.setForeground(Color.GRAY);
        header.add(lblTitulo);
        header.add(lblInfo);
        main.add(header, BorderLayout.NORTH);

        // PANEL DE "CAJAS" DE RESUMEN (Ahora con 4 indicadores)
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 125));

        cardsPanel.add(createStatCard("ENTRADAS", "$5,400.00", "Ventas totales", COLOR_EXITO));
        cardsPanel.add(createStatCard("SALIDAS GRAL", "$450.00", "Gastos/Retiros", COLOR_PELIGRO));
        cardsPanel.add(createStatCard("PAGOS PROV.", "$2,100.00", "Abonos a proveedores", COLOR_PROVEEDOR));
        cardsPanel.add(createStatCard("SALDO NETO", "$2,850.00", "Efectivo disponible", COLOR_ACCENTO));

        // TABLA DE MOVIMIENTOS
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);
        
        JLabel lblTable = new JLabel("Últimos Movimientos y Pagos");
        lblTable.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablePanel.add(lblTable, BorderLayout.NORTH);

        String[] columns = {"Fecha", "Concepto / Proveedor", "Tipo", "Monto", "Método", "Usuario"};
        Object[][] data = {
            {"2026-04-27 08:30", "Venta Publico General", "INGRESO", "$450.00", "EFECTIVO", "Cajero1"},
            {"2026-04-27 09:15", "PROVEEDOR: Coca-Cola", "PAGO PROV", "$1,200.00", "TRANSFERENCIA", "Admin"},
            {"2026-04-27 10:00", "Pago de Luz (Recibo)", "SALIDA", "$300.00", "EFECTIVO", "Admin"},
            {"2026-04-27 11:20", "PROVEEDOR: Sabritas", "PAGO PROV", "$900.00", "EFECTIVO", "Admin"}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(230, 240, 255));
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        tablePanel.add(scroll, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(cardsPanel, BorderLayout.NORTH);
        body.add(tablePanel, BorderLayout.CENTER);

        main.add(body, BorderLayout.CENTER);
        return main;
    }

    private JPanel createStatCard(String title, String value, String desc, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:15");

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(130, 130, 130));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(accent);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDesc.setForeground(Color.GRAY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblDesc, BorderLayout.SOUTH);

        return card;
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

    private JPanel createScreen(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FONDO);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Light", Font.PLAIN, 40));
        label.setForeground(new Color(180, 190, 210));
        panel.add(label);
        return panel;
    }
}