package ventas;

import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.management.Notification;
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
                    // Ejecuta el clic del botón verde
                    btnFinalizar.doClick();
                } else {
                    // Feedback de error si el carrito está vacío
                    txtBuscador.putClientProperty("JComponent.outline", "error");
                    Timer timer = new Timer(500, ex -> txtBuscador.putClientProperty("JComponent.outline", null));
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        };

// 2. Registramos la tecla ESCAPE a nivel de PANEL (para que funcione en toda la ventana de ventas)
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

        // --- 3. PANEL CARRITO ---
        JPanel panelCheckout = new JPanel(new BorderLayout());
        panelCheckout.setPreferredSize(new Dimension(520, 0));
        panelCheckout.setBackground(white);
        panelCheckout.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, borderSoft));

        // 1. DEFINICIÓN DEL BOTÓN CON ESTILO
        btnFinalizar = new JButton("$ 0.00 MXN");
        btnFinalizar.setBackground(new Color(200, 200, 200));
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        btnFinalizar.setPreferredSize(new Dimension(0, 70));
        btnFinalizar.setBorder(null);
        btnFinalizar.setFocusPainted(false);
        btnFinalizar.setEnabled(false); // Empieza bloqueado
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
                    btnFinalizar.setBackground(new Color(34, 197, 94)); // Verde Esmeralda
                    btnFinalizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    btnFinalizar.setEnabled(false);
                    btnFinalizar.setBackground(new Color(200, 200, 200)); // Gris
                    btnFinalizar.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            } catch (Exception ex) {
                btnFinalizar.setEnabled(false);
            }
        });

        panelCarritoContenedor = new JPanel();
        panelCarritoContenedor.setLayout(new BoxLayout(panelCarritoContenedor, BoxLayout.Y_AXIS));
        panelCarritoContenedor.setBackground(white);

        JScrollPane scrollCarrito = new JScrollPane(panelCarritoContenedor);
        scrollCarrito.setBorder(null);
        scrollCarrito.getViewport().setBackground(white);

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

        //  panelCheckout.add(lblTit, BorderLayout.NORTH);
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
                        p.getSku(),
                        p.getName() != null ? p.getName() : "Sin nombre",
                        precioBD,
                        p.getImagePath(),
                        p.isGranel());

                // Importante: Si tu ProductoDTO tiene el booleano isGranel, asegúrate de pasarlo también
                item.setGranel(p.isGranel());

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

    /* ========== TARJETA CATÁLOGO MODIFICADA ========== */
    class CardProducto extends JPanel {

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
        // 1. SI TIENE IMAGEN: Intentar cargarla desde la ruta
        if (ruta != null && !ruta.isEmpty()) {
            java.io.File archivo = new java.io.File(ruta);
            if (archivo.exists()) {
                ImageIcon iconOriginal = new ImageIcon(ruta);
                // Ajustamos la imagen al tamaño del contenedor (w, h)
                Image imgEscalada = iconOriginal.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(imgEscalada);
            }
        }

        // 2. SI NO TIENE IMAGEN (O NO EXISTE): Usar tu código original de la inicial
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(230, 235, 240));
        g.fillRoundRect(0, 0, w, h, 8, 8);
        g.setColor(new Color(110, 110, 110));
        g.setFont(new Font("Segoe UI", Font.BOLD, w / 2));

        // Manejo de error si el nombre viene vacío para que no truene el substring
        String s = (t != null && !t.isEmpty()) ? t.substring(0, 1).toUpperCase() : "?";

        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (w - fm.stringWidth(s)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
        g.dispose();

        return new ImageIcon(bi);
    }
}
