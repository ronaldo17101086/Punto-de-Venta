package caja;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CajaView extends JFrame {

    // --- PALETA DE COLORES PREMIUM ---
    private final Color COLOR_FONDO = new Color(241, 245, 249);
    private final Color COLOR_PRIMARIO = new Color(99, 102, 241);
    private final Color COLOR_EXITO = new Color(34, 197, 94);
    private final Color COLOR_PELIGRO = new Color(239, 68, 68);
    private final Color COLOR_TEXTO_DARK = new Color(30, 41, 59);
    private final Color COLOR_TEXTO_SOFT = new Color(100, 116, 139);
    private final Color COLOR_BORDE = new Color(226, 232, 240);
    private final Font FONT_REGULAR = new Font("Inter", Font.PLAIN, 14);
    private final Font FONT_BOLD = new Font("Inter", Font.BOLD, 14);

    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JLabel lblEfectivoEsperado, lblVentasDia, lblGastos, lblMontoApertura;
    private JButton btnIngreso, btnGasto, btnCierre;

    private CajaService service;
    private final int ID_CAJA_ACTUAL = 1;

// BORRA EL CONSTRUCTOR VACÍO Y EL QUE TENÍAS ANTES. 
// DEJA SOLO ESTE:
    @Autowired
    public CajaView(CajaService service) {
        this.service = service; // Guardamos el servicio primero
        configurarVentana();
        initLayout();
        configurarEventos();

        // Solo actualizamos si el servicio llegó bien
        if (this.service != null) {
            actualizarDataReal();
        } else {
            System.err.println("¡ADVERTENCIA: El servicio de caja llegó nulo!");
        }
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
        add(crearCabeceraSuperior(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 30));
        content.setBackground(COLOR_FONDO);
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        content.add(crearGridTarjetas(), BorderLayout.NORTH);
        content.add(crearContenedorTabla(), BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
        add(crearBarraHerramientas(), BorderLayout.SOUTH);
    }

    public void actualizarDataReal() {
        if (service == null) {
            return;
        }

        // Actualizar Tabla
        service.fetchMovimientosATabla(modeloTabla, ID_CAJA_ACTUAL);

        // Actualizar Tarjetas (Kpis)
        double total = service.getSaldoTotal(ID_CAJA_ACTUAL);
        if (lblEfectivoEsperado != null) {
            lblEfectivoEsperado.setText(String.format("$%.2f", total));
        }
        // Nota: Aquí podrías llamar a métodos de service para lblVentasDia, etc.
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
        title.setFont(new Font("Inter", Font.BOLD, 26));
        title.setForeground(COLOR_TEXTO_DARK);

        JLabel subtitle = new JLabel("Terminal: 01-PRINCIPAL | Monitoreo en vivo");
        subtitle.setFont(FONT_REGULAR);
        subtitle.setForeground(COLOR_TEXTO_SOFT);

        left.add(title);
        left.add(subtitle);
        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JPanel crearGridTarjetas() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 25, 0));
        grid.setBackground(COLOR_FONDO);
        grid.add(new TarjetaModerna("EFECTIVO ESPERADO", "$0.00", COLOR_PRIMARIO, true));
        grid.add(new TarjetaModerna("VENTAS DEL DÍA", "$0.00", COLOR_EXITO, false));
        grid.add(new TarjetaModerna("GASTOS Y SALIDAS", "$0.00", COLOR_PELIGRO, false));
        grid.add(new TarjetaModerna("MONTO APERTURA", "$0.00", COLOR_TEXTO_SOFT, false));
        return grid;
    }

    private JPanel crearContenedorTabla() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1, true));

        String[] cols = {"ID", "FECHA/HORA", "TIPO", "COMENTARIO", "MONTO", "USUARIO"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
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
        tabla.setRowHeight(45);
        tabla.setSelectionBackground(new Color(249, 250, 251));
        tabla.setSelectionForeground(COLOR_TEXTO_DARK);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(COLOR_BORDE);
        tabla.setFont(new Font("Inter", Font.PLAIN, 13));

        // Estilo de encabezado
        tabla.getTableHeader().setFont(FONT_BOLD);
        tabla.getTableHeader().setBackground(Color.WHITE);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Renderer para el Monto
        tabla.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (v != null) {
                    double val = Double.parseDouble(v.toString().replace("$", "").replace(",", ""));
                    setForeground(val < 0 ? COLOR_PELIGRO : COLOR_EXITO);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setFont(FONT_BOLD);
                }
                return this;
            }
        });
    }

    private JPanel crearBarraHerramientas() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 25));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE));

        btnIngreso = new ModernActionBtn("Nuevo Ingreso", COLOR_EXITO);
        btnGasto = new ModernActionBtn("Registrar Gasto", COLOR_PELIGRO);
        btnCierre = new ModernActionBtn("Realizar Corte", COLOR_PRIMARIO);

        bar.add(btnIngreso);
        bar.add(btnGasto);
        bar.add(btnCierre);
        return bar;
    }

    private void configurarEventos() {
        btnIngreso.addActionListener(e -> mostrarModalMovimiento("Ingreso de Efectivo", COLOR_EXITO, true));
        btnGasto.addActionListener(e -> mostrarModalMovimiento("Egreso / Gasto", COLOR_PELIGRO, false));
        btnCierre.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que desea cerrar la caja y generar el reporte?", "Corte de Caja", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && service != null) {
                service.sendRequest("POST", ID_CAJA_ACTUAL, TipoMovimiento.CIERRE, 0.0, "Corte de caja efectuado", null);
                actualizarDataReal();
            }
        });
    }

    private void mostrarModalMovimiento(String titulo, Color colorTema, boolean esIngreso) {
        JDialog modal = new JDialog(this, true);
        modal.setUndecorated(true);
        modal.setBackground(new Color(0, 0, 0, 0));

        ShadowPanel container = new ShadowPanel(450, 520);
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(30, 35, 30, 35));

        // Cabecera Modal
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo.toUpperCase());
        lblTitulo.setFont(new Font("Inter", Font.BOLD, 18));
        lblTitulo.setForeground(colorTema);

        JButton btnCerrar = new JButton("✕");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> modal.dispose());

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(btnCerrar, BorderLayout.EAST);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 0, 5, 0);

        // Campo Monto
        form.add(new JLabel("MONTO DEL MOVIMIENTO"), gbc);
        JTextField txtMonto = new JTextField();
        txtMonto.setPreferredSize(new Dimension(0, 40));
        txtMonto.setFont(new Font("Inter", Font.BOLD, 16));
        form.add(txtMonto, gbc);

        // Campo Tipo
        form.add(new JLabel("CATEGORÍA"), gbc);
        TipoMovimiento[] opciones = esIngreso
                ? new TipoMovimiento[]{TipoMovimiento.VENTA, TipoMovimiento.INGRESO_EXTRA, TipoMovimiento.APERTURA}
                : new TipoMovimiento[]{TipoMovimiento.RETIRO_GASTO, TipoMovimiento.PAGO_PROVEEDOR, TipoMovimiento.DEVOLUCION};
        JComboBox<TipoMovimiento> combo = new JComboBox<>(opciones);
        combo.setPreferredSize(new Dimension(0, 40));
        form.add(combo, gbc);

        // Campo Comentario (NUEVO)
        form.add(new JLabel("COMENTARIO / OBSERVACIÓN"), gbc);
        JTextArea txtNota = new JTextArea(3, 20);
        txtNota.setLineWrap(true);
        txtNota.setBorder(BorderFactory.createLineBorder(COLOR_BORDE));
        form.add(new JScrollPane(txtNota), gbc);

        // Botón Confirmar
        JButton btnConfirmar = new ModernActionBtn("Confirmar Movimiento", colorTema);
        // Busca esta parte dentro de mostrarModalMovimiento y reemplázala:
        btnConfirmar.addActionListener(e -> {
            try {
                String textoMonto = txtMonto.getText().trim();
                if (textoMonto.isEmpty()) {
                    JOptionPane.showMessageDialog(modal, "El monto no puede estar vacío.");
                    return;
                }

                double monto = Double.parseDouble(textoMonto);
                TipoMovimiento tipo = (TipoMovimiento) combo.getSelectedItem();
                String nota = txtNota.getText().trim().isEmpty() ? "Movimiento manual" : txtNota.getText();

                System.out.println("Intentando guardar: " + tipo + " de $" + monto); // Debug en consola

                if (service == null) {
                    JOptionPane.showMessageDialog(modal, "Error: El servicio de base de datos no está inicializado.");
                    return;
                }

                // Ejecutamos la petición
                boolean exito = service.sendRequest("POST", ID_CAJA_ACTUAL, tipo, monto, nota, null);

                if (exito) {
                    actualizarDataReal(); // Refresca la tabla y el label azul
                    modal.dispose();      // Cierra el modal
                    JOptionPane.showMessageDialog(this, "Movimiento registrado con éxito.");
                } else {
                    JOptionPane.showMessageDialog(modal, "La base de datos rechazó el movimiento.");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(modal, "El monto debe ser un número válido (ej: 500.00)");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(modal, "Error inesperado: " + ex.getMessage());
            }
        });

        container.add(header, BorderLayout.NORTH);
        container.add(form, BorderLayout.CENTER);
        container.add(btnConfirmar, BorderLayout.SOUTH);

        modal.add(container);
        modal.pack();
        modal.setLocationRelativeTo(this);
        modal.setVisible(true);
    }

    // --- COMPONENTES PERSONALIZADOS ---
    class ShadowPanel extends JPanel {

        public ShadowPanel(int w, int h) {
            setPreferredSize(new Dimension(w, h));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 30, 30);
            g2.dispose();
        }
    }

    class TarjetaModerna extends JPanel {

        public TarjetaModerna(String title, String value, Color color, boolean highlight) {
            setLayout(new BorderLayout(0, 10));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                    new EmptyBorder(20, 20, 20, 20)
            ));

            JLabel t = new JLabel(title);
            t.setFont(new Font("Inter", Font.BOLD, 12));
            t.setForeground(COLOR_TEXTO_SOFT);

            JLabel v = new JLabel(value);
            v.setFont(new Font("Inter", Font.BOLD, highlight ? 30 : 24));
            v.setForeground(color);

            add(t, BorderLayout.NORTH);
            add(v, BorderLayout.CENTER);

            if (title.contains("ESPERADO")) {
                lblEfectivoEsperado = v;
            }
        }
    }

    class ModernActionBtn extends JButton {

        public ModernActionBtn(String text, Color bg) {
            super(text);
            setForeground(Color.WHITE);
            setBackground(bg);
            setFont(FONT_BOLD);
            setPreferredSize(new Dimension(180, 45));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
