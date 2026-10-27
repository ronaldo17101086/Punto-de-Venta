package consultasVentas;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class ConsultasVentasInterfice extends JPanel {

    private JTabbedPane tabbedPane;
    private JTable tablaVentas;
    private DefaultTableModel modeloVentas;
    private JTextField txtBuscadorVentas;

    // Componentes de Fecha Interactivos
    private JButton btnSelectorFecha;
    private LocalDate fechaInicioFiltro = null;
    private LocalDate fechaFinFiltro = null;

    // Variables de Estado de Filtros Avanzados (Píldoras)
    private String filtroEstado = "Todos";
    private String filtroCredito = "Todos";
    private String filtroMoneda = "Todos";
    private String filtroFormaPago = "Todos";

    private JLabel lblTotalVentasMonto;
    private JLabel lblCantidadRegistrosVentas;

    private JTable tablaAbonos;
    private DefaultTableModel modeloAbonos;
    private JTextField txtBuscadorAbonos;
    private JLabel lblTotalAbonosMonto;

    @Autowired
    private DataSource dataSource;

    public ConsultasVentasInterfice() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // --- CABECERA PRINCIPAL ---
        JPanel panelCabecera = new JPanel(new BorderLayout());
        panelCabecera.setBackground(new Color(24, 110, 201));
        panelCabecera.setBorder(new EmptyBorder(14, 25, 14, 25));

        JLabel lblTitulo = new JLabel("Ventas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        panelCabecera.add(lblTitulo, BorderLayout.WEST);

        JButton btnHeaderRefresh = new JButton("↻");
        btnHeaderRefresh.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnHeaderRefresh.setForeground(Color.WHITE);
        btnHeaderRefresh.setContentAreaFilled(false);
        btnHeaderRefresh.setBorderPainted(false);
        btnHeaderRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHeaderRefresh.addActionListener(e -> dispararFiltroVentas());

        JPanel panelHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelHeaderRight.setOpaque(false);
        panelHeaderRight.add(btnHeaderRefresh);
        panelCabecera.add(panelHeaderRight, BorderLayout.EAST);

        // --- PESTAÑAS ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());

        tabbedPane.addTab("  Consulta de Ventas  ", crearPanelVentas());
        tabbedPane.addTab("  Consulta de Abonos  ", crearPanelAbonos());

        add(panelCabecera, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        dispararFiltroVentas();
        cargarAbonosBD("");
    }

    // =========================================================================
    // PANEL DE CONSULTA DE VENTAS
    // =========================================================================
 private JPanel crearPanelVentas() {
    JPanel panel = new JPanel(new BorderLayout(15, 15));
    panel.setBackground(new Color(245, 247, 250));
    panel.setBorder(new EmptyBorder(20, 20, 20, 20));

    // --- BARRA DE FILTROS SUPERIOR OPTIMIZADA (Buscador flexible) ---
    JPanel panelFiltros = new JPanel(new BorderLayout(15, 0));
    panelFiltros.setBackground(Color.WHITE);
    panelFiltros.putClientProperty(FlatClientProperties.STYLE, "arc:12; border:10,15,10,15,#E5E9F0,1,12");

    // Contenedor Izquierdo: Botón filtros y Actualizar (fijos)
    JPanel panelIzqAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    panelIzqAcciones.setOpaque(false);

    JButton btnFiltroMultiple = new JButton("🎛");
    btnFiltroMultiple.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    btnFiltroMultiple.setToolTipText("Filtros avanzados");
    btnFiltroMultiple.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#F1F5F9; borderWidth:0; margin:4,8,4,8");
    btnFiltroMultiple.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnFiltroMultiple.addActionListener(e -> mostrarDialogoFiltrosSicar((JFrame) SwingUtilities.getWindowAncestor(panel)));

    JButton btnActualizar = new JButton("Actualizar");
    btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
    btnActualizar.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#E2E8F0; borderWidth:0; margin:4,10,4,10");
    btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnActualizar.addActionListener(e -> {
        txtBuscadorVentas.setText("");
        fechaInicioFiltro = null;
        fechaFinFiltro = null;
        filtroEstado = "Todos";
        filtroCredito = "Todos";
        filtroMoneda = "Todos";
        filtroFormaPago = "Todos";
        btnSelectorFecha.setText("<html><center><font color='#94A3B8' size='2'>Fecha de creación</font><br><b>Todo el Historial</b></center></html>");
        dispararFiltroVentas();
    });

    panelIzqAcciones.add(btnFiltroMultiple);
    panelIzqAcciones.add(btnActualizar);

    // Contenedor Central: Buscador que SE EXPANDE ocupando todo el ancho disponible
    JPanel panelBuscadorCentro = new JPanel(new BorderLayout());
    panelBuscadorCentro.setOpaque(false);
    
    txtBuscadorVentas = new JTextField();
    txtBuscadorVentas.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar por Folio o Cliente...");
    txtBuscadorVentas.putClientProperty(FlatClientProperties.STYLE, "arc:8; margin:4,10,4,10");
    txtBuscadorVentas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    txtBuscadorVentas.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
            dispararFiltroVentas();
        }
    });
    panelBuscadorCentro.add(txtBuscadorVentas, BorderLayout.CENTER);

    // Unimos acciones izquierdas y buscador central en un panel WEST
    JPanel panelWestUnificado = new JPanel(new BorderLayout(8, 0));
    panelWestUnificado.setOpaque(false);
    panelWestUnificado.add(panelIzqAcciones, BorderLayout.WEST);
    panelWestUnificado.add(panelBuscadorCentro, BorderLayout.CENTER);

    // Contenedor Derecho: Selector de Fecha pegado a la derecha
    JPanel panelDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    panelDer.setOpaque(false);
    
    btnSelectorFecha = new JButton("<html><center><font color='#94A3B8' size='2'>Fecha de creación</font><br><b>Todo el Historial</b></center></html>");
    btnSelectorFecha.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    btnSelectorFecha.setHorizontalAlignment(SwingConstants.LEFT);
    btnSelectorFecha.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#F8FAFC; borderWidth:1; borderColor:#CBD5E1; margin:2,10,2,10");
    btnSelectorFecha.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnSelectorFecha.addActionListener(e -> mostrarSelectorCalendario((JFrame) SwingUtilities.getWindowAncestor(panel)));

    panelDer.add(btnSelectorFecha);

    panelFiltros.add(panelWestUnificado, BorderLayout.CENTER);
    panelFiltros.add(panelDer, BorderLayout.EAST);

    // --- TABLA DE VENTAS ---
    String[] columnas = {"Serie-Folio", "Tipo Pago", "Cliente", "Comentarios", "Total"};
    modeloVentas = new DefaultTableModel(columnas, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };

    tablaVentas = new JTable(modeloVentas);
    tablaVentas.setRowHeight(52); 
    tablaVentas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    tablaVentas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    tablaVentas.getTableHeader().setForeground(new Color(100, 116, 139));
    tablaVentas.getTableHeader().setPreferredSize(new Dimension(0, 38));
    tablaVentas.setSelectionBackground(new Color(238, 242, 255));
    tablaVentas.setSelectionForeground(new Color(30, 41, 59));
    tablaVentas.setShowVerticalLines(false);
    tablaVentas.setGridColor(new Color(241, 245, 249));

    tablaVentas.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Object[]) {
                Object[] datos = (Object[]) value;
                setText("<html><b>" + datos[0] + "</b><br><font color='#94A3B8' size='2'>" + datos[1] + "</font></html>");
            } else {
                setText(value != null ? value.toString() : "");
            }
            setHorizontalAlignment(JLabel.LEFT);
            setBorder(new EmptyBorder(0, 10, 0, 0));
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(Color.WHITE);
                setForeground(new Color(15, 23, 42));
            }
            return this;
        }
    });

    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
    rightRenderer.setBorder(new EmptyBorder(0, 0, 0, 15));
    tablaVentas.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

    tablaVentas.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int fila = tablaVentas.getSelectedRow();
                if (fila != -1) {
                    Long ventaId = (Long) tablaVentas.getClientProperty("id_venta_" + fila);
                    Object valFolio = modeloVentas.getValueAt(fila, 0);
                    String folioStr = valFolio instanceof Object[] ? ((Object[]) valFolio)[0].toString() : valFolio.toString();
                    String cliente = modeloVentas.getValueAt(fila, 2).toString();
                    String total = modeloVentas.getValueAt(fila, 4).toString();
                    mostrarDetalleVenta((JFrame) SwingUtilities.getWindowAncestor(panel), ventaId, folioStr, cliente, total);
                }
            }
        }
    });

    JScrollPane scrollTabla = new JScrollPane(tablaVentas);
    scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
    scrollTabla.getViewport().setBackground(Color.WHITE);

    // --- BARRA INFERIOR DE TOTALES ---
    JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
    panelInferior.setBackground(Color.WHITE);
    panelInferior.putClientProperty(FlatClientProperties.STYLE, "arc:12; border:10,15,10,15,#E5E9F0,1,12");

    lblCantidadRegistrosVentas = new JLabel("Registros encontrados: 0");
    lblCantidadRegistrosVentas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblCantidadRegistrosVentas.setForeground(new Color(100, 116, 139));

    lblTotalVentasMonto = new JLabel("Monto Total Filtrado: $0.00 MXN");
    lblTotalVentasMonto.setFont(new Font("Segoe UI", Font.BOLD, 15));
    lblTotalVentasMonto.setForeground(new Color(16, 185, 129));

    panelInferior.add(lblCantidadRegistrosVentas);
    panelInferior.add(new JLabel(" | "));
    panelInferior.add(lblTotalVentasMonto);

    panel.add(panelFiltros, BorderLayout.NORTH);
    panel.add(scrollTabla, BorderLayout.CENTER);
    panel.add(panelInferior, BorderLayout.SOUTH);

    return panel;
}

    // =========================================================================
    // MODAL DE FILTROS AVANZADOS (PÍLDORAS)
    // =========================================================================
    private void mostrarDialogoFiltrosSicar(JFrame parent) {
        JDialog dialogo = new JDialog(parent, "Filtros Avanzados de Ventas", true);
        dialogo.setSize(520, 440);
        dialogo.setLocationRelativeTo(parent);
        dialogo.setLayout(new BorderLayout(10, 10));

        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBorder(new EmptyBorder(15, 20, 15, 20));
        panelContenido.setBackground(Color.WHITE);

        final String[] estadoTemp = {filtroEstado};
        final String[] creditoTemp = {filtroCredito};
        final String[] monedaTemp = {filtroMoneda};
        final String[] formaPagoTemp = {filtroFormaPago};

        panelContenido.add(crearFilaFiltroPildoras("Estado", new String[]{"Todos", "Vigente", "Cancelado"}, estadoTemp));
        panelContenido.add(Box.createVerticalStrut(12));
        panelContenido.add(crearFilaFiltroPildoras("Crédito", new String[]{"Todos", "Sí", "No"}, creditoTemp));
        panelContenido.add(Box.createVerticalStrut(12));
        panelContenido.add(crearFilaFiltroPildoras("Moneda", new String[]{"Todos", "MXN", "USD"}, monedaTemp));
        panelContenido.add(Box.createVerticalStrut(12));
        panelContenido.add(crearFilaFiltroPildoras("Forma de pago", new String[]{"Todos", "Efectivo", "Tarjeta", "Transferencia"}, formaPagoTemp));

        JScrollPane scrollFiltros = new JScrollPane(panelContenido);
        scrollFiltros.setBorder(BorderFactory.createEmptyBorder());

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnLimpiar.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#F1F5F9; borderWidth:0");
        btnLimpiar.addActionListener(e -> {
            estadoTemp[0] = "Todos";
            creditoTemp[0] = "Todos";
            monedaTemp[0] = "Todos";
            formaPagoTemp[0] = "Todos";
            dialogo.dispose();
        });

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBuscar.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#186EC9; foreground:#FFFFFF; borderWidth:0");
        btnBuscar.addActionListener(e -> {
            filtroEstado = estadoTemp[0];
            filtroCredito = creditoTemp[0];
            filtroMoneda = monedaTemp[0];
            filtroFormaPago = formaPagoTemp[0];
            dialogo.dispose();
            dispararFiltroVentas();
        });

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelSur.setBackground(new Color(245, 247, 250));
        panelSur.add(btnLimpiar);
        panelSur.add(btnBuscar);

        dialogo.add(scrollFiltros, BorderLayout.CENTER);
        dialogo.add(panelSur, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }

    private JPanel crearFilaFiltroPildoras(String etiquetaTitulo, String[] opciones, final String[] seleccionActual) {
        JPanel panelFila = new JPanel(new BorderLayout(5, 5));
        panelFila.setOpaque(false);

        JLabel lbl = new JLabel(etiquetaTitulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        panelFila.add(lbl, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelBotones.setOpaque(false);

        List<JButton> botonesPildora = new ArrayList<>();

        for (String opc : opciones) {
            JButton btnOpc = new JButton(opc);
            btnOpc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnOpc.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (opc.equals(seleccionActual[0])) {
                btnOpc.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:#186EC9; foreground:#FFFFFF; borderWidth:0; margin:3,10,3,10");
            } else {
                btnOpc.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:#F1F5F9; foreground:#334155; borderWidth:0; margin:3,10,3,10");
            }

            btnOpc.addActionListener(e -> {
                seleccionActual[0] = opc;
                for (JButton b : botonesPildora) {
                    if (b.getText().equals(opc)) {
                        b.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:#186EC9; foreground:#FFFFFF; borderWidth:0; margin:3,10,3,10");
                    } else {
                        b.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:#F1F5F9; foreground:#334155; borderWidth:0; margin:3,10,3,10");
                    }
                    b.repaint();
                }
            });

            botonesPildora.add(btnOpc);
            panelBotones.add(btnOpc);
        }

        panelFila.add(panelBotones, BorderLayout.CENTER);
        return panelFila;
    }

    // =========================================================================
    // SELECTOR DE CALENDARIO INTERACTIVO
    // =========================================================================
    private void mostrarSelectorCalendario(JFrame parent) {
        JDialog dialogoCal = new JDialog(parent, "Seleccionar Rango de Fechas", true);
        dialogoCal.setSize(420, 260);
        dialogoCal.setLocationRelativeTo(parent);
        dialogoCal.setLayout(new BorderLayout(10, 10));

        JPanel panelForm = new JPanel(new GridLayout(3, 2, 10, 15));
        panelForm.setBorder(new EmptyBorder(25, 25, 10, 25));
        panelForm.setBackground(Color.WHITE);

        JLabel lblDesde = new JLabel("Fecha Inicio (AAAA-MM-DD):");
        lblDesde.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField txtFechaInicio = new JTextField(LocalDate.now().minusDays(7).toString());
        txtFechaInicio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtFechaInicio.putClientProperty(FlatClientProperties.STYLE, "arc:8");

        JLabel lblHasta = new JLabel("Fecha Fin (AAAA-MM-DD):");
        lblHasta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField txtFechaFin = new JTextField(LocalDate.now().toString());
        txtFechaFin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtFechaFin.putClientProperty(FlatClientProperties.STYLE, "arc:8");

        panelForm.add(lblDesde);
        panelForm.add(txtFechaInicio);
        panelForm.add(lblHasta);
        panelForm.add(txtFechaFin);

        JButton btnSeleccionarRango = new JButton("Aplicar Rango");
        btnSeleccionarRango.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSeleccionarRango.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#186EC9; foreground:#FFFFFF; borderWidth:0");
        btnSeleccionarRango.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSeleccionarRango.addActionListener(e -> {
            try {
                fechaInicioFiltro = LocalDate.parse(txtFechaInicio.getText().trim());
                fechaFinFiltro = LocalDate.parse(txtFechaFin.getText().trim());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                String textoRango = formatter.format(fechaInicioFiltro) + " - " + formatter.format(fechaFinFiltro);

                btnSelectorFecha.setText("<html><center><font color='#94A3B8' size='2'>Fecha de creación</font><br><b>📅 " + textoRango + "</b></center></html>");
                dialogoCal.dispose();
                dispararFiltroVentas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogoCal, "Formato de fecha inválido. Utilice AAAA-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panelSur.setBackground(new Color(245, 247, 250));
        panelSur.add(btnSeleccionarRango);

        dialogoCal.add(panelForm, BorderLayout.CENTER);
        dialogoCal.add(panelSur, BorderLayout.SOUTH);
        dialogoCal.setVisible(true);
    }

    // =========================================================================
    // PANEL DE CONSULTA DE ABONOS
    // =========================================================================
    private JPanel crearPanelAbonos() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel panelFiltros = new JPanel(new BorderLayout());
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.putClientProperty(FlatClientProperties.STYLE, "arc:12; border:12,15,12,15,#E5E9F0,1,12");

        JPanel panelIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelIzq.setOpaque(false);

        txtBuscadorAbonos = new JTextField(30);
        txtBuscadorAbonos.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar abono por cliente o concepto...");
        txtBuscadorAbonos.putClientProperty(FlatClientProperties.STYLE, "arc:8; margin:4,10,4,10");
        txtBuscadorAbonos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscadorAbonos.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                cargarAbonosBD(txtBuscadorAbonos.getText().trim());
            }
        });

        JLabel lblBuscadorAbonos = new JLabel("Buscador de Abonos:");
        lblBuscadorAbonos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBuscadorAbonos.setForeground(new Color(100, 116, 139));

        panelIzq.add(lblBuscadorAbonos);
        panelIzq.add(txtBuscadorAbonos);
        panelFiltros.add(panelIzq, BorderLayout.WEST);

        String[] columnasAbonos = {"ID Movimiento", "Fecha / Hora", "Cliente / Concepto", "Monto Abonado"};
        modeloAbonos = new DefaultTableModel(columnasAbonos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaAbonos = new JTable(modeloAbonos);
        tablaAbonos.setRowHeight(44);
        tablaAbonos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaAbonos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaAbonos.getTableHeader().setForeground(new Color(100, 116, 139));
        tablaAbonos.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tablaAbonos.setSelectionBackground(new Color(238, 242, 255));
        tablaAbonos.setShowVerticalLines(false);
        tablaAbonos.setGridColor(new Color(241, 245, 249));

        JScrollPane scrollAbonos = new JScrollPane(tablaAbonos);
        scrollAbonos.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollAbonos.getViewport().setBackground(Color.WHITE);

        JPanel panelInferiorAbonos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        panelInferiorAbonos.setBackground(Color.WHITE);
        panelInferiorAbonos.putClientProperty(FlatClientProperties.STYLE, "arc:12; border:10,15,10,15,#E5E9F0,1,12");

        lblTotalAbonosMonto = new JLabel("Total Abonos Registrados: $0.00 MXN");
        lblTotalAbonosMonto.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalAbonosMonto.setForeground(new Color(16, 185, 129));
        panelInferiorAbonos.add(lblTotalAbonosMonto);

        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(scrollAbonos, BorderLayout.CENTER);
        panel.add(panelInferiorAbonos, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // LÓGICA DE FILTRADO Y CONSULTAS (SIN COLUMNA CFDI)
    // =========================================================================
    private void dispararFiltroVentas() {
        String texto = txtBuscadorVentas.getText().trim();
        cargarVentasPersonalizado(texto);
    }

    private void cargarVentasPersonalizado(String filtro) {
        if (dataSource == null) {
            return;
        }
        modeloVentas.setRowCount(0);

        StringBuilder sql = new StringBuilder(
                "SELECT v.id, v.uuid_venta, v.fecha_venta, v.hora_venta, "
                + "COALESCE(c.nombre, 'Público En General') AS cliente_nombre, "
                + "v.tipo_pago, COALESCE(v.comentarios, '-') AS comentarios, v.total "
                + "FROM ventas v LEFT JOIN clientes c ON v.cliente_id = c.id WHERE 1=1 "
        );

        if (filtro != null && !filtro.isEmpty()) {
            sql.append("AND (v.uuid_venta LIKE ? OR c.nombre LIKE ? OR v.id LIKE ?) ");
        }

        if (fechaInicioFiltro != null && fechaFinFiltro != null) {
            sql.append("AND v.fecha_venta BETWEEN ? AND ? ");
        }

        if (!filtroCredito.equals("Todos")) {
            if (filtroCredito.equals("Sí")) {
                sql.append("AND v.es_credito = 1 ");
            } else {
                sql.append("AND (v.es_credito = 0 OR v.es_credito IS NULL) ");
            }
        }
        if (!filtroFormaPago.equals("Todos")) {
            sql.append("AND v.tipo_pago = ? ");
        }

        sql.append("ORDER BY v.id DESC LIMIT 300");

        BigDecimal acumuladoTotal = BigDecimal.ZERO;
        int contadorRegistros = 0;

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (filtro != null && !filtro.isEmpty()) {
                pstmt.setString(index++, "%" + filtro + "%");
                pstmt.setString(index++, "%" + filtro + "%");
                pstmt.setString(index++, "%" + filtro + "%");
            }
            if (fechaInicioFiltro != null && fechaFinFiltro != null) {
                pstmt.setDate(index++, java.sql.Date.valueOf(fechaInicioFiltro));
                pstmt.setDate(index++, java.sql.Date.valueOf(fechaFinFiltro));
            }
            if (!filtroFormaPago.equals("Todos")) {
                pstmt.setString(index++, filtroFormaPago);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal montoVenta = rs.getBigDecimal("total");
                    if (montoVenta != null) {
                        acumuladoTotal = acumuladoTotal.add(montoVenta);
                    }

                    Long idVenta = rs.getLong("id");
                    String folio = rs.getString("uuid_venta");
                    if (folio == null || folio.isEmpty()) {
                        folio = String.valueOf(idVenta);
                    }

                    String fechaHora = rs.getDate("fecha_venta") + " " + rs.getTime("hora_venta");
                    String tipoPago = rs.getString("tipo_pago");
                    String cliente = rs.getString("cliente_nombre");
                    String comentarios = rs.getString("comentarios");

                    int filaActual = modeloVentas.getRowCount();
                    tablaVentas.putClientProperty("id_venta_" + filaActual, idVenta);

                    modeloVentas.addRow(new Object[]{
                        new Object[]{folio, fechaHora},
                        tipoPago,
                        cliente,
                        comentarios,
                        "$" + (montoVenta != null ? montoVenta.toPlainString() : "0.00") + " MXN"
                    });
                    contadorRegistros++;
                }
            }

            lblCantidadRegistrosVentas.setText("Registros encontrados: " + contadorRegistros);
            lblTotalVentasMonto.setText("Monto Total Filtrado: $" + acumuladoTotal.toPlainString() + " MXN");

        } catch (SQLException e) {
            System.err.println("Error al cargar ventas: " + e.getMessage());
        }
    }

    private void cargarAbonosBD(String filtro) {
        if (dataSource == null) {
            return;
        }
        modeloAbonos.setRowCount(0);

        String sql = "SELECT m.id, m.fecha, m.concepto, m.monto FROM movimientos_caja m WHERE m.tipo = 'DEVOLUCION' OR m.concepto LIKE '%abono%' ";
        if (filtro != null && !filtro.isEmpty()) {
            sql += "AND m.concepto LIKE ? ";
        }
        sql += "ORDER BY m.id DESC LIMIT 300";

        BigDecimal acumuladoAbonos = BigDecimal.ZERO;

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (filtro != null && !filtro.isEmpty()) {
                pstmt.setString(1, "%" + filtro + "%");
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal montoAbono = rs.getBigDecimal("monto");
                    if (montoAbono != null) {
                        acumuladoAbonos = acumuladoAbonos.add(montoAbono);
                    }
                    modeloAbonos.addRow(new Object[]{
                        rs.getLong("id"),
                        rs.getTimestamp("fecha"),
                        rs.getString("concepto"),
                        "$" + (montoAbono != null ? montoAbono.toPlainString() : "0.00") + " MXN"
                    });
                }
            }
            lblTotalAbonosMonto.setText("Total Abonos Registrados: $" + acumuladoAbonos.toPlainString() + " MXN");
        } catch (SQLException e) {
            System.err.println("Error al cargar abonos: " + e.getMessage());
        }
    }

    // =========================================================================
    // MODAL DE DETALLE DE VENTA ESTILO SICARX
    // =========================================================================
private void mostrarDetalleVenta(JFrame parent, Long ventaId, String folio, String cliente, String total) {
    JDialog dialogoDetalle = new JDialog(parent, "Venta " + folio, true);
    dialogoDetalle.setSize(850, 580);
    dialogoDetalle.setLocationRelativeTo(parent);
    dialogoDetalle.setLayout(new BorderLayout());
    dialogoDetalle.getContentPane().setBackground(new Color(245, 247, 250));

    // --- BARRA SUPERIOR DE ACCIONES (Con opción de Cancelar Venta) ---
    JPanel panelToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
    panelToolbar.setBackground(Color.WHITE);
    panelToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

    JButton btnFacturar = new JButton("📄 Facturar");
    JButton btnCliente = new JButton("👤 Cliente");
    JButton btnImprimir = new JButton("🖨️ Imprimir");
    JButton btnPdf = new JButton("📥 PDF");
    
    // Botón de Cancelar Venta estilizado en rojo claro
    JButton btnCancelarVenta = new JButton("🗑️ Cancelar Venta");
    btnCancelarVenta.setForeground(new Color(220, 38, 38));
    btnCancelarVenta.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#FEF2F2; borderWidth:1; borderColor:#FCA5A5; margin:4,10,4,10");
    btnCancelarVenta.setCursor(new Cursor(Cursor.HAND_CURSOR));

    btnCancelarVenta.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(
            dialogoDetalle, 
            "¿Estás seguro de que deseas cancelar la venta con folio " + folio + "?", 
            "Confirmar Cancelación", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sqlUpdate = "UPDATE ventas SET estado = 'CANCELADO' WHERE id = ?";
            try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setLong(1, ventaId);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(dialogoDetalle, "Venta cancelada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dialogoDetalle.dispose();
                dispararFiltroVentas(); // Refresca la tabla principal
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialogoDetalle, "Error al cancelar la venta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });

    for (JButton btn : new JButton[]{btnFacturar, btnCliente, btnImprimir, btnPdf, btnCancelarVenta}) {
        if (btn != btnCancelarVenta) {
            btn.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#F8FAFC; borderWidth:1; borderColor:#CBD5E1; margin:4,10,4,10");
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        panelToolbar.add(btn);
    }

    // --- PANEL DE INFORMACIÓN GENERAL ---
    JPanel panelInfoCliente = new JPanel(new BorderLayout());
    panelInfoCliente.setBackground(Color.WHITE);
    panelInfoCliente.setBorder(new EmptyBorder(15, 20, 15, 20));

    JLabel lblDatosCliente = new JLabel("<html><b>Cliente:</b> " + cliente + " &nbsp;&nbsp;|&nbsp;&nbsp; <b>Folio:</b> " + folio + " &nbsp;&nbsp;|&nbsp;&nbsp; <b>Total:</b> <font color='#16A34A'><b>" + total + "</b></font></html>");
    lblDatosCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    panelInfoCliente.add(lblDatosCliente, BorderLayout.CENTER);

    JPanel panelNorteContenedor = new JPanel(new BorderLayout());
    panelNorteContenedor.add(panelToolbar, BorderLayout.NORTH);
    panelNorteContenedor.add(panelInfoCliente, BorderLayout.SOUTH);

    // --- TABLA DE PRODUCTOS DEL DETALLE ---
    String[] colDetalle = {"Cantidad", "Descripción del Producto", "Precio Unitario"};
    DefaultTableModel modeloDetalle = new DefaultTableModel(colDetalle, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };

    JTable tablaDetalle = new JTable(modeloDetalle);
    tablaDetalle.setRowHeight(56);
    tablaDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    tablaDetalle.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    tablaDetalle.getTableHeader().setForeground(new Color(100, 116, 139));
    tablaDetalle.getTableHeader().setPreferredSize(new Dimension(0, 38));
    tablaDetalle.setShowVerticalLines(false);
    tablaDetalle.setGridColor(new Color(241, 245, 249));

    tablaDetalle.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String nombreProd = value != null ? value.toString() : "";
            setText("<html><div style='display: flex; align-items: center;'>"
                    + "<span style='background-color:#0284C7; color:white; padding:4px 8px; border-radius:4px; font-weight:bold; margin-right:8px;'>SKU</span>"
                    + "<span style='font-weight:500; color:#1E293B;'>" + nombreProd + "</span>"
                    + "</div></html>");
            setBorder(new EmptyBorder(0, 10, 0, 0));
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    });

    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
    rightRenderer.setBorder(new EmptyBorder(0, 0, 0, 15));
    tablaDetalle.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

    String sqlDetalle = "SELECT vp.cantidad, p.name AS producto_nombre, p.price FROM venta_productos vp "
                      + "JOIN productos p ON vp.producto_id = p.id WHERE vp.registro_venta_id = ?";

    try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlDetalle)) {
        pstmt.setLong(1, ventaId);
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                modeloDetalle.addRow(new Object[]{
                    rs.getBigDecimal("cantidad") + " PZA",
                    rs.getString("producto_nombre"),
                    "$" + rs.getBigDecimal("price")
                });
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al cargar detalle de venta: " + e.getMessage());
    }

    JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
    scrollDetalle.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    scrollDetalle.getViewport().setBackground(Color.WHITE);

    JButton btnCerrar = new JButton("Cerrar");
    btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
    btnCerrar.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#E2E8F0; borderWidth:0; margin:6,16,6,16");
    btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnCerrar.addActionListener(e -> dialogoDetalle.dispose());

    JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
    panelSur.setBackground(new Color(245, 247, 250));
    panelSur.add(btnCerrar);

    dialogoDetalle.add(panelNorteContenedor, BorderLayout.NORTH);
    dialogoDetalle.add(scrollDetalle, BorderLayout.CENTER);
    dialogoDetalle.add(panelSur, BorderLayout.SOUTH);

    dialogoDetalle.setVisible(true);
}
}
