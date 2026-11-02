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

    private LocalDate tempFechaInicio = LocalDate.now().minusDays(7);
    private LocalDate tempFechaFin = LocalDate.now();

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

    private JPanel crearPanelVentas() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- BARRA DE FILTROS SUPERIOR OPTIMIZADA ---
        JPanel panelFiltros = new JPanel(new BorderLayout(15, 0));
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.putClientProperty(FlatClientProperties.STYLE, "arc:12; border:10,15,10,15,#E5E9F0,1,12");

        // Contenedor Izquierdo: Botón filtros y Actualizar (fijos)
        JPanel panelIzqAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelIzqAcciones.setOpaque(false);

        JButton btnFiltroMultiple = new JButton("Filtros");
        btnFiltroMultiple.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFiltroMultiple.setToolTipText("Filtros avanzados");
        btnFiltroMultiple.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#F1F5F9; foreground:#334155; borderWidth:0; margin:4,10,4,10");
        btnFiltroMultiple.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFiltroMultiple.addActionListener(e -> mostrarDialogoFiltrosSicar((JFrame) SwingUtilities.getWindowAncestor(panel)));

        // Botón Actualizar: Mantiene los filtros activos y recarga la tabla
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnActualizar.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#E2E8F0; borderWidth:0; margin:4,10,4,10");
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> {
            dispararFiltroVentas();
        });

        panelIzqAcciones.add(btnFiltroMultiple);
        panelIzqAcciones.add(btnActualizar);

        // Contenedor Central: Buscador LARGO y expansible
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

        // Panel de píldoras de filtros activos al lado del buscador
        JPanel panelPildorasActivas = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelPildorasActivas.setOpaque(false);
        panelFiltros.putClientProperty("panelPildorasActivas", panelPildorasActivas);

        // Agrupamos el buscador en el CENTER para que se expanda al máximo
        JPanel panelBuscadorYPildoras = new JPanel(new BorderLayout(10, 0));
        panelBuscadorYPildoras.setOpaque(false);
        panelBuscadorYPildoras.add(txtBuscadorVentas, BorderLayout.CENTER); // Se estira de forma fluida
        panelBuscadorYPildoras.add(panelPildorasActivas, BorderLayout.EAST);   // Las píldoras se acomodan a la derecha del texto

        // Unimos acciones izquierdas y el bloque central expansivo
        JPanel panelWestUnificado = new JPanel(new BorderLayout(10, 0));
        panelWestUnificado.setOpaque(false);
        panelWestUnificado.add(panelIzqAcciones, BorderLayout.WEST);
        panelWestUnificado.add(panelBuscadorYPildoras, BorderLayout.CENTER);

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
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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

        actualizarPildorasFiltrosActivos(panelPildorasActivas);

        return panel;
    }

    private void actualizarPildorasFiltrosActivos(JPanel panelPildoras) {
        if (panelPildoras == null) {
            return;
        }
        panelPildoras.removeAll();

        // Creamos una píldora por cada filtro activo (que sea diferente de "Todos")
        if (!filtroEstado.equals("Todos")) {
            panelPildoras.add(crearPildoraFiltroActivo("Estado: " + filtroEstado, () -> {
                filtroEstado = "Todos";
                dispararFiltroVentas();
                actualizarPildorasFiltrosActivos(panelPildoras);
            }));
        }
        if (!filtroCredito.equals("Todos")) {
            panelPildoras.add(crearPildoraFiltroActivo("Crédito: " + filtroCredito, () -> {
                filtroCredito = "Todos";
                dispararFiltroVentas();
                actualizarPildorasFiltrosActivos(panelPildoras);
            }));
        }
        if (!filtroMoneda.equals("Todos")) {
            panelPildoras.add(crearPildoraFiltroActivo("Moneda: " + filtroMoneda, () -> {
                filtroMoneda = "Todos";
                dispararFiltroVentas();
                actualizarPildorasFiltrosActivos(panelPildoras);
            }));
        }
        if (!filtroFormaPago.equals("Todos")) {
            panelPildoras.add(crearPildoraFiltroActivo("Pago: " + filtroFormaPago, () -> {
                filtroFormaPago = "Todos";
                dispararFiltroVentas();
                actualizarPildorasFiltrosActivos(panelPildoras);
            }));
        }
        if (fechaInicioFiltro != null && fechaFinFiltro != null) {
            panelPildoras.add(crearPildoraFiltroActivo("Rango Fechas", () -> {
                fechaInicioFiltro = null;
                fechaFinFiltro = null;
                btnSelectorFecha.setText("<html><center><font color='#94A3B8' size='2'>Fecha de creación</font><br><b>Todo el Historial</b></center></html>");
                dispararFiltroVentas();
                actualizarPildorasFiltrosActivos(panelPildoras);
            }));
        }

        panelPildoras.revalidate();
        panelPildoras.repaint();
    }

    private JButton crearPildoraFiltroActivo(String texto, Runnable onRemover) {
        JButton btnPildora = new JButton(texto + "  ✕");
        btnPildora.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnPildora.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPildora.putClientProperty(FlatClientProperties.STYLE, "arc:12; background:#EFF6FF; foreground:#1E40AF; borderWidth:1; borderColor:#BFDBFE; margin:2,8,2,8");
        btnPildora.addActionListener(e -> onRemover.run());
        return btnPildora;
    }

    private void mostrarDialogoFiltrosSicar(JFrame parent) {
        JDialog dialogo = new JDialog(parent, "Filtros Avanzados", true);
        dialogo.setSize(540, 610);
        dialogo.setLocationRelativeTo(parent);
        dialogo.setLayout(new BorderLayout());
        dialogo.getContentPane().setBackground(new Color(248, 250, 252));

        // --- CABECERA MODERNA CON ACENTO VISUAL ---
        JPanel panelCabeceraModal = new JPanel(new BorderLayout());
        panelCabeceraModal.setBackground(Color.WHITE);
        panelCabeceraModal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JLabel lblTituloModal = new JLabel("<html><div style='color: #0F172A; font-size: 14px; font-weight: bold;'>Filtros Avanzados de Ventas</div>"
                + "<div style='color: #64748B; font-size: 11px; margin-top: 2px;'>Selecciona los criterios para acotar los resultados</div></html>");
        panelCabeceraModal.add(lblTituloModal, BorderLayout.CENTER);

        // Contenedor principal de tarjetas de filtro
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBorder(new EmptyBorder(16, 20, 16, 20));
        panelContenido.setBackground(new Color(248, 250, 252));

        final String[] estadoTemp = {filtroEstado};
        final String[] creditoTemp = {filtroCredito};
        final String[] monedaTemp = {filtroMoneda};
        final String[] formaPagoTemp = {filtroFormaPago};

        // Creamos referencias para poder limpiar visualmente los botones al hacer clic en "Limpiar"
        List<Runnable> accionesLimpiarVisual = new ArrayList<>();

        JPanel filaEstado = crearFilaFiltroPildoras("Estado de Venta", new String[]{"Todos", "Vigente", "Cancelado"}, estadoTemp, accionesLimpiarVisual);
        JPanel filaCredito = crearFilaFiltroPildoras("Tipo de Crédito", new String[]{"Todos", "Sí", "No"}, creditoTemp, accionesLimpiarVisual);
        JPanel filaMoneda = crearFilaFiltroPildoras("Moneda", new String[]{"Todos", "MXN", "USD"}, monedaTemp, accionesLimpiarVisual);
        JPanel filaFormaPago = crearFilaFiltroPildoras("Forma de Pago", new String[]{"Todos", "Efectivo", "Tarjeta", "Transferencia"}, formaPagoTemp, accionesLimpiarVisual);

        panelContenido.add(filaEstado);
        panelContenido.add(Box.createVerticalStrut(12));
        panelContenido.add(filaCredito);
        panelContenido.add(Box.createVerticalStrut(12));
        panelContenido.add(filaMoneda);
        panelContenido.add(Box.createVerticalStrut(12));
        panelContenido.add(filaFormaPago);

        JScrollPane scrollFiltros = new JScrollPane(panelContenido);
        scrollFiltros.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltros.getViewport().setBackground(new Color(248, 250, 252));
        scrollFiltros.getVerticalScrollBar().setUnitIncrement(16);

        // --- BOTONES INFERIORES ESTILIZADOS ---
        JButton btnLimpiar = new JButton("Restablecer");
        btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLimpiar.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#F1F5F9; foreground:#475569; borderWidth:0; margin:8,18,8,18");
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ACCIÓN LIMPIAR REAL: Resetea variables y actualiza los botones visualmente al instante
        btnLimpiar.addActionListener(e -> {
            estadoTemp[0] = "Todos";
            creditoTemp[0] = "Todos";
            monedaTemp[0] = "Todos";
            formaPagoTemp[0] = "Todos";

            // Ejecuta la limpieza visual de cada grupo de píldoras
            for (Runnable limpiarUI : accionesLimpiarVisual) {
                limpiarUI.run();
            }
        });

        JButton btnBuscar = new JButton("Aplicar Filtros");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuscar.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#186EC9; foreground:#FFFFFF; borderWidth:0; margin:8,22,8,22");
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> {
            filtroEstado = estadoTemp[0];
            filtroCredito = creditoTemp[0];
            filtroMoneda = monedaTemp[0];
            filtroFormaPago = formaPagoTemp[0];
            dialogo.dispose();
            dispararFiltroVentas();
        });

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        panelSur.setBackground(Color.WHITE);
        panelSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        panelSur.add(btnLimpiar);
        panelSur.add(btnBuscar);

        dialogo.add(panelCabeceraModal, BorderLayout.NORTH);
        dialogo.add(scrollFiltros, BorderLayout.CENTER);
        dialogo.add(panelSur, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }

    private JPanel crearFilaFiltroPildoras(String etiquetaTitulo, String[] opciones, final String[] seleccionActual, List<Runnable> listaLimpieza) {
        JPanel panelFila = new JPanel(new BorderLayout(0, 8));
        panelFila.setBackground(Color.WHITE);
        panelFila.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        panelFila.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        JLabel lbl = new JLabel(etiquetaTitulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(30, 41, 59));
        panelFila.add(lbl, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelBotones.setOpaque(false);

        List<JButton> botonesPildora = new ArrayList<>();

        for (String opc : opciones) {
            JButton btnOpc = new JButton(opc);
            btnOpc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnOpc.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (opc.equals(seleccionActual[0])) {
                btnOpc.putClientProperty(FlatClientProperties.STYLE, "arc:18; background:#186EC9; foreground:#FFFFFF; borderWidth:0; margin:5,14,5,14");
            } else {
                btnOpc.putClientProperty(FlatClientProperties.STYLE, "arc:18; background:#F8FAFC; foreground:#475569; borderWidth:1; borderColor:#CBD5E1; margin:5,14,5,14");
            }

            btnOpc.addActionListener(e -> {
                seleccionActual[0] = opc;
                for (JButton b : botonesPildora) {
                    if (b.getText().equals(opc)) {
                        b.putClientProperty(FlatClientProperties.STYLE, "arc:18; background:#186EC9; foreground:#FFFFFF; borderWidth:0; margin:5,14,5,14");
                    } else {
                        b.putClientProperty(FlatClientProperties.STYLE, "arc:18; background:#F8FAFC; foreground:#475569; borderWidth:1; borderColor:#CBD5E1; margin:5,14,5,14");
                    }
                    b.repaint();
                }
            });

            botonesPildora.add(btnOpc);
            panelBotones.add(btnOpc);
        }

        // Añadimos la regla para que el botón "Restablecer" devuelva visualmente la selección a "Todos"
        listaLimpieza.add(() -> {
            seleccionActual[0] = "Todos";
            for (JButton b : botonesPildora) {
                if (b.getText().equals("Todos")) {
                    b.putClientProperty(FlatClientProperties.STYLE, "arc:18; background:#186EC9; foreground:#FFFFFF; borderWidth:0; margin:5,14,5,14");
                } else {
                    b.putClientProperty(FlatClientProperties.STYLE, "arc:18; background:#F8FAFC; foreground:#475569; borderWidth:1; borderColor:#CBD5E1; margin:5,14,5,14");
                }
                b.repaint();
            }
        });

        panelFila.add(panelBotones, BorderLayout.CENTER);
        return panelFila;
    }

    private void mostrarSelectorCalendario(JFrame parent) {
        JDialog dialogoCal = new JDialog(parent, "Seleccionar Rango de Fechas", true);
        dialogoCal.setSize(620, 460); // Ampliamos el ancho de la ventana
        dialogoCal.setLocationRelativeTo(parent);
        dialogoCal.setLayout(new BorderLayout());
        dialogoCal.getContentPane().setBackground(new Color(248, 250, 252));

        if (fechaInicioFiltro != null) {
            tempFechaInicio = fechaInicioFiltro;
        }
        if (fechaFinFiltro != null) {
            tempFechaFin = fechaFinFiltro;
        }

        // --- CABECERA ---
        JPanel panelCabecera = new JPanel(new BorderLayout());
        panelCabecera.setBackground(Color.WHITE);
        panelCabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(16, 20, 16, 20)
        ));
        JLabel lblTitulo = new JLabel("<html><b style='color: #0F172A; font-size: 14px;'>Selecciona el Rango de Fechas</b><br><font color='#64748B' size='2'>Elige el mes y año directamente o haz clic en el día</font></html>");
        panelCabecera.add(lblTitulo, BorderLayout.CENTER);

        // --- CONTENEDOR DE CALENDARIOS (INICIO Y FIN) ---
        JPanel panelCalendarios = new JPanel(new GridLayout(1, 2, 15, 0));
        panelCalendarios.setBorder(new EmptyBorder(15, 20, 15, 20));
        panelCalendarios.setBackground(new Color(248, 250, 252));

        JPanel panelInicioCard = construirPanelCalendarioHibrido("Fecha de Inicio", tempFechaInicio, fecha -> tempFechaInicio = fecha);
        JPanel panelFinCard = construirPanelCalendarioHibrido("Fecha de Fin", tempFechaFin, fecha -> tempFechaFin = fecha);

        panelCalendarios.add(panelInicioCard);
        panelCalendarios.add(panelFinCard);

        // --- BOTÓN INFERIOR ---
        JButton btnAplicar = new JButton("Aplicar Rango");
        btnAplicar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAplicar.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:#186EC9; foreground:#FFFFFF; borderWidth:0; margin:8,22,8,22");
        btnAplicar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAplicar.addActionListener(e -> {
            if (tempFechaInicio.isAfter(tempFechaFin)) {
                JOptionPane.showMessageDialog(dialogoCal, "La fecha de inicio no puede ser posterior a la fecha fin.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            fechaInicioFiltro = tempFechaInicio;
            fechaFinFiltro = tempFechaFin;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String textoRango = formatter.format(fechaInicioFiltro) + " - " + formatter.format(fechaFinFiltro);

            btnSelectorFecha.setText("<html><center><font color='#94A3B8' size='2'>Fecha de creación</font><br><b>📅 " + textoRango + "</b></center></html>");
            dialogoCal.dispose();
            dispararFiltroVentas();
        });

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 14));
        panelSur.setBackground(Color.WHITE);
        panelSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        panelSur.add(btnAplicar);

        dialogoCal.add(panelCabecera, BorderLayout.NORTH);
        dialogoCal.add(panelCalendarios, BorderLayout.CENTER);
        dialogoCal.add(panelSur, BorderLayout.SOUTH);
        dialogoCal.setVisible(true);
    }

    private JPanel construirPanelCalendarioHibrido(String titulo, LocalDate fechaInicial, java.util.function.Consumer<LocalDate> onFechaSeleccionada) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        JLabel lblTitulo = new JLabel(titulo, JLabel.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(new Color(30, 41, 59));
        card.add(lblTitulo, BorderLayout.NORTH);

        // Contenedor superior con Combos de Mes y Año
        JPanel panelSelectoresFecha = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        panelSelectoresFecha.setOpaque(false);

        ComboItem[] meses = {
            new ComboItem(1, "Ene"), new ComboItem(2, "Feb"), new ComboItem(3, "Mar"),
            new ComboItem(4, "Abr"), new ComboItem(5, "May"), new ComboItem(6, "Jun"),
            new ComboItem(7, "Jul"), new ComboItem(8, "Ago"), new ComboItem(9, "Sep"),
            new ComboItem(10, "Oct"), new ComboItem(11, "Nov"), new ComboItem(12, "Dic")
        };
        JComboBox<ComboItem> cmbMes = new JComboBox<>(meses);
        cmbMes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbMes.setSelectedIndex(fechaInicial.getMonthValue() - 1);
        cmbMes.putClientProperty(FlatClientProperties.STYLE, "arc:6");

        int anioActual = LocalDate.now().getYear();
        Integer[] anios = new Integer[12];
        for (int i = 0; i < 12; i++) {
            anios[i] = (anioActual - 8) + i;
        }
        JComboBox<Integer> cmbAnio = new JComboBox<>(anios);
        cmbAnio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbAnio.setSelectedItem(fechaInicial.getYear());
        cmbAnio.putClientProperty(FlatClientProperties.STYLE, "arc:6");

        panelSelectoresFecha.add(cmbMes);
        panelSelectoresFecha.add(cmbAnio);

        // Cuadrícula de días
        JPanel panelGridDias = new JPanel(new GridLayout(7, 7, 3, 3));
        panelGridDias.setBackground(Color.WHITE);

        final LocalDate[] seleccionLocal = {fechaInicial};
        final Runnable[] actualizarGrid = new Runnable[1];

        actualizarGrid[0] = () -> {
            panelGridDias.removeAll();
            String[] nombresDias = {"Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá"};
            for (String d : nombresDias) {
                JLabel lblD = new JLabel(d, JLabel.CENTER);
                lblD.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblD.setForeground(new Color(148, 163, 184));
                panelGridDias.add(lblD);
            }

            int mesSeleccionado = ((ComboItem) cmbMes.getSelectedItem()).valor;
            int anioSeleccionado = (int) cmbAnio.getSelectedItem();

            LocalDate primerDiaMes = LocalDate.of(anioSeleccionado, mesSeleccionado, 1);
            int primerDiaSemana = primerDiaMes.getDayOfWeek().getValue() % 7;
            int diasEnMes = primerDiaMes.lengthOfMonth();

            for (int i = 0; i < primerDiaSemana; i++) {
                panelGridDias.add(new JLabel(""));
            }

            for (int dia = 1; dia <= diasEnMes; dia++) {
                final LocalDate fechaCelda = LocalDate.of(anioSeleccionado, mesSeleccionado, dia);
                JButton btnDia = new JButton(String.valueOf(dia));
                btnDia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnDia.setCursor(new Cursor(Cursor.HAND_CURSOR));

                if (fechaCelda.equals(seleccionLocal[0])) {
                    btnDia.putClientProperty(FlatClientProperties.STYLE, "arc:6; background:#186EC9; foreground:#FFFFFF; borderWidth:0; margin:0,0,0,0");
                } else {
                    btnDia.putClientProperty(FlatClientProperties.STYLE, "arc:6; background:#F8FAFC; foreground:#334155; borderWidth:0; hoverBackground:#E2E8F0; margin:0,0,0,0");
                }

                btnDia.addActionListener(e -> {
                    seleccionLocal[0] = fechaCelda;
                    onFechaSeleccionada.accept(fechaCelda);
                    actualizarGrid[0].run();
                });

                panelGridDias.add(btnDia);
            }

            int totalCeldasPintadas = primerDiaSemana + diasEnMes;
            int celdasRestantes = (totalCeldasPintadas <= 35 ? 35 : 42) - totalCeldasPintadas;
            for (int i = 0; i < celdasRestantes; i++) {
                panelGridDias.add(new JLabel(""));
            }

            panelGridDias.revalidate();
            panelGridDias.repaint();
        };

        cmbMes.addActionListener(e -> actualizarGrid[0].run());
        cmbAnio.addActionListener(e -> actualizarGrid[0].run());

        actualizarGrid[0].run();

        JPanel panelCentroContenedor = new JPanel(new BorderLayout(0, 10));
        panelCentroContenedor.setOpaque(false);
        panelCentroContenedor.add(panelSelectoresFecha, BorderLayout.NORTH);
        panelCentroContenedor.add(panelGridDias, BorderLayout.CENTER);

        card.add(panelCentroContenedor, BorderLayout.CENTER);
        return card;
    }

    private static class ComboItem {

        int valor;
        String texto;

        public ComboItem(int valor, String texto) {
            this.valor = valor;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private JPanel crearPanelAbonos() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- PANEL SUPERIOR (Filtros, Botones y Buscador) ---
        JPanel panelFiltros = new JPanel(new BorderLayout(10, 0));
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.putClientProperty(FlatClientProperties.STYLE, "arc:12; border:12,15,12,15,#E5E9F0,1,12");

        JPanel panelIzquierdaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelIzquierdaFiltros.setOpaque(false);

        JButton btnFiltrosAbonos = new JButton("Filtros");
        btnFiltrosAbonos.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; background:#E2E8F0; font:bold 12");
        btnFiltrosAbonos.addActionListener(e -> abrirModalFiltrosAbonos());

        JButton btnActualizarAbonos = new JButton("Actualizar");
        btnActualizarAbonos.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; background:#E2E8F0; font:bold 12");
        btnActualizarAbonos.addActionListener(e -> {
            txtBuscadorAbonos.setText("");
            filtroEstado = "Todos";
            fechaInicioFiltro = null;
            fechaFinFiltro = null;
            cargarAbonosBD("");
        });

        panelIzquierdaFiltros.add(btnFiltrosAbonos);
        panelIzquierdaFiltros.add(btnActualizarAbonos);

        // Buscador central
        txtBuscadorAbonos = new JTextField(25);
        txtBuscadorAbonos.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar por ID Abono, Cliente o ID Venta...");
        txtBuscadorAbonos.putClientProperty(FlatClientProperties.STYLE, "arc:8; margin:4,10,4,10");
        txtBuscadorAbonos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscadorAbonos.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                cargarAbonosBD(txtBuscadorAbonos.getText().trim());
            }
        });
        panelIzquierdaFiltros.add(txtBuscadorAbonos);

        panelFiltros.add(panelIzquierdaFiltros, BorderLayout.WEST);

        // Etiqueta de Fecha a la derecha
        JPanel panelDerechaFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelDerechaFiltros.setOpaque(false);
        JLabel lblFechaTitulo = new JLabel("Fecha de registro:");
        lblFechaTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFechaTitulo.setForeground(new Color(100, 116, 139));

        JButton btnRangoFechaAbonos = new JButton("Todo el Historial");
        btnRangoFechaAbonos.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:1; borderColor:#E2E8F0; background:#FFFFFF; font:bold 12");
        // Opcional: Agregar selector de fecha al hacer clic si usas un selector de fechas

        panelDerechaFiltros.add(lblFechaTitulo);
        panelDerechaFiltros.add(btnRangoFechaAbonos);
        panelFiltros.add(panelDerechaFiltros, BorderLayout.EAST);

        // --- TABLA DE ABONOS ---
        String[] columnasAbonos = {"ID Abono", "Fecha / Hora", "Cliente", "Ref. Venta ID", "Estatus", "Monto Abonado"};
        modeloAbonos = new DefaultTableModel(columnasAbonos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaAbonos = new JTable(modeloAbonos);
        tablaAbonos.setRowHeight(40);
        tablaAbonos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaAbonos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaAbonos.getTableHeader().setForeground(new Color(100, 116, 139));
        tablaAbonos.setSelectionBackground(new Color(238, 242, 255));

        JScrollPane scrollAbonos = new JScrollPane(tablaAbonos);
        scrollAbonos.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollAbonos.getViewport().setBackground(Color.WHITE);

        // --- PANEL INFERIOR (Totales) ---
        JPanel panelInferiorAbonos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        panelInferiorAbonos.setBackground(Color.WHITE);
        panelInferiorAbonos.putClientProperty(FlatClientProperties.STYLE, "arc:12; border:10,15,10,15,#E5E9F0,1,12");

        lblTotalAbonosMonto = new JLabel("Total Abonos Filtrados: $0.00 MXN");
        lblTotalAbonosMonto.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalAbonosMonto.setForeground(new Color(16, 185, 129));
        panelInferiorAbonos.add(lblTotalAbonosMonto);

        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(scrollAbonos, BorderLayout.CENTER);
        panel.add(panelInferiorAbonos, BorderLayout.SOUTH);

        return panel;
    }

    private void abrirModalFiltrosAbonos() {
        JDialog dialogFiltros = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Filtros Avanzados de Abonos", true);
        dialogFiltros.setSize(450, 320);
        dialogFiltros.setLocationRelativeTo(this);
        dialogFiltros.setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridLayout(3, 1, 10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        // Panel Estado Abono
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstado.setOpaque(false);
        panelEstado.setBorder(BorderFactory.createTitledBorder("Estatus del Abono"));

        JToggleButton btnTodos = new JToggleButton("Todos", filtroEstado.equals("Todos"));
        JToggleButton btnActivo = new JToggleButton("Activo", filtroEstado.equals("ACTIVO"));
        JToggleButton btnCancelado = new JToggleButton("Cancelado", filtroEstado.equals("CANCELADO"));

        ButtonGroup grupoEstado = new ButtonGroup();
        grupoEstado.add(btnTodos);
        grupoEstado.add(btnActivo);
        grupoEstado.add(btnCancelado);

        panelEstado.add(btnTodos);
        panelEstado.add(btnActivo);
        panelEstado.add(btnCancelado);
        content.add(panelEstado);

        // Botón Aplicar
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setOpaque(false);
        JButton btnAplicar = new JButton("Aplicar Filtros");
        btnAplicar.setBackground(new Color(13, 148, 136));
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0");

        btnAplicar.addActionListener(e -> {
            if (btnActivo.isSelected()) {
                filtroEstado = "ACTIVO";
            } else if (btnCancelado.isSelected()) {
                filtroEstado = "CANCELADO";
            } else {
                filtroEstado = "Todos";
            }

            dialogFiltros.dispose();
            cargarAbonosBD(txtBuscadorAbonos.getText().trim());
        });

        panelBoton.add(btnAplicar);

        dialogFiltros.add(content, BorderLayout.CENTER);
        dialogFiltros.add(panelBoton, BorderLayout.SOUTH);
        dialogFiltros.setVisible(true);
    }

private void cargarAbonosBD(String filtro) {
        if (dataSource == null) {
            return;
        }
        modeloAbonos.setRowCount(0);

        // Hacemos JOIN con 'ventas v' para obtener el 'uuid_venta' (el folio real)
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.fecha_registro, a.monto, a.status, "
                + "COALESCE(c.nombre, 'Cliente General') AS cliente_nombre, "
                + "COALESCE(v.uuid_venta, 'Sin Venta Asociada') AS folio_venta "
                + "FROM abonos a "
                + "LEFT JOIN clientes c ON a.cliente_id = c.id "
                + "LEFT JOIN ventas v ON a.venta_id = v.id "
                + "WHERE 1=1 "
        );

        // Filtro por estatus avanzado
        if (!filtroEstado.equals("Todos")) {
            sql.append("AND a.status = ? ");
        }

        // Buscador: Busca por ID de Abono, Nombre del Cliente o Folio de Venta (uuid_venta)
        if (filtro != null && !filtro.isEmpty()) {
            sql.append("AND (c.nombre LIKE ? OR a.id LIKE ? OR v.uuid_venta LIKE ?) ");
        }

        // Filtro por rango de fechas
        if (fechaInicioFiltro != null && fechaFinFiltro != null) {
            sql.append("AND DATE(a.fecha_registro) BETWEEN ? AND ? ");
        }

        sql.append("ORDER BY a.id DESC LIMIT 300");

        BigDecimal acumuladoAbonos = BigDecimal.ZERO;
        int contadorAbonos = 0;

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int index = 1;

            if (!filtroEstado.equals("Todos")) {
                pstmt.setString(index++, filtroEstado);
            }
            if (filtro != null && !filtro.isEmpty()) {
                pstmt.setString(index++, "%" + filtro + "%");
                pstmt.setString(index++, "%" + filtro + "%");
                pstmt.setString(index++, "%" + filtro + "%");
            }
            if (fechaInicioFiltro != null && fechaFinFiltro != null) {
                pstmt.setDate(index++, java.sql.Date.valueOf(fechaInicioFiltro));
                pstmt.setDate(index++, java.sql.Date.valueOf(fechaFinFiltro));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal montoAbono = rs.getBigDecimal("monto");
                    if (montoAbono != null && "ACTIVO".equalsIgnoreCase(rs.getString("status"))) {
                        acumuladoAbonos = acumuladoAbonos.add(montoAbono); // Solo acumula si está activo
                    }

                    Long idAbono = rs.getLong("id");
                    java.sql.Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
                    String clienteNombre = rs.getString("cliente_nombre");
                    String folioVenta = rs.getString("folio_venta");
                    String estatus = rs.getString("status");

                    modeloAbonos.addRow(new Object[]{
                        idAbono,
                        fechaRegistro != null ? fechaRegistro.toString() : "-",
                        clienteNombre,
                        folioVenta, // Muestra el folio real de la venta (ej. NV-179...) o "Sin Venta Asociada"
                        estatus,
                        "$" + (montoAbono != null ? montoAbono.toPlainString() : "0.00") + " MXN"
                    });
                    contadorAbonos++;
                }
            }
            lblTotalAbonosMonto.setText("Total Abonos Filtrados: $" + acumuladoAbonos.toPlainString() + " MXN (" + contadorAbonos + " registros)");
        } catch (SQLException e) {
            System.err.println("Error al cargar abonos filtrados: " + e.getMessage());
        }
    }

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
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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
