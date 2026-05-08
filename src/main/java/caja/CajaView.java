package caja;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Vista de Control de Caja v2.5 - Edición Moderna con Sombras Reales y Componentes Custom.
 */
public class CajaView extends JFrame {

    // --- SISTEMA DE DISEÑO (Design Tokens) ---
    private final Color COLOR_FONDO       = new Color(248, 250, 252);
    private final Color COLOR_PRIMARIO    = new Color(79, 70, 229);   // Indigo Moderno
    private final Color COLOR_EXITO       = new Color(34, 197, 94);   // Verde Esmeralda
    private final Color COLOR_PELIGRO     = new Color(239, 68, 68);    // Rojo Rose
    private final Color COLOR_TEXTO_DARK  = new Color(15, 23, 42);    // Slate 900
    private final Color COLOR_TEXTO_SOFT  = new Color(100, 116, 139); // Slate 500
    private final Color COLOR_BORDE       = new Color(226, 232, 240); // Slate 200
    private final Font FONT_REGULAR       = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BOLD          = new Font("Segoe UI", Font.BOLD, 14);

    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JLabel lblEfectivoEsperado, lblVentasDia, lblGastos, lblMontoApertura;
    private JButton btnIngreso, btnGasto, btnCierre;

    public CajaView() {
        configurarVentana();
        initLayout();
        configurarEventos();
    }

