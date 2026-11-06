package com.mycompany.chancuellarpuntodeventa.services.dashboard;

import com.mycompany.chancuellarpuntodeventa.login.LoginInterface;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ventas.ventas;
import productoFrom.productInterface;
import clientes.clientInterface;
import consultasVentas.ConsultasVentasInterfice;

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

    @Autowired
    private ConsultasVentasInterfice consultasPanel;

    private JPanel sideMenu;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private boolean menuExpandido = true; // Controla si el menú está abierto o contraído

    // Colores de Paleta originales
    private final Color COLOR_PRIMARIO = new Color(28, 35, 41);
    private final Color COLOR_BARRA_SUP = new Color(43, 52, 69);
    private final Color COLOR_ACCENTO = new Color(0, 153, 255);
    private final Color COLOR_LOGOUT = new Color(210, 45, 45);
    private final Color COLOR_FONDO = new Color(235, 238, 242);

    public dashboard() {
    }

    @jakarta.annotation.PostConstruct
    public void setup() {
        initUI();
        cardLayout.show(contentPanel, "ventas");
    }

    private void initUI() {
        setTitle("Chancuellar - Gestión de Punto de Venta");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 720));
        setLayout(new BorderLayout());

        // ===== BARRA SUPERIOR =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, 45));
        topBar.setBackground(COLOR_BARRA_SUP);

        // Panel izquierdo en la barra superior (Contiene el botón de menú con icono vectorial)
        JPanel topLeftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topLeftBar.setOpaque(false);
        topLeftBar.setPreferredSize(new Dimension(220, 0));

        JButton btnToggleMenu = new JButton(" Menú", crearIconoHamburguesa());
        btnToggleMenu.setFocusPainted(false);
        btnToggleMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleMenu.setBackground(new Color(55, 65, 81));
        btnToggleMenu.setForeground(Color.WHITE);
        btnToggleMenu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleMenu.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0");

        // ACCIÓN PARA CONTRAER / EXPANDIR EL MENÚ LATERAL COMPLETO
        btnToggleMenu.addActionListener(e -> {
            menuExpandido = !menuExpandido;
            if (menuExpandido) {
                sideMenu.setPreferredSize(new Dimension(210, 0));
                btnToggleMenu.setText(" Ocultar");
            } else {
                sideMenu.setPreferredSize(new Dimension(0, 0)); // Colapsa el menú y libera todo el espacio
                btnToggleMenu.setText(" Menú");
            }
            sideMenu.revalidate();
            sideMenu.repaint();
        });

        topLeftBar.add(btnToggleMenu);
        topBar.add(topLeftBar, BorderLayout.WEST);

        JLabel lblTitle = new JLabel("Punto de Venta CHANCUELLAR", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        topBar.add(lblTitle, BorderLayout.CENTER);

        // Panel derecho en la barra superior (Cerrar sesión)
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

        // ===== MENÚ LATERAL COMPLETO =====
        sideMenu = new JPanel();
        sideMenu.setPreferredSize(new Dimension(210, 0));
        sideMenu.setBackground(COLOR_PRIMARIO);
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Añadimos los encabezados y botones originales
        addMenuHeader("OPERACIONES");
        addMenuButton(" Ventas", "ventas", crearIconoCarrito());
        addMenuButton(" Productos", "productos", crearIconoProductos());
        addMenuButton(" Consultas", "consultas", crearIconoConsultas());

        addMenuHeader("CREDITO");
        addMenuButton(" Clientes", "clientes", crearIconoClientes());

        mainContainer.add(sideMenu, BorderLayout.WEST);

        // ===== PANEL DE CONTENIDO =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_FONDO);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(ventasPanel, "ventas");
        contentPanel.add(productosPanel, "productos");
        contentPanel.add(clientesPanel, "clientes");
        contentPanel.add(consultasPanel, "consultas");
    }

    private Icon crearIconoHamburguesa() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Tres líneas horizontales modernas
                g2.drawLine(x + 2, y + 5, x + 16, y + 5);
                g2.drawLine(x + 2, y + 9, x + 16, y + 9);
                g2.drawLine(x + 2, y + 13, x + 16, y + 13);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private void addMenuHeader(String text) {
        JLabel header = new JLabel(text);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(new Color(110, 125, 150));
        header.setBorder(BorderFactory.createEmptyBorder(20, 15, 5, 0));
        sideMenu.add(header);
    }

    private void addMenuButton(String name, String card, Icon icono) {
        JButton btn = new JButton(name, icono);
        btn.setFocusPainted(false);
        btn.setForeground(new Color(190, 200, 215));
        btn.setBackground(COLOR_PRIMARIO);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);
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
        sideMenu.add(Box.createRigidArea(new Dimension(0, 3)));
    }

    // ==========================================
    // MÉTODOS DE ICONOS VECTORIALES NATIVOS
    // ==========================================
    private Icon crearIconoCarrito() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 175, 195));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g2.drawRect(x + 1, y + 4, 14, 10);
                g2.drawLine(x + 4, y + 17, x + 6, y + 17);
                g2.drawLine(x + 12, y + 17, x + 14, y + 17);
                g2.drawLine(x + 3, y + 1, x + 5, y + 4);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private Icon crearIconoProductos() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 175, 195));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g2.drawRect(x + 1, y + 5, 16, 12);
                g2.drawLine(x + 1, y + 9, x + 17, y + 9);
                g2.drawLine(x + 9, y + 9, x + 9, y + 17);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private Icon crearIconoConsultas() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 175, 195));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g2.drawOval(x + 1, y + 1, 10, 10);
                g2.drawLine(x + 10, y + 10, x + 16, y + 16);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private Icon crearIconoClientes() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 175, 195));
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g2.drawOval(x + 5, y + 1, 5, 5);
                g2.drawArc(x + 1, y + 9, 13, 7, 0, 180);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private void logout() {
        JDialog dialog = new JDialog(this, "Cerrar Sesión", true);
        dialog.setSize(380, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.getRootPane().putClientProperty(FlatClientProperties.STYLE, "border:1,1,1,1,#E0E0E0,20,20");

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel lblMensaje = new JLabel("<html><div style='text-align: center;'><b style='font-size: 15px; color: #202938;'>¿Desea cerrar sesión?</b><br><span style='color: #6c757d; font-size: 12px;'>.</span></div></html>", SwingConstants.CENTER);
        panel.add(lblMensaje, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(110, 38));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.putClientProperty(FlatClientProperties.STYLE, "arc:12; background:#F0F2F5; borderWidth:0; font:bold 12 ciudadanos");
        btnCancelar.addActionListener(e -> dialog.dispose());

       JButton btnConfirmar = new JButton("Sí, Cerrar");
        btnConfirmar.setPreferredSize(new Dimension(120, 38));
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.putClientProperty(FlatClientProperties.STYLE, "arc:12; background:#E53E3E; foreground:#FFFFFF; borderWidth:0; font:bold 12 sans");
        btnConfirmar.addActionListener(e -> {
            dialog.dispose();
            this.dispose();
            // Llama al método que reinicia y vuelve a mostrar el Login correctamente
            context.getBean(LoginInterface.class).reiniciarLogin();
        });

        panelBotones.add(btnCancelar);
        panelBotones.add(btnConfirmar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
