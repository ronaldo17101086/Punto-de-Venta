package ventas;

import clientes.clientInterface;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.mycompany.chancuellarpuntodeventa.services.dtos.CambioDTO;
import com.mycompany.chancuellarpuntodeventa.services.querys.ventaServicio;
import com.mycompany.chancuellarpuntodeventa.services.dtos.ClienteDTO;
import com.mycompany.chancuellarpuntodeventa.services.dtos.EnvasePrestadoDTO;
import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import com.mycompany.chancuellarpuntodeventa.services.dtos.VentaDTO;
import com.mycompany.chancuellarpuntodeventa.services.querys.ClienteServicio;
import com.mycompany.chancuellarpuntodeventa.services.querys.ventaServicio;
import com.mycompany.chancuellarpuntodeventa.services.repository.ProductoRepository;
import tools.PlaceholderTextField;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.management.Notification;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import productoFrom.DialogoDetalleProducto;
import productoFrom.ItemProductoCarrito;
import tools.DialogoPago;
import tools.DialogoPago.DialogoExitoVenta;

@org.springframework.stereotype.Component
public class ventas extends JPanel {

    @org.springframework.beans.factory.annotation.Autowired
    private ProductoRepository productoRepository;

    public static JTextField txtBuscador;
    public static JPanel panelCarritoContenedor;
    public static JPanel panelCardsContenedor;
    public static JLabel lblSubtotal, lblIVA;
    public static JButton btnFinalizar;
    public JTextField txtBuscadorDirecto;

    private List<ProductoDTO> listaProductos = new ArrayList<>();
    private final int COLUMNAS = 4;
    private final Color COLOR_SELECCION = new Color(232, 244, 253);

    @Autowired
    private caja.CajaService cajaService; // Esto "jala" el servicio automáticamente

    @Autowired
    private ClienteServicio clienteServicio;
    @Autowired
    private ventaServicio ventaServicio;
    private JComboBox<clientInterface.ItemClienteCombo> comboClientes;
    private JTextField txtComentarios;

    public ventas() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(242, 244, 247));
        initComponentes();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                txtBuscador.requestFocusInWindow();
            }
        });
    }

    @jakarta.annotation.PostConstruct
    private void init() {
        initComponentes();
        cargarProductosCompletos();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                txtBuscador.requestFocusInWindow();
            }
        });
    }

    private void initComponentes() {
        // --- MOTOR DE DISEÑO Y RENDIMIENTO (2026 PRO) ---
        Color bgMain = new Color(248, 250, 253);
        Color white = Color.WHITE;
        Color greenScanner = new Color(34, 197, 94);
        Color textDark = new Color(15, 23, 42);
        Color textMuted = new Color(100, 116, 139);
        Color borderSoft = new Color(230, 235, 245);
        this.setLayout(new BorderLayout(0, 0));

        int columnasFijas = 4;
        panelCardsContenedor = new JPanel(new GridLayout(0, columnasFijas, 15, 15));
        panelCardsContenedor.setBackground(bgMain);
        panelCardsContenedor.setBorder(new EmptyBorder(20, 10, 20, 10));

        // --- LA CORRECCIÓN REAL ---
        JPanel wrapperCatalogo = new JPanel(new BorderLayout());
        wrapperCatalogo.setBackground(bgMain);
        wrapperCatalogo.add(panelCardsContenedor, BorderLayout.NORTH);
        JScrollPane scrollCatalogo = new JScrollPane(wrapperCatalogo);
        scrollCatalogo.setBorder(null);
        scrollCatalogo.getViewport().setBackground(bgMain);
        scrollCatalogo.getVerticalScrollBar().setUnitIncrement(35);
        scrollCatalogo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCatalogo.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                panelCardsContenedor.revalidate();
            }
        });

        // --- 2. CABECERA (BUSCADORES) ---
        JPanel panelCabecera = new JPanel(new GridBagLayout());
        panelCabecera.setBackground(white);
        panelCabecera.setPreferredSize(new Dimension(0, 85));
        panelCabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderSoft));
        GridBagConstraints gbc = new GridBagConstraints();

        // CORRECCIÓN ICONO: Usamos el carácter de flecha circular directa
        JButton btnActualizar = new JButton("\u27F3");
        btnActualizar.setPreferredSize(new Dimension(55, 45));
        btnActualizar.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22)); // Fuente Symbol es más estable para iconos
        btnActualizar.putClientProperty("JButton.buttonType", "roundRect");
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> {
            txtBuscador.setText("");
            cargarProductosCompletos();
        });

        // 1. Asegúrate de que txtBuscador ya esté instanciado
        txtBuscador = new tools.PlaceholderTextField("");
        txtBuscador.setPreferredSize(new Dimension(380, 45));
        txtBuscador.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        // 2. Botón de Limpieza Minimalista
        JButton btnClear = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()) - 10;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Efecto Hover: Círculo sutil
                if (getModel().isRollover()) {
                    g2.setColor(new Color(240, 240, 240)); // Gris muy claro premium
                    g2.fillOval(x, y, size, size);
                }

                // La "X" elegante y delgada
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(150, 150, 150)); // Gris oscuro, no negro total para elegancia

                int padding = size / 3;
                g2.drawLine(x + padding, y + padding, x + size - padding, y + size - padding);
                g2.drawLine(x + size - padding, y + padding, x + padding, y + size - padding);

                g2.dispose();
            }
        };

        // Configuración del botón
        btnClear.setPreferredSize(new Dimension(35, 35));
        btnClear.setFocusable(false);
        btnClear.setBorderPainted(false);
        btnClear.setContentAreaFilled(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Acción de limpieza
        btnClear.addActionListener(e -> {
            txtBuscador.setText("");
            txtBuscador.requestFocusInWindow();
        });

        // 3. Integración en el TextField
        txtBuscador.putClientProperty("JTextField.trailingComponent", btnClear);
        txtBuscador.putClientProperty("JTextField.showClearButton", false); // Desactivamos la X de Windows
        txtBuscador.putClientProperty("JComponent.roundRect", true);
        txtBuscador.putClientProperty("JTextField.padding", new Insets(0, 12, 0, 8)); // Espaciado interno

        // --- DEFINICIÓN DE NUEVOS COLORES (MÁS BONITOS) ---
        Color emeraldBG = new Color(240, 253, 244);     // Fondo menta ultra suave
        Color emeraldBorder = new Color(34, 197, 94);   // Verde vibrante (tipo WhatsApp/Spotify)
        Color emeraldText = new Color(21, 128, 61);     // Texto verde bosque oscuro

        txtBuscadorDirecto = new PlaceholderTextField("");
        txtBuscadorDirecto.setPreferredSize(new Dimension(450, 48));
        txtBuscadorDirecto.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        txtBuscadorDirecto.setForeground(emeraldText);
        txtBuscadorDirecto.setBackground(emeraldBG);
        txtBuscadorDirecto.putClientProperty("JTextField.placeholderText", "MODO SCANNER: Ingrese código de barras...");
        txtBuscadorDirecto.putClientProperty("JTextField.leadingIcon", new com.formdev.flatlaf.icons.FlatSearchWithHistoryIcon());
        txtBuscadorDirecto.putClientProperty("JComponent.roundRect", true);
        txtBuscadorDirecto.putClientProperty("JTextField.outlineColor", emeraldBorder);
        txtBuscadorDirecto.putClientProperty("JTextField.focusedBackground", Color.WHITE);
        txtBuscadorDirecto.putClientProperty("JTextField.showClearButton", true);
        txtBuscadorDirecto.putClientProperty("JTextField.padding", new Insets(0, 10, 0, 10));
        txtBuscadorDirecto.addActionListener(e -> buscarYAgregarDirecto());

        // 1. Definimos la acción que abre el pago (reutilizando el clic del botón)
        Action finalizarVentaAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnFinalizar.isEnabled()) {
                    btnFinalizar.doClick();
                } else {
                    txtBuscador.putClientProperty("JComponent.outline", "error");
                    Timer timer = new Timer(500, ex -> txtBuscador.putClientProperty("JComponent.outline", null));
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        };

        // 2. Registramos la tecla ESCAPE a nivel de PANEL
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "finalizarVenta"
        );
        this.getActionMap().put("finalizarVenta", finalizarVentaAction);

        gbc.insets = new Insets(0, 20, 0, 10);
        gbc.gridx = 0;
        panelCabecera.add(btnActualizar, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCabecera.add(txtBuscador, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 40, 0, 20);
        panelCabecera.add(txtBuscadorDirecto, gbc);

        configurarMenuOpciones(panelCabecera, gbc);

        // --- 3. PANEL CHECKOUT (INICIALIZACIÓN LÓGICA OCULTA) ---
        JPanel panelCheckout = new JPanel(new BorderLayout());
        panelCheckout.setPreferredSize(new Dimension(520, 0));
        panelCheckout.setBackground(white);
        panelCheckout.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, borderSoft));

        comboClientes = new JComboBox<>();
        comboClientes.addItem(new clientInterface.ItemClienteCombo(null, "Público General"));
        cargarClientesBD(comboClientes);

        txtComentarios = new JTextField();

        btnFinalizar = new JButton("$ 0.00 MXN");
        btnFinalizar.setBackground(new Color(200, 200, 200));
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        btnFinalizar.setPreferredSize(new Dimension(0, 70));
        btnFinalizar.setBorder(null);
        btnFinalizar.setFocusPainted(false);
        btnFinalizar.setEnabled(false);
        btnFinalizar.putClientProperty("JButton.buttonType", "roundRect");
        btnFinalizar.addActionListener(e -> {
            finalizarVenta();
        });
        btnFinalizar.addPropertyChangeListener("text", evt -> {
            String texto = btnFinalizar.getText().replaceAll("[^0-9.]", "");
            try {
                double valor = Double.parseDouble(texto);
                if (valor > 0) {
                    btnFinalizar.setEnabled(true);
                    btnFinalizar.setBackground(new Color(34, 197, 94));
                    btnFinalizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    btnFinalizar.setEnabled(false);
                    btnFinalizar.setBackground(new Color(200, 200, 200));
                    btnFinalizar.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            } catch (Exception ex) {
                btnFinalizar.setEnabled(false);
            }
        });

        panelCarritoContenedor = new JPanel();
        panelCarritoContenedor.setLayout(new BoxLayout(panelCarritoContenedor, BoxLayout.Y_AXIS));
        panelCarritoContenedor.setBackground(white);

        // Creamos un wrapper para alinear los elementos arriba y evitar tirones de redibujo
        JPanel wrapperCarrito = new JPanel(new BorderLayout());
        wrapperCarrito.setBackground(white);
        wrapperCarrito.add(panelCarritoContenedor, BorderLayout.NORTH);

        JScrollPane scrollCarrito = new JScrollPane(wrapperCarrito);
        scrollCarrito.setBorder(null);
        scrollCarrito.getViewport().setBackground(white);
        // Añadimos velocidad al scroll del carrito para que no se sienta pesado
        scrollCarrito.getVerticalScrollBar().setUnitIncrement(25);
        scrollCarrito.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // --- 4. PIE DE PAGO ---
        JPanel pPieVenta = new JPanel(new GridBagLayout());
        pPieVenta.setBackground(white);
        pPieVenta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(235, 238, 242)),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        lblSubtotal = new JLabel("$ 0.00");
        lblSubtotal.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 19));
        lblSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);

        lblIVA = new JLabel("$ 0.00");
        lblIVA.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 19));
        lblIVA.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel tSub = new JLabel("Subtotal Neto:");
        tSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tSub.setForeground(textMuted);

        JLabel tIva = new JLabel("Impuestos (16%):");
        tIva.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tIva.setForeground(textMuted);

        g.gridy = 0;
        pPieVenta.add(tSub, g);
        pPieVenta.add(lblSubtotal, g);
        g.gridy = 1;
        g.insets = new Insets(10, 0, 15, 0);
        pPieVenta.add(tIva, g);
        pPieVenta.add(lblIVA, g);

        panelCheckout.add(scrollCarrito, BorderLayout.CENTER);
        panelCheckout.add(pPieVenta, BorderLayout.SOUTH);
        panelCheckout.add(btnFinalizar, BorderLayout.PAGE_END);

        // --- ENSAMBLAJE FINAL ---
        this.add(panelCabecera, BorderLayout.NORTH);
        this.add(scrollCatalogo, BorderLayout.CENTER);
        this.add(panelCheckout, BorderLayout.EAST);

        txtBuscador.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            private void update() {
                SwingUtilities.invokeLater(() -> filtrarCatalogo(txtBuscador.getText()));
            }
        });
    }