    private void configurarVentana() {
        setTitle("Sistema de Gestión de Caja Pro");
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(0, 0));
    }

    private void initLayout() {
        // Cabecera Superior
        add(crearCabeceraSuperior(), BorderLayout.NORTH);

        // Contenedor Central
        JPanel content = new JPanel(new BorderLayout(0, 30));
        content.setBackground(COLOR_FONDO);
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        content.add(crearGridTarjetas(), BorderLayout.NORTH);
        content.add(crearContenedorTabla(), BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        // Barra de Herramientas Inferior
        add(crearBarraHerramientas(), BorderLayout.SOUTH);
    }

    private JPanel crearCabeceraSuperior() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 90));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(15, 40, 15, 0));

        JLabel title = new JLabel("Control de Flujo de Caja");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(COLOR_TEXTO_DARK);

        JLabel subtitle = new JLabel("Terminal: 01-PRINCIPAL | Sesión Actual");
        subtitle.setFont(FONT_REGULAR);
        subtitle.setForeground(COLOR_TEXTO_SOFT);

        left.add(title);
        left.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 25));
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(0, 0, 0, 40));

        JLabel userLabel = new JLabel("Operador: Administrador");
        userLabel.setFont(FONT_BOLD);
        
        JLabel badgeEstado = new JLabel("  CAJA ABIERTA  ");
        badgeEstado.setOpaque(true);
        badgeEstado.setBackground(new Color(220, 252, 231));
        badgeEstado.setForeground(COLOR_EXITO);
        badgeEstado.setFont(new Font("Segoe UI", Font.BOLD, 11));

        right.add(userLabel);
        right.add(badgeEstado);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel crearGridTarjetas() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 25, 0));
        grid.setBackground(COLOR_FONDO);

        grid.add(new TarjetaModerna("EFECTIVO ESPERADO", "$12,450.00", COLOR_PRIMARIO, true));
        grid.add(new TarjetaModerna("VENTAS DEL DÍA", "$8,200.00", COLOR_EXITO, false));
        grid.add(new TarjetaModerna("GASTOS Y SALIDAS", "$1,150.00", COLOR_PELIGRO, false));
        grid.add(new TarjetaModerna("MONTO APERTURA", "$5,000.00", COLOR_TEXTO_SOFT, false));

        return grid;
    }

    private JPanel crearContenedorTabla() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1, true));

        String[] cols = {"ID", "HORA", "MOVIMIENTO", "CONCEPTO", "MONTO", "USUARIO"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modeloTabla);
        configurarEstiloTabla();

        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);

        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private void configurarEstiloTabla() {
        tabla.setRowHeight(55);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setFont(FONT_REGULAR);
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 45));
        tabla.getTableHeader().setFont(FONT_BOLD);
        tabla.getTableHeader().setForeground(COLOR_TEXTO_SOFT);

        tabla.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (v != null) {
                    lbl.setForeground(v.toString().startsWith("-") ? COLOR_PELIGRO : COLOR_EXITO);
                    lbl.setFont(FONT_BOLD);
                }
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                lbl.setBorder(new EmptyBorder(0, 0, 0, 15));
                return lbl;
            }
        });
    }

    private JPanel crearBarraHerramientas() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 25));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE));

        btnIngreso = new ModernActionBtn("Nuevo Ingreso", false);
        btnGasto = new ModernActionBtn("Registrar Gasto", false);
        btnCierre = new ModernActionBtn("Realizar Cierre de Caja", true);

        bar.add(btnIngreso);
        bar.add(btnGasto);
        bar.add(btnCierre);

        return bar;
    }

    private void configurarEventos() {
        btnIngreso.addActionListener(e -> mostrarModalMovimiento("Nuevo Ingreso de Efectivo", COLOR_EXITO));
        btnGasto.addActionListener(e -> mostrarModalMovimiento("Registro de Gasto / Salida", COLOR_PELIGRO));
        btnCierre.addActionListener(e -> mostrarModalCierre());
    }

    // --- COMPONENTES PERSONALIZADOS (CLASES INTERNAS) ---

    private void mostrarModalMovimiento(String titulo, Color colorTema) {
        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setBackground(new Color(0,0,0,0));

        ShadowPanel container = new ShadowPanel(450, 580);
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(35, 45, 35, 45));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JButton close = new JButton("✕");
        close.setBorderPainted(false); close.setContentAreaFilled(false);
        close.addActionListener(e -> modal.dispose());
        head.add(lblT, BorderLayout.WEST);
        head.add(close, BorderLayout.EAST);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1; gbc.gridx = 0;

        gbc.insets = new Insets(30, 0, 5, 0);
        form.add(new JLabel("MONTO EN EFECTIVO"), gbc);
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 50));
        txt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.insets = new Insets(0, 0, 20, 0);
        form.add(txt, gbc);

        gbc.insets = new Insets(10, 0, 5, 0);
        form.add(new JLabel("CONCEPTO"), gbc);
        JComboBox<String> combo = new JComboBox<>(new String[]{"Venta Directa", "Ajuste de Caja", "Pago Servicio"});
        combo.setPreferredSize(new Dimension(0, 45));
        gbc.insets = new Insets(0, 0, 30, 0);
        form.add(combo, gbc);

        JButton confirm = new ModernActionBtn("Confirmar Movimiento", true);
        confirm.setBackground(colorTema);

        container.add(head, BorderLayout.NORTH);
        container.add(form, BorderLayout.CENTER);
        container.add(confirm, BorderLayout.SOUTH);

        modal.add(container);
        modal.pack();
        modal.setLocationRelativeTo(this);
        modal.setVisible(true);
    }

    private void mostrarModalCierre() {
        // Implementación similar con iconos vectoriales dibujados manualmente
        mostrarModalMovimiento("Cierre de Jornada", COLOR_PRIMARIO);
    }

    class ShadowPanel extends JPanel {
        public ShadowPanel(int w, int h) {
            setPreferredSize(new Dimension(w, h));
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Sombra Progresiva (Efecto Glass/Modern)
            for (int i = 0; i < 15; i++) {
                g2.setColor(new Color(0, 0, 0, 15 - i));
                g2.drawRoundRect(5+i, 5+i, getWidth()-12-i*2, getHeight()-12-i*2, 30, 30);
            }
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(10, 10, getWidth()-25, getHeight()-25, 25, 25);
            g2.dispose();
        }
    }

    class TarjetaModerna extends JPanel {
        public TarjetaModerna(String title, String value, Color color, boolean highlight) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(25, 25, 25, 25)
            ));

            JLabel t = new JLabel(title);
            t.setFont(new Font("Segoe UI", Font.BOLD, 11));
            t.setForeground(COLOR_TEXTO_SOFT);

            JLabel v = new JLabel(value);
            v.setFont(new Font("Segoe UI", Font.BOLD, highlight ? 32 : 26));
            v.setForeground(color);

            add(t, BorderLayout.NORTH);
            add(v, BorderLayout.CENTER);
            
            // Vincular labels para actualización dinámica
            if(title.contains("ESPERADO")) lblEfectivoEsperado = v;
        }
    }

    class ModernActionBtn extends JButton {
        private boolean primary;
        public ModernActionBtn(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(FONT_BOLD);
            setPreferredSize(new Dimension(220, 52));
            setBackground(primary ? COLOR_PRIMARIO : Color.WHITE);
            setForeground(primary ? Color.WHITE : COLOR_TEXTO_DARK);
            if(!primary) setBorder(BorderFactory.createLineBorder(COLOR_BORDE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}