// --- MÉTODOS DE DIÁLOGO FLOTANTE ---
    private void abrirDialogoCliente() {
        // 1. Obtener todos los clientes de la BD previamente
        List<ClienteDTO> listaClientesOriginales = new ArrayList<>();
        if (clienteServicio != null) {
            try {
                List<ClienteDTO> clientes = clienteServicio.obtenerTodosLosClientes();
                if (clientes != null) {
                    listaClientesOriginales.add(null); // Público General por defecto
                    for (ClienteDTO c : clientes) {
                        String nombre = c.getNombre() != null ? c.getNombre().trim() : "";
                        if (!nombre.equalsIgnoreCase("público general") && !nombre.equalsIgnoreCase("publico general")) {
                            listaClientesOriginales.add(c);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Detectar qué ID de cliente está seleccionado actualmente en el combo principal
        // Detectar qué ID de cliente está seleccionado actualmente en el combo principal
        Long idClienteActualSeleccionado = null;
        if (comboClientes != null && comboClientes.getSelectedItem() != null) {
            Object seleccionado = comboClientes.getSelectedItem();
            if (seleccionado instanceof clientInterface.ItemClienteCombo itemCombo) {
                idClienteActualSeleccionado = itemCombo.getId(); // Será null si es Público General
            }
        }

        // 2. Creamos y configuramos el diálogo flotante con mayor tamaño (520x420)
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Seleccionar Cliente", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 0));

        JPanel panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelPrincipal.setBackground(Color.WHITE);

        // --- ENCABEZADO: Título y Buscador ---
        JPanel panelNorte = new JPanel(new BorderLayout(8, 8));
        panelNorte.setOpaque(false);

        JLabel lblTitulo = new JLabel("Buscar y Seleccionar Cliente:");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(30, 41, 59));

        JTextField txtFiltro = new JTextField();
        txtFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtFiltro.putClientProperty("JTextField.placeholderText", "Escribe el nombre o teléfono del cliente...");
        txtFiltro.putClientProperty("JTextField.showClearButton", true);
        txtFiltro.setPreferredSize(new Dimension(0, 38));

        panelNorte.add(lblTitulo, BorderLayout.NORTH);
        panelNorte.add(txtFiltro, BorderLayout.CENTER);

        // --- TABLA DE CLIENTES ---
        String[] columnas = {"ID", "Nombre del Cliente", "Teléfono"};
        javax.swing.table.DefaultTableModel modeloTabla = new javax.swing.table.DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        javax.swing.JTable tablaClientes = new javax.swing.JTable(modeloTabla);
        tablaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaClientes.setRowHeight(32);
        tablaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaClientes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(60);

        // Variables empaquetadas para uso seguro dentro de la lambda
        final Long idBusqueda = idClienteActualSeleccionado;
        int[] filaASeleccionar = {0};

        // Función para llenar la tabla dinámicamente
        Runnable actualizarTabla = () -> {
            modeloTabla.setRowCount(0);
            String filtro = txtFiltro.getText().toLowerCase().trim();
            int indexEnTabla = 0;

            for (ClienteDTO c : listaClientesOriginales) {
                if (c == null) {
                    if (filtro.isEmpty() || "público general".contains(filtro)) {
                        modeloTabla.addRow(new Object[]{"-", "Público General", "N/A"});
                        if (idBusqueda == null) {
                            filaASeleccionar[0] = indexEnTabla;
                        }
                        indexEnTabla++;
                    }
                } else {
                    String nombre = c.getNombre() != null ? c.getNombre() : "";
                    String telefono = c.getTelefono() != null ? c.getTelefono() : "";

                    if (filtro.isEmpty() || nombre.toLowerCase().contains(filtro) || telefono.toLowerCase().contains(filtro)) {
                        modeloTabla.addRow(new Object[]{c.getId(), nombre, telefono});
                        if (idBusqueda != null && c.getId().equals(idBusqueda)) {
                            filaASeleccionar[0] = indexEnTabla;
                        }
                        indexEnTabla++;
                    }
                }
            }

            // Seleccionar la fila correspondiente de forma automática
            if (modeloTabla.getRowCount() > 0) {
                int filaDestino = Math.min(filaASeleccionar[0], modeloTabla.getRowCount() - 1);
                tablaClientes.setRowSelectionInterval(filaDestino, filaDestino);
                tablaClientes.scrollRectToVisible(tablaClientes.getCellRect(filaDestino, 0, true));
            }
        };

        // Ejecutar carga inicial
        actualizarTabla.run();

        // Escucha en tiempo real para el buscador
        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizarTabla.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizarTabla.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizarTabla.run();
            }
        });

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(tablaClientes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        // --- BOTÓN DE ACCIÓN ---
        JButton btnAceptar = new JButton("Seleccionar Cliente");
        btnAceptar.setBackground(new Color(16, 185, 129));
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAceptar.setPreferredSize(new Dimension(0, 42));
        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Runnable seleccionarYSalir = () -> {
            int filaSeleccionada = tablaClientes.getSelectedRow();
            if (filaSeleccionada != -1) {
                Object idObj = modeloTabla.getValueAt(filaSeleccionada, 0);

                if (idObj instanceof Long idClienteReal) {
                    for (ClienteDTO c : listaClientesOriginales) {
                        if (c != null && c.getId().equals(idClienteReal)) {
                            comboClientes.removeAllItems();
                            comboClientes.addItem(new clientInterface.ItemClienteCombo(c.getId(), c.getNombre()));
                            break;
                        }
                    }
                } else {
                    comboClientes.removeAllItems();
                    comboClientes.addItem(new clientInterface.ItemClienteCombo(null, "Público General"));
                }
            }
            dialog.dispose();
        };

        btnAceptar.addActionListener(e -> seleccionarYSalir.run());
        tablaClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    seleccionarYSalir.run();
                }
            }
        });

        panelPrincipal.add(panelNorte, BorderLayout.NORTH);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(btnAceptar, BorderLayout.SOUTH);

        dialog.add(panelPrincipal);
        dialog.setVisible(true);
    }

// --- MÉTODO DE DIÁLOGO FLOTANTE DE COMENTARIOS MEJORADO ---
    private void abrirDialogoComentario() {
        // 1. Creamos y configuramos el diálogo con mayor tamaño (480x320) para dar comodidad
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Comentarios de Venta", true);
        dialog.setSize(480, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 0));

        // Panel Principal con diseño limpio y moderno
        JPanel panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelPrincipal.setBackground(Color.WHITE);

        // --- ENCABEZADO: Título descriptivo ---
        JLabel lbl = new JLabel("Ingrese notas o referencia para el ticket/venta:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(30, 41, 59));

        // --- ÁREA DE TEXTO MULTILÍNEA ---
        JTextArea txtAreaTemp = new JTextArea(txtComentarios.getText());
        txtAreaTemp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtAreaTemp.setLineWrap(true);          // Ajusta el texto al ancho del cuadro
        txtAreaTemp.setWrapStyleWord(true);     // Evita que corte palabras a la mitad
        txtAreaTemp.setMargin(new Insets(8, 8, 8, 8));

        // Envolvemos el JTextArea en un JScrollPane para que aparezca la barra de desplazamiento si escriben mucho
        JScrollPane scrollPane = new JScrollPane(txtAreaTemp);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        // --- BOTÓN DE ACCIÓN ---
        JButton btnGuardar = new JButton("Guardar Nota");
        btnGuardar.setBackground(new Color(16, 185, 129)); // Verde moderno
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setPreferredSize(new Dimension(0, 42));
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGuardar.addActionListener(e -> {
            txtComentarios.setText(txtAreaTemp.getText());
            dialog.dispose();
        });

        // Ensamblaje ordenado en el panel principal
        panelPrincipal.add(lbl, BorderLayout.NORTH);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(btnGuardar, BorderLayout.SOUTH);

        dialog.add(panelPrincipal);
        dialog.setVisible(true);
    }

    private void cargarClientesBD(JComboBox<clientInterface.ItemClienteCombo> combo) {
        if (clienteServicio == null) {
            return; // Evita el null pointer si Spring aún lo está inyectando
        }
        try {
            // 1. Limpiamos los elementos anteriores para evitar duplicados o datos viejos
            combo.removeAllItems();

            // 2. Opcional: Volvemos a asegurar que "Público General" esté siempre fijo al inicio
            combo.addItem(new clientInterface.ItemClienteCombo(null, "Público General"));

            // 3. Traemos la lista actualizada desde la base de datos
            List<ClienteDTO> clientes = clienteServicio.obtenerTodosLosClientes();
            if (clientes != null) {
                for (ClienteDTO c : clientes) {
                    // Evitamos duplicar "Público General" si ya viene guardado en la BD
                    String nombre = c.getNombre() != null ? c.getNombre().trim() : "";
                    if (!nombre.equalsIgnoreCase("público general") && !nombre.equalsIgnoreCase("publico general")) {
                        combo.addItem(new clientInterface.ItemClienteCombo(c.getId(), c.getNombre()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buscarYAgregarDirecto() {
        String query = txtBuscadorDirecto.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            return;
        }

        ProductoDTO encontrado = null;

        // 1. Buscamos en la lista maestra por SKU o por Nombre exacto
        for (ProductoDTO p : listaProductos) {
            if (p.getSku().toLowerCase().equals(query) || p.getName().toLowerCase().equals(query)) {
                encontrado = p;
                break;
            }
        }

        if (encontrado != null) {
            // 2. CORRECCIÓN: Usamos alSeleccionarProducto para que si es a granel, 
            // abra el modal de monto en dinero en lugar de agregar 1 unidad automáticamente.
            alSeleccionarProducto(encontrado);

            // 3. Limpieza y enfoque para el siguiente escaneo
            txtBuscadorDirecto.setText("");
            txtBuscadorDirecto.requestFocusInWindow();
        } else {
            // 4. Feedback si el SKU no existe
            JOptionPane.showMessageDialog(this,
                    "No se encontró ningún producto con el nombre o SKU: " + query,
                    "Producto no encontrado",
                    JOptionPane.ERROR_MESSAGE);
            txtBuscadorDirecto.selectAll();
        }
    }

    private void configurarMenuOpciones(JPanel panelCabecera, GridBagConstraints gbc) {
        // 1. Botón "Más" con diseño limpio y minimalista
        JButton btnMas = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Efecto de botón sutil y elegante al pasar el cursor
                if (getModel().isPressed()) {
                    g2.setColor(new Color(203, 213, 225));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(226, 232, 240));
                } else {
                    g2.setColor(new Color(248, 250, 252));
                }
                g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 12, 12);

                // Dibujar los 3 puntos con un color contrastante y elegante
                g2.setColor(new Color(51, 65, 85));
                int x = getWidth() / 2 - 2;
                int yCenter = getHeight() / 2;

                g2.fillOval(x, yCenter - 7, 4, 4);
                g2.fillOval(x, yCenter - 1, 4, 4);
                g2.fillOval(x, yCenter + 5, 4, 4);
                g2.dispose();
            }
        };

        btnMas.setPreferredSize(new Dimension(44, 44));
        btnMas.setBorderPainted(false);
        btnMas.setContentAreaFilled(false);
        btnMas.setFocusPainted(false);
        btnMas.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 2. Menú desplegable estilo "Card" moderna (Fondo blanco limpio, sombras y bordes suaves)
        JPopupMenu menu = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo principal blanco puro
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Borde exterior fino y elegante
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };

        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Tipografía limpia y legible
        Font fontMenu = new Font("Segoe UI", Font.PLAIN, 14);

        // --- OPCIÓN 1: Seleccionar Cliente ---
        JMenuItem itemCliente = new JMenuItem("Seleccionar Cliente", new com.formdev.flatlaf.icons.FlatSearchIcon());
        estilizarItemModoClaro(itemCliente, fontMenu);
        itemCliente.addActionListener(e -> abrirDialogoCliente());

        // --- OPCIÓN 2: Agregar Comentario ---
        JMenuItem itemComentario = new JMenuItem("Agregar Comentario", new com.formdev.flatlaf.icons.FlatClearIcon());
        estilizarItemModoClaro(itemComentario, fontMenu);
        itemComentario.addActionListener(e -> abrirDialogoComentario());

        // --- OPCIÓN 3: Gestión de Importes ---
        JMenuItem itemImportes = new JMenuItem("Importes", new com.formdev.flatlaf.icons.FlatFileViewFloppyDriveIcon());
        estilizarItemModoClaro(itemImportes, fontMenu);
        itemImportes.addActionListener(e -> abrirDialogoGestionImportes());

        // --- OPCIÓN: Gestión de Envases Prestados ---
        JMenuItem itemEnvasesPrestados = new JMenuItem("Prestados", new com.formdev.flatlaf.icons.FlatFileViewFloppyDriveIcon());
        estilizarItemModoClaro(itemEnvasesPrestados, fontMenu); // Descomenta si usas este método en tu menú
        itemEnvasesPrestados.addActionListener(e -> abrirDialogoGestionEnvasesPrestados());

        // --- OPCIÓN 4: Control de Caja ---
        JMenuItem itemCaja = new JMenuItem("Control de Caja");
        estilizarItemModoClaro(itemCaja, fontMenu);
        itemCaja.addActionListener(e -> {
            try {
                caja.CajaView vc = new caja.CajaView(cajaService);
                vc.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Ensamblaje ordenado con separador estético
        menu.add(itemCliente);
        menu.add(itemComentario);
        menu.add(itemImportes);
        menu.add(itemEnvasesPrestados);
        menu.addSeparator();
        menu.add(itemCaja);

        // Mostrar menú alineado correctamente
        btnMas.addActionListener(e -> {
            menu.show(btnMas, btnMas.getWidth() - menu.getPreferredSize().width, btnMas.getHeight() + 4);
        });

        // 3. Ubicación final en la cabecera
        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 5, 0, 15);
        panelCabecera.add(btnMas, gbc);
    }

    // Método auxiliar para darles espaciado y un diseño limpio a las opciones
    private void estilizarItemModoClaro(JMenuItem item, Font font) {
        item.setFont(font);
        item.setForeground(new Color(30, 41, 59)); // Texto oscuro de alta legibilidad
        item.setIconTextGap(14);
        item.setOpaque(true);
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    private void abrirDialogoGestionImportes() {
        JDialog dialogo = new JDialog((Frame) null, "Gestión de Clientes con Importes Pendientes", true);
        dialogo.setSize(1000, 550);
        dialogo.setLocationRelativeTo(null);
        dialogo.setLayout(new BorderLayout(10, 10));

        // Panel Superior: Búsqueda de cliente en tiempo real
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelFiltro.setBackground(Color.WHITE);
        JLabel lblBuscar = new JLabel("Filtrar por Cliente:");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JTextField txtBuscar = new JTextField(25);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panelFiltro.add(lblBuscar);
        panelFiltro.add(txtBuscar);

        // Tabla de Importes Pendientes
        String[] columnas = {"ID Cliente", "Cliente", "Teléfono", "Pzas", "Detalle de Importe", "Monto Total ($)"};
        javax.swing.table.DefaultTableModel modeloTabla = new javax.swing.table.DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };
        JTable tablaImportes = new JTable(modeloTabla);
        tablaImportes.setRowHeight(28);
        tablaImportes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaImportes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollTabla = new JScrollPane(tablaImportes);

        // --- EVENTO DE DOBLE CLIC PARA VER EL DETALLE EN FORMATO DE LISTA CON SCROLL ---
        tablaImportes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int filaSeleccionada = tablaImportes.getSelectedRow();
                    if (filaSeleccionada != -1) {
                        String nombreCliente = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
                        String piezasTotal = modeloTabla.getValueAt(filaSeleccionada, 3).toString();
                        String detalleCompleto = (String) modeloTabla.getValueAt(filaSeleccionada, 4);
                        String montoTotal = modeloTabla.getValueAt(filaSeleccionada, 5).toString();

                        StringBuilder listaFormateada = new StringBuilder();
                        if (detalleCompleto != null && !detalleCompleto.isEmpty()) {
                            String[] items = detalleCompleto.split(",");
                            for (String item : items) {
                                listaFormateada.append(" • ").append(item.trim()).append("\n\n");
                            }
                        } else {
                            listaFormateada.append("Sin detalles registrados.");
                        }

                        JTextArea txtAreaDetalle = new JTextArea(listaFormateada.toString());
                        txtAreaDetalle.setEditable(false);
                        txtAreaDetalle.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        txtAreaDetalle.setForeground(new Color(33, 37, 41));
                        txtAreaDetalle.setBackground(new Color(250, 250, 250));
                        txtAreaDetalle.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                        JScrollPane scrollDetalle = new JScrollPane(txtAreaDetalle);
                        scrollDetalle.setPreferredSize(new Dimension(480, 220));
                        scrollDetalle.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 1));

                        JPanel panelContenedor = new JPanel(new BorderLayout(10, 10));
                        panelContenedor.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                        JLabel lblHeader = new JLabel("<html><b>Cliente:</b> " + nombreCliente + " &nbsp;|&nbsp; <b>Pzas:</b> " + piezasTotal + " &nbsp;|&nbsp; <b>Total:</b> " + montoTotal + "</html>");
                        lblHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        lblHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

                        panelContenedor.add(lblHeader, BorderLayout.NORTH);
                        panelContenedor.add(scrollDetalle, BorderLayout.CENTER);

                        JOptionPane.showMessageDialog(
                                dialogo,
                                panelContenedor,
                                "Detalle Completo de Importes",
                                JOptionPane.PLAIN_MESSAGE
                        );
                    }
                }
            }
        });

        // Método para cargar y filtrar los datos usando tu servicio
        Runnable cargarDatosImportes = () -> {
            modeloTabla.setRowCount(0);
            String filtro = txtBuscar.getText().trim();

            try {
                List<ventaServicio.ImporteClienteDTO> listaImportes = ventaServicio.obtenerClientesConImportes(filtro);

                for (ventaServicio.ImporteClienteDTO imp : listaImportes) {
                    modeloTabla.addRow(new Object[]{
                        imp.getClienteId(),
                        imp.getNombre(),
                        imp.getTelefono(),
                        imp.getPiezas() + " pzas",
                        imp.getDetalle(),
                        "$" + imp.getMontoTotal()
                    });
                }
            } catch (Exception ex) {
                System.err.println("Error al cargar importes en la tabla: " + ex.getMessage());
            }
        };

        // Carga inicial y automática al abrir
        cargarDatosImportes.run();

        // Evento de búsqueda en tiempo real al escribir en el JTextField
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                cargarDatosImportes.run();
            }
        });

        // Panel Inferior: Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBotones.setBackground(Color.WHITE);

        JButton btnAbonarLiquidar = new JButton("Abonar / Liquidar Importe");
        btnAbonarLiquidar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAbonarLiquidar.setBackground(new Color(13, 148, 136)); // Color verde/azulado elegante
        btnAbonarLiquidar.setForeground(Color.WHITE);
        btnAbonarLiquidar.setFocusPainted(false);

        // Acción al presionar Abonar / Liquidar (Abre ventana interactiva por piezas/productos)
        btnAbonarLiquidar.addActionListener(e -> {
            int filaSeleccionada = tablaImportes.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(dialogo, "Por favor, seleccione un cliente de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Long clienteId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
            String nombreCliente = (String) modeloTabla.getValueAt(filaSeleccionada, 1);

            // Crear diálogo interactivo de devolución de importes por producto
            JDialog dialogoDevolucion = new JDialog(dialogo, "Abonar o Liquidar Importe - " + nombreCliente, true);
            dialogoDevolucion.setSize(650, 400);
            dialogoDevolucion.setLocationRelativeTo(dialogo);
            dialogoDevolucion.setLayout(new BorderLayout(10, 10));

            List<ventaServicio.DetalleImporteDTO> listaDetalle = ventaServicio.obtenerDetalleImportesCliente(clienteId);

            if (listaDetalle.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Este cliente no tiene importes pendientes registrados.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] colDev = {"ID Prod", "Producto", "Pendientes", "Monto Unit.", "Cantidad a Devolver"};
            javax.swing.table.DefaultTableModel modeloDev = new javax.swing.table.DefaultTableModel(colDev, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 4; // Solo la columna "Cantidad a Devolver" es editable
                }
            };

            for (ventaServicio.DetalleImporteDTO det : listaDetalle) {
                BigDecimal unitario = det.getCantidadPendiente().compareTo(BigDecimal.ZERO) > 0
                        ? det.getMontoTotal().divide(det.getCantidadPendiente(), 2, java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                modeloDev.addRow(new Object[]{
                    det.getProductoId(),
                    det.getNombreProducto(),
                    det.getCantidadPendiente(),
                    "$" + unitario,
                    BigDecimal.ZERO // Valor inicial
                });
            }

            JTable tablaDev = new JTable(modeloDev);
            tablaDev.setRowHeight(30);
            tablaDev.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tablaDev.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            JScrollPane scrollDev = new JScrollPane(tablaDev);

            JPanel pnlBotonesDev = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            pnlBotonesDev.setBackground(Color.WHITE);

            JButton btnLiquidarTodo = new JButton("Liquidar Todo");
            btnLiquidarTodo.setBackground(new Color(220, 53, 69));
            btnLiquidarTodo.setForeground(Color.WHITE);
            btnLiquidarTodo.setFocusPainted(false);

            JButton btnConfirmarDev = new JButton("Confirmar Abono / Salida");
            btnConfirmarDev.setBackground(new Color(13, 148, 136));
            btnConfirmarDev.setForeground(Color.WHITE);
            btnConfirmarDev.setFocusPainted(false);

            btnLiquidarTodo.addActionListener(ev -> {
                int confirm = JOptionPane.showConfirmDialog(dialogoDevolucion, "¿Desea liquidar TODOS los importes y retirar el efectivo completo de caja para este cliente?", "Confirmar Liquidación Total", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        Map<Long, BigDecimal> devolucionesMap = new java.util.HashMap<>();
                        for (int i = 0; i < modeloDev.getRowCount(); i++) {
                            Long prodId = (Long) modeloDev.getValueAt(i, 0);
                            BigDecimal pendiente = (BigDecimal) modeloDev.getValueAt(i, 2);
                            devolucionesMap.put(prodId, pendiente);
                        }

                        ventaServicio.abonarOLiquidarImportePorProducto(clienteId, devolucionesMap);
                        JOptionPane.showMessageDialog(dialogoDevolucion, "¡Importes liquidados con éxito y salida registrada en caja!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        dialogoDevolucion.dispose();
                        cargarDatosImportes.run();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialogoDevolucion, "Error al liquidar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            btnConfirmarDev.addActionListener(ev -> {
                if (tablaDev.isEditing()) {
                    tablaDev.getCellEditor().stopCellEditing();
                }

                try {
                    Map<Long, BigDecimal> devolucionesMap = new java.util.HashMap<>();
                    for (int i = 0; i < modeloDev.getRowCount(); i++) {
                        Long prodId = (Long) modeloDev.getValueAt(i, 0);
                        BigDecimal pendiente = (BigDecimal) modeloDev.getValueAt(i, 2);

                        Object valDev = modeloDev.getValueAt(i, 4);
                        BigDecimal aDevolver = BigDecimal.ZERO;
                        if (valDev != null && !valDev.toString().trim().isEmpty()) {
                            aDevolver = new BigDecimal(valDev.toString().trim());
                        }

                        if (aDevolver.compareTo(BigDecimal.ZERO) > 0) {
                            if (aDevolver.compareTo(pendiente) > 0) {
                                JOptionPane.showMessageDialog(dialogoDevolucion, "No puede abonar más de las piezas pendientes en la fila " + (i + 1), "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            devolucionesMap.put(prodId, aDevolver);
                        }
                    }

                    if (devolucionesMap.isEmpty()) {
                        JOptionPane.showMessageDialog(dialogoDevolucion, "Ingrese al menos una cantidad mayor a 0 para abonar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Registra el abono, actualiza las piezas y genera la salida de dinero en caja automáticamente
                    ventaServicio.abonarOLiquidarImportePorProducto(clienteId, devolucionesMap);

                    JOptionPane.showMessageDialog(dialogoDevolucion, "¡Abono registrado con éxito y salida reflejada en caja!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dialogoDevolucion.dispose();
                    cargarDatosImportes.run();

                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(dialogoDevolucion, "Por favor, ingrese números válidos en las cantidades.", "Formato Inválido", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialogoDevolucion, "Error al procesar el abono: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            pnlBotonesDev.add(btnLiquidarTodo);
            pnlBotonesDev.add(btnConfirmarDev);

            JPanel pnlNorteDev = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlNorteDev.setBackground(Color.WHITE);
            pnlNorteDev.add(new JLabel("<html><b>Edite la columna 'Cantidad a Devolver' para abonar por piezas:</b></html>"));

            dialogoDevolucion.add(pnlNorteDev, BorderLayout.NORTH);
            dialogoDevolucion.add(scrollDev, BorderLayout.CENTER);
            dialogoDevolucion.add(pnlBotonesDev, BorderLayout.SOUTH);

            dialogoDevolucion.setVisible(true);
        });

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCerrar.addActionListener(e -> dialogo.dispose());

        panelBotones.add(btnAbonarLiquidar);
        panelBotones.add(btnCerrar);

        dialogo.add(panelFiltro, BorderLayout.NORTH);
        dialogo.add(scrollTabla, BorderLayout.CENTER);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setVisible(true);
    }

    private void abrirDialogoGestionEnvasesPrestados() {
        JDialog dialogo = new JDialog((Frame) null, "Control y Devolución de Envases Prestados", true);
        dialogo.setSize(1000, 550);
        dialogo.setLocationRelativeTo(null);
        dialogo.setLayout(new BorderLayout(10, 10));

        // Panel Superior: Búsqueda de cliente en tiempo real
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelFiltro.setBackground(Color.WHITE);
        JLabel lblBuscar = new JLabel("Filtrar por Cliente:");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JTextField txtBuscar = new JTextField(25);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panelFiltro.add(lblBuscar);
        panelFiltro.add(txtBuscar);

        // Tabla principal de Envases Prestados Pendientes
        String[] columnas = {"ID Cliente", "Cliente", "Teléfono", "Pzas Prestadas", "Detalle de Envases", "Estado"};
        javax.swing.table.DefaultTableModel modeloTabla = new javax.swing.table.DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };
        JTable tablaEnvases = new JTable(modeloTabla);
        tablaEnvases.setRowHeight(28);
        tablaEnvases.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaEnvases.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollTabla = new JScrollPane(tablaEnvases);

        // --- EVENTO DE DOBLE CLIC FORMATO LISTA CON SCROLL PARA VER DETALLES ---
        tablaEnvases.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int filaSeleccionada = tablaEnvases.getSelectedRow();
                    if (filaSeleccionada != -1) {
                        String nombreCliente = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
                        String piezasTotal = modeloTabla.getValueAt(filaSeleccionada, 3).toString();
                        String detalleCompleto = (String) modeloTabla.getValueAt(filaSeleccionada, 4);

                        StringBuilder listaFormateada = new StringBuilder();
                        if (detalleCompleto != null && !detalleCompleto.isEmpty()) {
                            String[] items = detalleCompleto.split(",");
                            for (String item : items) {
                                listaFormateada.append(" • ").append(item.trim()).append("\n\n");
                            }
                        } else {
                            listaFormateada.append("Sin detalles registrados.");
                        }

                        JTextArea txtAreaDetalle = new JTextArea(listaFormateada.toString());
                        txtAreaDetalle.setEditable(false);
                        txtAreaDetalle.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        txtAreaDetalle.setForeground(new Color(33, 37, 41));
                        txtAreaDetalle.setBackground(new Color(250, 250, 250));
                        txtAreaDetalle.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                        JScrollPane scrollDetalle = new JScrollPane(txtAreaDetalle);
                        scrollDetalle.setPreferredSize(new Dimension(480, 220));
                        scrollDetalle.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 1));

                        JPanel panelContenedor = new JPanel(new BorderLayout(10, 10));
                        panelContenedor.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                        JLabel lblHeader = new JLabel("<html><b>Cliente:</b> " + nombreCliente + " &nbsp;&nbsp;|&nbsp;&nbsp; <b>Total prestado:</b> " + piezasTotal + "</html>");
                        lblHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        lblHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

                        panelContenedor.add(lblHeader, BorderLayout.NORTH);
                        panelContenedor.add(scrollDetalle, BorderLayout.CENTER);

                        JOptionPane.showMessageDialog(
                                dialogo,
                                panelContenedor,
                                "Detalle de Envases Prestados",
                                JOptionPane.PLAIN_MESSAGE
                        );
                    }
                }
            }
        });

        // Método para cargar y actualizar los datos al instante
        Runnable cargarDatosEnvases = () -> {
            modeloTabla.setRowCount(0);
            String filtro = txtBuscar.getText().trim();

            try {
                List<EnvasePrestadoDTO> listaEnvases = ventaServicio.obtenerClientesConEnvasesPrestados(filtro);

                for (EnvasePrestadoDTO env : listaEnvases) {
                    modeloTabla.addRow(new Object[]{
                        env.getClienteId(),
                        env.getNombre(),
                        env.getTelefono(),
                        env.getPiezasPendientes() + " pzas",
                        env.getDetalleEnvases(),
                        "Pendiente de devolución"
                    });
                }
            } catch (Exception ex) {
                System.err.println("Error al cargar envases en la tabla: " + ex.getMessage());
            }
        };

        // Carga inicial y automática al abrir la ventana
        cargarDatosEnvases.run();

        // Evento de búsqueda en tiempo real al escribir
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                cargarDatosEnvases.run();
            }
        });

        // Panel Inferior: Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBotones.setBackground(Color.WHITE);

        JButton btnRegistrarDevolucion = new JButton("Registrar Devolución / Liberar");
        btnRegistrarDevolucion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegistrarDevolucion.setBackground(new Color(40, 167, 69));
        btnRegistrarDevolucion.setForeground(Color.WHITE);
        btnRegistrarDevolucion.setFocusPainted(false);

        // --- ACCIÓN DE DEVOLUCIÓN MEDIANTE VENTANA DE SELECCIÓN DE PRODUCTOS ---
        btnRegistrarDevolucion.addActionListener(e -> {
            int filaSeleccionada = tablaEnvases.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(dialogo, "Por favor, seleccione un cliente de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Long clienteId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
            String nombreCliente = (String) modeloTabla.getValueAt(filaSeleccionada, 1);

            // Crear diálogo personalizado con tabla de productos a devolver
            JDialog dialogoDevolucion = new JDialog(dialogo, "Control de Devolución - " + nombreCliente, true);
            dialogoDevolucion.setSize(600, 400);
            dialogoDevolucion.setLocationRelativeTo(dialogo);
            dialogoDevolucion.setLayout(new BorderLayout(10, 10));

            List<ventaServicio.DetalleEnvaseDTO> listaDetalle = ventaServicio.obtenerDetalleEnvasesCliente(clienteId);

            if (listaDetalle.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Este cliente no tiene envases pendientes registrados.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] colDev = {"ID Prod", "Producto", "Pendientes", "Cantidad a Devolver"};
            javax.swing.table.DefaultTableModel modeloDev = new javax.swing.table.DefaultTableModel(colDev, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 3; // Solo la columna "Cantidad a Devolver" es editable
                }
            };

            for (ventaServicio.DetalleEnvaseDTO det : listaDetalle) {
                modeloDev.addRow(new Object[]{
                    det.getProductoId(),
                    det.getNombreProducto(),
                    det.getCantidadPendiente(),
                    BigDecimal.ZERO // Valor inicial a devolver
                });
            }

            JTable tablaDev = new JTable(modeloDev);
            tablaDev.setRowHeight(30);
            tablaDev.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tablaDev.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            JScrollPane scrollDev = new JScrollPane(tablaDev);

            JPanel pnlBotonesDev = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            pnlBotonesDev.setBackground(Color.WHITE);

            JButton btnLiberarTodo = new JButton("Liberar Todo");
            btnLiberarTodo.setBackground(new Color(220, 53, 69));
            btnLiberarTodo.setForeground(Color.WHITE);
            btnLiberarTodo.setFocusPainted(false);

            JButton btnConfirmarDev = new JButton("Confirmar Devolución");
            btnConfirmarDev.setBackground(new Color(40, 167, 69));
            btnConfirmarDev.setForeground(Color.WHITE);
            btnConfirmarDev.setFocusPainted(false);

            btnLiberarTodo.addActionListener(ev -> {
                int confirm = JOptionPane.showConfirmDialog(dialogoDevolucion, "¿Desea liberar TODOS los envases pendientes de este cliente?", "Confirmar Liberación Total", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        ventaServicio.registrarDevolucionEnvases(clienteId, "TODO");
                        JOptionPane.showMessageDialog(dialogoDevolucion, "¡Todos los envases han sido liberados con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        dialogoDevolucion.dispose();
                        cargarDatosEnvases.run();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialogoDevolucion, "Error al liberar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            btnConfirmarDev.addActionListener(ev -> {
                // --- CORRECCIÓN CLAVE: Detener edición de la celda activa para tomar el valor recién escrito ---
                if (tablaDev.isEditing()) {
                    tablaDev.getCellEditor().stopCellEditing();
                }

                try {
                    Map<Long, BigDecimal> devolucionesMap = new java.util.HashMap<>();
                    for (int i = 0; i < modeloDev.getRowCount(); i++) {
                        Long prodId = (Long) modeloDev.getValueAt(i, 0);
                        BigDecimal pendiente = new BigDecimal(modeloDev.getValueAt(i, 2).toString());

                        Object valDev = modeloDev.getValueAt(i, 3);
                        BigDecimal aDevolver = BigDecimal.ZERO;

                        if (valDev != null && !valDev.toString().trim().isEmpty()) {
                            aDevolver = new BigDecimal(valDev.toString().trim());
                        }

                        if (aDevolver.compareTo(BigDecimal.ZERO) > 0) {
                            if (aDevolver.compareTo(pendiente) > 0) {
                                JOptionPane.showMessageDialog(dialogoDevolucion,
                                        "No puede devolver más de lo pendiente (" + pendiente + ") en el producto de la fila " + (i + 1),
                                        "Cantidad Excedida", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            devolucionesMap.put(prodId, aDevolver);
                        }
                    }

                    if (devolucionesMap.isEmpty()) {
                        JOptionPane.showMessageDialog(dialogoDevolucion, "Ingrese al menos una cantidad mayor a 0 para devolver.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Guardar devolución parcial
                    ventaServicio.registrarDevolucionEnvasesPorProducto(clienteId, devolucionesMap);

                    JOptionPane.showMessageDialog(dialogoDevolucion, "¡Devolución registrada con éxito! Se actualizaron las piezas pendientes.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                    dialogoDevolucion.dispose();

                    // Refrescar la tabla principal para que Ronaldo ahora muestre 3 pzas (1 Galón + 2 Caguamas)
                    cargarDatosEnvases.run();

                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(dialogoDevolucion, "Por favor, ingrese números válidos en las cantidades a devolver.", "Formato Inválido", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialogoDevolucion, "Error al registrar la devolución: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            pnlBotonesDev.add(btnLiberarTodo);
            pnlBotonesDev.add(btnConfirmarDev);

            JPanel pnlNorteDev = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlNorteDev.setBackground(Color.WHITE);
            pnlNorteDev.add(new JLabel("<html><b>Edite la columna 'Cantidad a Devolver' según lo que trajo el cliente:</b></html>"));

            dialogoDevolucion.add(pnlNorteDev, BorderLayout.NORTH);
            dialogoDevolucion.add(scrollDev, BorderLayout.CENTER);
            dialogoDevolucion.add(pnlBotonesDev, BorderLayout.SOUTH);

            dialogoDevolucion.setVisible(true);
        });

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCerrar.addActionListener(e -> dialogo.dispose());

        panelBotones.add(btnRegistrarDevolucion);
        panelBotones.add(btnCerrar);

        dialogo.add(panelFiltro, BorderLayout.NORTH);
        dialogo.add(scrollTabla, BorderLayout.CENTER);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setVisible(true);
    }

    private JMenuItem crearItemModerno(String texto, String icono, java.awt.event.ActionListener accion) {
        JMenuItem item = new JMenuItem();
        item.setLayout(new GridBagLayout()); // Usamos Layout para control total
        item.setPreferredSize(new Dimension(240, 48)); // Más ancho y alto
        item.setBackground(Color.WHITE);
        item.setOpaque(true);
        item.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15)); // Margen interno lateral
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        GridBagConstraints c = new GridBagConstraints();

        // --- ICONO ---
        JLabel lblIcono = new JLabel(icono);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        c.gridx = 0;
        c.weightx = 0.0;
        c.insets = new Insets(0, 0, 0, 15); // ESPACIO FIJO entre icono y texto
        item.add(lblIcono, c);

        // --- TEXTO ---
        JLabel lblTexto = new JLabel(texto);
        lblTexto.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Semibold para modernidad
        lblTexto.setForeground(new Color(51, 65, 85)); // Color Slate 700
        c.gridx = 1;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        item.add(lblTexto, c);

        // --- ACCIÓN ---
        // Importante: quitamos cualquier listener previo y agregamos el nuevo
        for (java.awt.event.ActionListener al : item.getActionListeners()) {
            item.removeActionListener(al);
        }
        item.addActionListener(accion);

        // --- EFECTOS VISUALES CORREGIDOS ---
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Un azul moderno y vibrante (estilo Microsoft/Google)
                item.setBackground(new Color(37, 99, 235));
                lblTexto.setForeground(Color.WHITE); // Texto en blanco para contraste total
                lblIcono.setForeground(Color.WHITE); // Si el icono es texto/emoji, también cambia
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                // Volvemos al estado limpio
                item.setBackground(Color.WHITE);
                lblTexto.setForeground(new Color(51, 65, 85)); // Slate 700
                lblIcono.setForeground(new Color(51, 65, 85));
            }
        });

// ESTO ES CLAVE: Evita que Swing pinte su propio fondo azul encima del nuestro
        item.setContentAreaFilled(false);
        item.setOpaque(true);

        return item;
    }

    public void alSeleccionarProducto(ProductoDTO p) {
        if (p.isGranel()) {
            // --- PALETA DE COLORES "CYBER-CLEAN" ---
            Color COLOR_FONDO = new Color(248, 250, 252);
            Color COLOR_PRIMARIO = new Color(79, 70, 229); // Indigo moderno
            Color COLOR_EXITO = new Color(16, 185, 129);   // Esmeralda
            Color COLOR_TEXTO = new Color(15, 23, 42);
            Color COLOR_GRADIENTE = new Color(238, 242, 255);

            // Crear JDialog con estilo Custom
            JDialog win = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
            win.setUndecorated(true); // Quitamos la barra de Windows fea
            win.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

            JPanel layout = new JPanel(new BorderLayout());
            layout.setBackground(COLOR_FONDO);

            // 1. HEADER CON GRADIENTE (Nombre y Precio)
            JPanel header = new JPanel(new GridLayout(2, 1));
            header.setBackground(COLOR_GRADIENTE);
            header.setBorder(new EmptyBorder(25, 40, 25, 40));

            JLabel name = new JLabel(p.getName().toUpperCase());
            name.setFont(new Font("Inter", Font.BOLD, 32));
            name.setForeground(COLOR_TEXTO);

            JLabel priceInfo = new JLabel("Tasa de mercado: $" + p.getPrice() + " MXN / Kilogramo");
            priceInfo.setFont(new Font("Inter", Font.ITALIC, 14));
            priceInfo.setForeground(new Color(71, 85, 105));

            header.add(name);
            header.add(priceInfo);

            // 2. CUERPO DE ENTRADA (Diseño Horizontal Minimalista)
            JPanel body = new JPanel(new GridBagLayout());
            body.setBackground(COLOR_FONDO);
            body.setBorder(new EmptyBorder(40, 40, 40, 40));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL; // Cambia BOTH por HORIZONTAL
            gbc.anchor = GridBagConstraints.CENTER;   // Centra los elementos verticalmente
            gbc.weightx = 1.0;

            // Estilizamos los campos con un método dedicado
            JTextField fieldGramos = crearFieldModerno(COLOR_PRIMARIO, "0");
            JTextField fieldTotal = crearFieldModerno(COLOR_EXITO, "0.00");

            gbc.gridx = 0;
            body.add(crearModuloInput("CANTIDAD (GRAMOS)", fieldGramos, "Báscula en tiempo real"), gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 30, 0, 0);
            body.add(crearModuloInput("IMPORTE ($)", fieldTotal, "Monto final"), gbc);

            // --- LÓGICA DE INTERCONEXIÓN (Segura) ---
            fieldGramos.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    try {
                        BigDecimal g = new BigDecimal(fieldGramos.getText().replaceAll("[^0-9.]", ""));
                        BigDecimal res = p.getPrice().multiply(g.divide(new BigDecimal("1000"))).setScale(2, RoundingMode.HALF_UP);
                        fieldTotal.setText(res.toString());
                    } catch (Exception ex) {
                        fieldTotal.setText("0.00");
                    }
                }
            });

            fieldTotal.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    try {
                        BigDecimal d = new BigDecimal(fieldTotal.getText().replaceAll("[^0-9.]", ""));
                        BigDecimal g = d.divide(p.getPrice(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("1000")).setScale(0, RoundingMode.HALF_UP);
                        fieldGramos.setText(g.toString());
                    } catch (Exception ex) {
                        fieldGramos.setText("0");
                    }
                }
            });

            // 3. BOTONERA INNOVADORA (Diseño "Edge-to-Edge")
            JPanel footer = new JPanel(new GridLayout(1, 2));

            JButton btnAdd = new JButton("CONFIRMAR OPERACIÓN");
            btnAdd.setFont(new Font("Inter", Font.BOLD, 15));
            btnAdd.setBackground(COLOR_PRIMARIO);
            btnAdd.setForeground(Color.WHITE);
            btnAdd.setFocusPainted(false);
            btnAdd.setBorder(new EmptyBorder(20, 0, 20, 0));
            btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JButton btnClose = new JButton("CANCELAR");
            btnClose.setFont(new Font("Inter", Font.BOLD, 14));
            btnClose.setBackground(new Color(241, 245, 249));
            btnClose.setForeground(new Color(100, 116, 139));
            btnClose.setFocusPainted(false);
            btnClose.setBorder(new EmptyBorder(20, 0, 20, 0));

            footer.add(btnClose);
            footer.add(btnAdd);

            // --- ACCIONES DE BOTONES ---
            btnAdd.addActionListener(e -> {
                try {
                    BigDecimal g = new BigDecimal(fieldGramos.getText());
                    BigDecimal kg = g.divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
                    // ESTA ES LA CLAVE: Llamada directa
                    if (kg.compareTo(BigDecimal.ZERO) > 0) {
                        agregarAlCarrito(p, kg);
                    }
                    win.dispose();
                } catch (Exception ex) {
                }
            });

            btnClose.addActionListener(e -> win.dispose());

            // Armar el JDialog
            layout.add(header, BorderLayout.NORTH);
            layout.add(body, BorderLayout.CENTER);
            layout.add(footer, BorderLayout.SOUTH);

            win.add(layout);
            win.pack();
            win.setLocationRelativeTo(this);
            win.setVisible(true);

        } else {
            agregarAlCarrito(p, BigDecimal.ONE);
        }
    }

    private JPanel crearModuloInput(String tag, JTextField f, String hint) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        JLabel lblTag = new JLabel(tag);
        lblTag.setFont(new Font("Inter", Font.BOLD, 12));
        lblTag.setForeground(new Color(100, 116, 139));

        JLabel lblHint = new JLabel(hint);
        lblHint.setFont(new Font("Inter", Font.PLAIN, 11));
        lblHint.setForeground(new Color(148, 163, 184));
        lblHint.setHorizontalAlignment(SwingConstants.CENTER);

        p.add(lblTag, BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        p.add(lblHint, BorderLayout.SOUTH);
        return p;
    }

    private JTextField crearFieldModerno(Color accent, String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(new Font("Inter", Font.BOLD, 42)); // TAMAÑO IMPACTANTE
        f.setHorizontalAlignment(JTextField.CENTER);
        f.setPreferredSize(new Dimension(240, 85));
        f.setBackground(Color.WHITE);
        f.setForeground(new Color(30, 41, 59));

        // Borde Neumórfico Suave
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createLineBorder(accent, 2, true));
                f.selectAll();
            }

            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 2, true));
            }
        });
        return f;
    }

    public void mostrarNotificacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Sistema de Ventas", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cargarProductosCompletos() {
        listaProductos.clear();
        if (panelCardsContenedor != null) {
            panelCardsContenedor.removeAll();
        }
        try {
            List<ProductoDTO> productosBD = productoRepository.findAll();
            for (ProductoDTO p : productosBD) {
                // Obtenemos el precio directamente como BigDecimal
                BigDecimal precioBD = p.getPrice();

                // Si por alguna razón el precio viene nulo de la base de datos, le ponemos CERO
                if (precioBD == null) {
                    precioBD = BigDecimal.ZERO;
                }

                ProductoDTO item = new ProductoDTO(
                        p.getId(), // Asegúrate de pasar el ID primero
                        p.getSku(),
                        p.getName() != null ? p.getName() : "Sin nombre",
                        precioBD,
                        p.getImagePath(),
                        p.isGranel()
                );

                // Importante: Si tu ProductoDTO tiene el booleano isGranel, asegúrate de pasarlo también
                item.setGranel(p.isGranel());

                // Aseguramos que la ruta de la imagen se mantenga asignada en el DTO del item
                item.setImagePath(p.getImagePath());

                listaProductos.add(item);
            }
            renderizarCards(listaProductos);
        } catch (Exception e) {
            System.err.println("Error al cargar productos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderizarCards(List<ProductoDTO> productos) {
        panelCardsContenedor.removeAll();
        for (int i = 0; i < productos.size(); i++) {
            panelCardsContenedor.add(new CardProducto(productos.get(i), i));
        }
        panelCardsContenedor.revalidate();
        panelCardsContenedor.repaint();
    }

    private void filtrarCatalogo(String t) {
        List<ProductoDTO> filtrados = new ArrayList<>();
        for (ProductoDTO p : listaProductos) {
            if (p.getName().toLowerCase().contains(t.toLowerCase())) {
                filtrados.add(p);
            }
        }
        Collections.sort(filtrados, Comparator.comparing(p -> p.getName()));
        renderizarCards(filtrados);
    }

    public void agregarAlCarrito(ProductoDTO p, BigDecimal cantidad) {
        if (p == null || cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (!p.isGranel()) {
            cantidad = cantidad.setScale(0, RoundingMode.DOWN);
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                cantidad = BigDecimal.ONE;
            }
        }

        boolean existe = false;

        for (Component c : panelCarritoContenedor.getComponents()) {
            if (c instanceof ItemProductoCarrito icv) {
                if (icv.getProducto().getSku().equals(p.getSku())) {
                    BigDecimal nuevaCantidad = icv.getCantidad().add(cantidad);
                    icv.setCantidad(nuevaCantidad);
                    icv.revalidate();
                    icv.repaint();
                    existe = true;
                    break;
                }
            }
        }

        if (!existe) {
            ItemProductoCarrito nuevo = new ItemProductoCarrito(p);
            nuevo.setCantidad(cantidad);
            panelCarritoContenedor.add(nuevo, 0);
            panelCarritoContenedor.revalidate();
            panelCarritoContenedor.repaint();
            nuevo.requestFocusInWindow();
        }
        calcularTotales();
        BigDecimal importeParaNotificar = p.getPrice().multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
        mostrarNotificacion(p, importeParaNotificar);
    }

    // 1. Modifica el método para que reciba el importe calculado
    private void mostrarNotificacion(ProductoDTO p, BigDecimal importeReal) {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) {
            ProductoDTO pTemporal = new ProductoDTO();
            pTemporal.setName(p.getName());
            pTemporal.setPrice(importeReal);
            pTemporal.setImagePath(p.getImagePath());
            productoFrom.NotificationInterfice toast
                    = new productoFrom.NotificationInterfice(win, "Agregado: " + p.getName(), pTemporal);
            toast.setVisible(true);
        }
    }

    public static void calcularTotales() {
        BigDecimal total = BigDecimal.ZERO;

        // 1. Recorremos el carrito y sumamos
        for (Component c : panelCarritoContenedor.getComponents()) {
            if (c instanceof ItemProductoCarrito icv) {
                BigDecimal precio = icv.getProducto().getPrice();
                BigDecimal cantidad = icv.getCantidad();

                if (precio != null && cantidad != null) {
                    // REDONDEO CRUCIAL: Calculamos el importe del renglón y lo redondeamos a 2 decimales
                    // Esto hace que 19.98222 se convierta en 20.00 antes de sumarse al total general
                    BigDecimal importe = precio.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
                    total = total.add(importe);
                }
            }
        }

        // 2. Cálculos de impuestos (IVA 16%)
        BigDecimal divisorIVA = new BigDecimal("1.16");
        // El subtotal se saca del total ya redondeado
        BigDecimal subtotal = total.divide(divisorIVA, 2, RoundingMode.HALF_UP);
        BigDecimal iva = total.subtract(subtotal);

        // 3. Actualizamos la interfaz (Labels)
        lblSubtotal.setText(String.format("$ %.2f", subtotal));
        lblIVA.setText(String.format("$ %.2f", iva));

        // 4. ACTUALIZACIÓN DEL BOTÓN
        // Usamos compareTo para verificar si hay dinero mayor a cero
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            // Formateamos el total para que el botón muestre exactamente lo mismo que los items
            btnFinalizar.setText(String.format("PAGAR: $ %.2f MXN", total));
            btnFinalizar.setEnabled(true);
            btnFinalizar.setBackground(new Color(34, 197, 94)); // Verde
            btnFinalizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            btnFinalizar.setText("$ 0.00 MXN");
            btnFinalizar.setEnabled(false);
            btnFinalizar.setBackground(new Color(200, 200, 200)); // Gris
            btnFinalizar.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        // Refrescar componentes para asegurar que el cambio se vea
        btnFinalizar.revalidate();
        btnFinalizar.repaint();
    }

    private void finalizarVenta() {
        if (panelCarritoContenedor.getComponentCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }

        BigDecimal totalVentaActual = BigDecimal.ZERO;
        List<ProductoDTO> productosEnCarrito = new java.util.ArrayList<>();

        for (Component c : panelCarritoContenedor.getComponents()) {
            if (c instanceof ItemProductoCarrito icv) {
                totalVentaActual = totalVentaActual.add(
                        icv.getProducto().getPrice().multiply(icv.getCantidad())
                );

                // --- CORRECCIÓN AQUÍ ---
                // Obtenemos el producto y le asignamos explícitamente su cantidad real
                ProductoDTO productoConCantidad = icv.getProducto();
                productoConCantidad.setCantidad(icv.getCantidad());

                productosEnCarrito.add(productoConCantidad);
            }
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);

        // --- OBTENCIÓN Y VALIDACIÓN REAL DEL CLIENTE SELECCIONADO ---
        ClienteDTO clienteActual = null;
        boolean esPublicoGeneral = true;
        BigDecimal creditoDisponible = BigDecimal.ZERO;

        try {
            clientInterface.ItemClienteCombo itemSeleccionado
                    = (clientInterface.ItemClienteCombo) comboClientes.getSelectedItem();

            if (itemSeleccionado != null && itemSeleccionado.getId() != null) {
                // Consulta a la BD por ID
                clienteActual = clienteServicio.obtenerClientePorId(itemSeleccionado.getId());

                if (clienteActual != null) {
                    BigDecimal limite = clienteActual.getLimiteCredito() != null ? clienteActual.getLimiteCredito() : BigDecimal.ZERO;
                    BigDecimal deuda = clienteActual.getDeudaActual() != null ? clienteActual.getDeudaActual() : BigDecimal.ZERO;
                    creditoDisponible = limite.subtract(deuda);

                    // Validamos SOLO por nombre para que el ID 1 no se bloquee como público general
                    String nombreCliente = clienteActual.getNombre() != null ? clienteActual.getNombre().toLowerCase().trim() : "";
                    esPublicoGeneral = nombreCliente.contains("público general")
                            || nombreCliente.contains("publico general")
                            || nombreCliente.isEmpty();
                }
            }
        } catch (Exception ex) {
            esPublicoGeneral = true;
            creditoDisponible = BigDecimal.ZERO;
        }

        // Instancias el diálogo con los datos listos
        DialogoPago dialogo = new DialogoPago(parentWindow, totalVentaActual, (List<?>) productosEnCarrito);

        // Pasas el estado real una sola vez
        dialogo.setClienteActual(esPublicoGeneral, creditoDisponible);

        dialogo.setVisible(true);

        if (dialogo.isVentaConfirmada()) {
            try {
                String comentariosVenta = txtComentarios.getText().isEmpty() ? "Venta en mostrador" : txtComentarios.getText();
                Map<String, BigDecimal> desglosePagos = dialogo.obtenerDesglosePagos();

                if (txtComentarios != null) {
                    txtComentarios.setText("");
                }

                // Generamos el UUID / Folio único
                String uuidGenerado = "NV-" + System.currentTimeMillis();

                // --- 1. OBTENER Y VALIDAR EL MONTO A CRÉDITO (CORREGIDO PARA LIMITAR AL TOTAL) ---
                BigDecimal montoCredito = desglosePagos.getOrDefault("CREDITO", BigDecimal.ZERO);

                // Si el monto a crédito ingresado es mayor al total de la venta, lo ajustamos al total exacto
                if (montoCredito.compareTo(totalVentaActual) > 0) {
                    montoCredito = totalVentaActual;
                    desglosePagos.put("CREDITO", montoCredito); // Actualizamos el mapa de pagos
                }

                // Validación A: ¿Intenta comprar a crédito el Público General?
                if (montoCredito.compareTo(BigDecimal.ZERO) > 0 && esPublicoGeneral) {
                    JOptionPane.showMessageDialog(parentWindow,
                            "No se puede registrar saldo a crédito para 'Público General'. Por favor, selecciona un cliente válido.",
                            "Restricción de Crédito", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Validación B: ¿El monto a crédito supera el crédito disponible del cliente?
                if (montoCredito.compareTo(BigDecimal.ZERO) > 0) {
                    if (montoCredito.compareTo(creditoDisponible) > 0) {
                        JOptionPane.showMessageDialog(parentWindow,
                                "El monto a crédito ($" + montoCredito + ") supera el límite disponible del cliente ($" + creditoDisponible + ").",
                                "Crédito Insuficiente", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                // ------------------------------------------------------------------

                // --- CÁLCULO PRECISO Y CORREGIDO DEL EFECTIVO Y CAMBIO ---
                BigDecimal efectivoRecibido = desglosePagos.getOrDefault("EFECTIVO", BigDecimal.ZERO);

                // Si el cliente pagó en efectivo un monto mayor al total (o al resto que no fue a crédito)
                BigDecimal cambioFinal = BigDecimal.ZERO;
                if (efectivoRecibido.compareTo(totalVentaActual) > 0) {
                    cambioFinal = efectivoRecibido.subtract(totalVentaActual);
                } else if (dialogo.getCambio() != null && dialogo.getCambio().compareTo(BigDecimal.ZERO) > 0) {
                    cambioFinal = dialogo.getCambio();
                }

                CambioDTO nodoCambio = new CambioDTO();
                nodoCambio.setEfectivoRecibido(efectivoRecibido);
                nodoCambio.setCambioDevuelto(cambioFinal);

                VentaDTO ventaDto = new VentaDTO();
                ventaDto.setUuidVenta(uuidGenerado);
                ventaDto.setFechaVenta(LocalDate.now());
                ventaDto.setHoraVenta(LocalTime.now());

                ventaDto.setCambio(nodoCambio);
                ventaDto.setCliente(clienteActual);
                ventaDto.setProductos(productosEnCarrito);
                ventaDto.setFormasPagoMap(desglosePagos);
                ventaDto.setComentarios(comentariosVenta);
                ventaDto.setTotalVenta(totalVentaActual);

                // --- CONFIGURAR SI LA VENTA ES A CRÉDITO (Si lleva parte o todo en crédito) ---
                boolean esCreditoVenta = montoCredito.compareTo(BigDecimal.ZERO) > 0 || dialogo.isEsCredito();
                ventaDto.setEsCredito(esCreditoVenta);
                // -----------------------------------------------------------------------------

                if (clienteActual != null && clienteActual.getId() != null) {
                    ventaDto.setClienteId(clienteActual.getId());
                }

                String folioGenerado = ventaServicio.registrarVentaCompleta(ventaDto);

                // Limpieza visual de la pantalla
                panelCarritoContenedor.removeAll();
                panelCarritoContenedor.revalidate();
                panelCarritoContenedor.repaint();
                calcularTotales();
                txtBuscador.setText("");

                // --- RESTABLECER CLIENTE A PÚBLICO GENERAL ÚNICAMENTE TRAS VENTA EXITOSA ---
                if (comboClientes != null) {
                    comboClientes.removeAllItems();
                    comboClientes.addItem(new clientInterface.ItemClienteCombo(null, "Público General"));
                    comboClientes.setSelectedIndex(0);
                }
                txtBuscador.requestFocusInWindow();

                // Mostramos el diálogo con el folio registrado y el cambio exacto
                DialogoExitoVenta exito = new DialogoExitoVenta(parentWindow, folioGenerado, cambioFinal);
                exito.setVisible(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar la venta: " + ex.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* ========== TARJETA CATÁLOGO MODIFICADA ========== */
    class CardProducto extends JPanel {

        private void finalizarVenta() {
            if (panelCarritoContenedor.getComponentCount() == 0) {
                JOptionPane.showMessageDialog(this, "El carrito está vacío.");
                return;
            }

            BigDecimal totalVentaActual = BigDecimal.ZERO;
            List<ProductoDTO> productosEnCarrito = new java.util.ArrayList<>();

            for (Component c : panelCarritoContenedor.getComponents()) {
                if (c instanceof ItemProductoCarrito icv) {
                    // Sustituimos: totalVentaActual += precio * cantidad;
                    totalVentaActual = totalVentaActual.add(
                            icv.getProducto().getPrice().multiply(icv.getCantidad())
                    );
                    productosEnCarrito.add(icv.getProducto());
                }
            }

            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            DialogoPago dialogo = new DialogoPago(parentWindow, totalVentaActual, (List<?>) productosEnCarrito);
            dialogo.setVisible(true);

            if (dialogo.isVentaConfirmada()) {
                // Aquí puedes poner tu lógica real de base de datos
                String folioGenerado = "NV-" + System.currentTimeMillis() / 1000;
                BigDecimal cambioFinal = dialogo.getCambio();

                panelCarritoContenedor.removeAll();
                panelCarritoContenedor.revalidate();
                panelCarritoContenedor.repaint();
                calcularTotales();
                txtBuscador.setText("");
                txtBuscador.requestFocusInWindow();

                // FORMA CORRECTA:
                DialogoExitoVenta exito = new DialogoExitoVenta(parentWindow, folioGenerado, cambioFinal);
                exito.setVisible(true);
            }
        }

        private void buscarYAgregarDirecto() {
            String query = txtBuscadorDirecto.getText().trim().toLowerCase();

            if (query.isEmpty()) {
                return;
            }

            ProductoDTO encontrado = null;

            // 1. Buscamos en la lista maestra por SKU o por Nombre exacto
            for (ProductoDTO p : listaProductos) {
                if (p.getSku().toLowerCase().equals(query) || p.getName().toLowerCase().equals(query)) {
                    encontrado = p;
                    break;
                }
            }

            if (encontrado != null) {
                // 2. CORRECCIÓN: Usamos alSeleccionarProducto para que si es a granel, 
                // abra el modal de monto en dinero en lugar de agregar 1 unidad automáticamente.
                alSeleccionarProducto(encontrado);

                // 3. Limpieza y enfoque para el siguiente escaneo
                txtBuscadorDirecto.setText("");
                txtBuscadorDirecto.requestFocusInWindow();
            } else {
                // 4. Feedback si el SKU no existe
                JOptionPane.showMessageDialog(this,
                        "No se encontró ningún producto con el nombre o SKU: " + query,
                        "Producto no encontrado",
                        JOptionPane.ERROR_MESSAGE);
                txtBuscadorDirecto.selectAll();
            }
        }

        int index;

        CardProducto(ProductoDTO p, int idx) {
            this.index = idx;
            setLayout(new BorderLayout(5, 5));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            setPreferredSize(new Dimension(180, 240)); // Un poco más alto
            setFocusable(true);

            // --- BOTÓN DE INFO (Arriba a la derecha) ---
            JButton btnInfo = new JButton("\u24D8"); // Código Unicode para ⓘ
            btnInfo.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
            btnInfo.setForeground(new Color(0, 102, 204));
            btnInfo.setBorderPainted(false);
            btnInfo.setContentAreaFilled(false);
            btnInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnInfo.setToolTipText("Ver detalles completos");

            btnInfo.addActionListener(e -> {
                Window parent = SwingUtilities.getWindowAncestor(this);
                new DialogoDetalleProducto(parent, p).setVisible(true);
            });

            JPanel pSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pSuperior.setOpaque(false);
            pSuperior.add(btnInfo);
            add(pSuperior, BorderLayout.NORTH);

            // --- IMAGEN ---
            JLabel img = new JLabel(cargarImagen(p.getImagePath(), 85, 85, p.getName()));
            img.setHorizontalAlignment(JLabel.CENTER);
            add(img, BorderLayout.CENTER);

            // --- INFO INFERIOR (SKU, Nombre, Precio) ---
            JPanel pInfo = new JPanel(new GridLayout(3, 1, 2, 2));
            pInfo.setOpaque(false);
            pInfo.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

            JLabel lblSku = new JLabel("SKU: " + p.getSku(), SwingConstants.CENTER);
            lblSku.setFont(new Font("Arial", Font.ITALIC, 11));
            lblSku.setForeground(Color.GRAY);

            JLabel lblN = new JLabel(p.getName(), SwingConstants.CENTER);
            lblN.setFont(new Font("Arial", Font.BOLD, 14));

            JLabel lblP = new JLabel("$" + String.format("%.2f", p.getPrice()), SwingConstants.CENTER);
            lblP.setFont(new Font("Arial", Font.BOLD, 16));
            lblP.setForeground(new Color(0, 102, 204));

            pInfo.add(lblSku);
            pInfo.add(lblN);
            pInfo.add(lblP);
            add(pInfo, BorderLayout.SOUTH);

            // Resto de tus listeners (Mouse, Focus, Key) se quedan igual...
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    alSeleccionarProducto(p);
                }
            });
        }
    }

    public static ImageIcon cargarImagen(String ruta, int w, int h, String t) {
        File archivo = null;

        // 1. Intentar con la ruta directa si viene especificada
        if (ruta != null && !ruta.trim().isEmpty()) {
            String rutaLimpia = ruta.trim().replace("\"", "");
            File fDirecto = new File(rutaLimpia);
            if (fDirecto.exists() && fDirecto.isFile()) {
                archivo = fDirecto;
            }
        }

        // 2. Si no se encontró por ruta directa, buscar usando el SKU o nombre base en las carpetas reales del proyecto
        if (archivo == null && t != null && !t.trim().isEmpty()) {
            String nombreBuscado = new File(ruta != null && !ruta.isEmpty() ? ruta : t).getName();
            // Si 't' es la inicial o texto, aseguramos buscar por el nombre base o SKU
            String[] extensiones = {".png", ".jpg", ".jpeg", ".webp", ".jfif", ".PNG", ".JPG", ".JPEG"};

            // Carpetas reales donde están tus imágenes
            File[] carpetasPosibles = {
                new File("imagenes"),
                new File("src/main/resources/images"),
                new File(System.getProperty("user.dir"), "imagenes"),
                new File(System.getProperty("user.dir"), "src/main/resources/images")
            };

            for (File carpeta : carpetasPosibles) {
                if (carpeta.exists() && carpeta.isDirectory()) {
                    // Probamos si el archivo se llama exactamente como la ruta traía o como el SKU/nombre
                    for (String ext : extensiones) {
                        File fPrueba = new File(carpeta, nombreBuscado.contains(".") ? nombreBuscado : nombreBuscado + ext);
                        if (fPrueba.exists() && fPrueba.isFile()) {
                            archivo = fPrueba;
                            break;
                        }
                    }
                }
                if (archivo != null) {
                    break;
                }
            }
        }

        // 3. Si el archivo físico por fin existe, lo cargamos y escalamos
        if (archivo != null && archivo.exists() && archivo.isFile()) {
            try {
                ImageIcon iconOriginal = new ImageIcon(archivo.getAbsolutePath());
                if (iconOriginal.getIconWidth() > 0) {
                    Image imgEscalada = iconOriginal.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    return new ImageIcon(imgEscalada);
                }
            } catch (Exception e) {
                System.err.println("Error cargando imagen desde archivo [" + archivo.getAbsolutePath() + "]: " + e.getMessage());
            }
        }

        // Si de plano no se encontró nada, genera el placeholder vectorial
        return generarImagenPlaceholder(w, h, t);
    }

    private static ImageIcon generarImagenPlaceholder(int w, int h, String t) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(230, 235, 240)); // Gris suave moderno
        g.fillRoundRect(0, 0, w, h, 15, 15);

        g.setColor(new Color(110, 116, 125));
        g.setFont(new Font("Segoe UI", Font.BOLD, w / 2));

        String inicial = (t != null && !t.isEmpty()) ? t.substring(0, 1).toUpperCase() : "?";

        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(inicial)) / 2;
        int y = (h + fm.getAscent() - fm.getDescent()) / 2;

        g.drawString(inicial, x, y);
        g.dispose();

        return new ImageIcon(bi);
    }
}
