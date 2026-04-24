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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import tools.DialogoPago;
import tools.DialogoPago.DialogoExitoVenta;

@org.springframework.stereotype.Component
public class ventas extends JPanel {

    @org.springframework.beans.factory.annotation.Autowired
    private ProductoRepository productoRepository;

    private JTextField txtBuscador;
    private JPanel panelCarritoContenedor;
    private JPanel panelCardsContenedor;
    private JLabel lblSubtotal, lblIVA;
    private JButton btnFinalizar;
    private JTextField txtBuscadorDirecto;

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
        wrapperCatalogo.add(panelCardsContenedor, BorderLayout.NORTH); // Lo pega arriba
        JScrollPane scrollCatalogo = new JScrollPane(wrapperCatalogo);
        scrollCatalogo.setBorder(null);
        scrollCatalogo.getViewport().setBackground(bgMain);
        scrollCatalogo.getVerticalScrollBar().setUnitIncrement(35);

// Bloqueamos el scroll horizontal
        scrollCatalogo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

// --- AJUSTE DINÁMICO DE TAMAÑO ---
// Este pequeño listener es vital: asegura que el panel de tarjetas 
// siempre mida lo mismo que el ancho del scroll para que el GridLayout calcule bien los espacios.
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

        // 2. Configura el buscador así:
        txtBuscador = new tools.PlaceholderTextField("");
        txtBuscador.setPreferredSize(new Dimension(380, 45));
        txtBuscador.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtBuscador.putClientProperty("JTextField.placeholderText", " ¿Qué producto buscas hoy?...");
        txtBuscador.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon()); // Pone la lupa a la izquierda
        txtBuscador.putClientProperty("JTextField.leadingIconGap", 10); // Espacio entre lupa y texto
        txtBuscador.putClientProperty("JComponent.roundRect", true);
        txtBuscador.setBackground(new Color(245, 247, 251));

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
            // --- COLORES EXACTOS DEL DISEÑO PROFESIONAL ---
            java.awt.Color COLOR_BORDE_SOFT = new java.awt.Color(235, 235, 235);
            java.awt.Color COLOR_GRIS_IMAGEN = new java.awt.Color(236, 240, 241);
            java.awt.Color COLOR_TEXTO_SKU = new java.awt.Color(37, 99, 235);
            java.awt.Color COLOR_PRECIO = new java.awt.Color(34, 197, 94);
            java.awt.Color COLOR_BOTON_ENTENDIDO = new java.awt.Color(51, 60, 68);

            // 1. Panel Principal (Contenedor con bordes redondeados simulados)
            JPanel modal = new JPanel(new java.awt.GridBagLayout());
            modal.setBackground(java.awt.Color.WHITE);
            modal.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();

            // 2. Cuadro de Imagen (Lado Izquierdo)
            JPanel panelImagen = new JPanel(new java.awt.GridBagLayout());
            panelImagen.setPreferredSize(new java.awt.Dimension(160, 160));
            panelImagen.setBackground(COLOR_GRIS_IMAGEN);
            JLabel lblLetra = new JLabel(p.getName().substring(0, 1).toUpperCase());
            lblLetra.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 60));
            lblLetra.setForeground(new java.awt.Color(160, 174, 192));
            panelImagen.add(lblLetra);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridheight = 2;
            gbc.insets = new java.awt.Insets(0, 0, 0, 25);
            modal.add(panelImagen, gbc);

            // 3. Panel de Información (Lado Derecho)
            JPanel info = new JPanel();
            info.setLayout(new javax.swing.BoxLayout(info, javax.swing.BoxLayout.Y_AXIS));
            info.setBackground(java.awt.Color.WHITE);

            // SKU Tag
            JLabel lblSku = new JLabel(" SKU: " + p.getSku() + " ");
            lblSku.setOpaque(true);
            lblSku.setBackground(new java.awt.Color(239, 246, 255));
            lblSku.setForeground(COLOR_TEXTO_SKU);
            lblSku.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));

            // Nombre y Precio
            JLabel lblNombre = new JLabel(p.getName().toLowerCase());
            lblNombre.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
            JLabel lblPrecio = new JLabel("$" + p.getPrice() + " MXN");
            lblPrecio.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
            lblPrecio.setForeground(COLOR_PRECIO);

            // Campo de Monto (Input estilizado)
            JTextField txtDinero = new JTextField();
            txtDinero.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
            txtDinero.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)), "MONTO A PAGAR ($)"));

            info.add(lblSku);
            info.add(javax.swing.Box.createVerticalStrut(10));
            info.add(lblNombre);
            info.add(lblPrecio);
            info.add(javax.swing.Box.createVerticalStrut(15));
            info.add(new JLabel("<html><font color='#888888' size='2'>DESCRIPCIÓN:</font><br>Venta por kilo</html>"));
            info.add(javax.swing.Box.createVerticalStrut(15));
            info.add(txtDinero);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridheight = 1;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            modal.add(info, gbc);

            // 4. Configuración del Dialogo y Botones
            UIManager.put("Button.background", COLOR_BOTON_ENTENDIDO);
            UIManager.put("Button.foreground", java.awt.Color.WHITE);
            UIManager.put("Button.font", new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));

            javax.swing.SwingUtilities.invokeLater(() -> txtDinero.requestFocusInWindow());

            int result = JOptionPane.showOptionDialog(this, modal, "",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new Object[]{"ENTENDIDO", "CANCELAR"}, "ENTENDIDO");

            if (result == JOptionPane.OK_OPTION && !txtDinero.getText().isEmpty()) {
                try {
                    java.math.BigDecimal dinero = new java.math.BigDecimal(txtDinero.getText().replaceAll("[^0-9.]", ""));
                    java.math.BigDecimal cantidad = dinero.divide(p.getPrice(), 3, java.math.RoundingMode.HALF_UP);
                    agregarAlCarrito(p, cantidad);
                } catch (Exception e) {
                    /* Manejar error */ }
            }
        } else {
            agregarAlCarrito(p, java.math.BigDecimal.ONE);
        }
    }

    public void mostrarNotificacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Sistema de Ventas", JOptionPane.INFORMATION_MESSAGE);
    }

    class Notification extends JDialog {

        // --- CLAVE: Variable estática para rastrear la notificación activa ---
        private static Notification instanciaActual = null;
        private int indiceImagenActual = 0;

        public Notification(Window parent, String mensaje, ProductoDTO p) {
            super(parent);

            // --- LÓGICA DE REEMPLAZO ---
            // Si ya hay una notificación mostrándose, la cerramos de golpe
            if (instanciaActual != null && instanciaActual.isVisible()) {
                instanciaActual.dispose();
            }
            // Registramos esta nueva notificación como la actual
            instanciaActual = this;

            setUndecorated(true);
            setLayout(new BorderLayout());
            setBackground(new Color(0, 0, 0, 0));
            setFocusableWindowState(false);
            setAlwaysOnTop(true);

            Color colorPrimario = new Color(0, 102, 255);
            Color colorExito = new Color(40, 167, 69);

            JPanel mainPanel = new JPanel(new BorderLayout(0, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 30, 30);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(3, 3, getWidth() - 10, getHeight() - 10, 30, 30);
                    g2.setColor(colorPrimario);
                    g2.fillRoundRect(3, 3, 12, getHeight() - 10, 30, 30);
                    g2.fillRect(10, 3, 5, getHeight() - 10);
                    g2.dispose();
                }
            };

            // Cabecera
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15));
            header.setOpaque(false);
            JLabel lblCheck = new JLabel("🛒 " + mensaje);
            lblCheck.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblCheck.setForeground(colorPrimario);
            header.add(lblCheck);

            // Cuerpo (Imagen + Info)
            JPanel body = new JPanel(new BorderLayout(15, 0));
            body.setOpaque(false);
            body.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 30));

            JLabel lblFoto = new JLabel(cargarImagen(p.getImagePath(), 100, 100, p.getName()));
            lblFoto.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240), 1, true));
            lblFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Hover para carrusel de imágenes
            lblFoto.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    String pathCompleto = p.getImagePath();
                    if (pathCompleto != null && !pathCompleto.isEmpty()) {
                        String[] rutas = pathCompleto.split(",");
                        if (rutas.length > 1) {
                            indiceImagenActual = (indiceImagenActual + 1) % rutas.length;
                            String nuevaRuta = rutas[indiceImagenActual].trim();
                            lblFoto.setIcon(cargarImagen(nuevaRuta, 100, 100, p.getName()));
                        }
                    }
                }
            });

            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 0));
            infoPanel.setOpaque(false);

            JLabel name = new JLabel(p.getName());
            name.setFont(new Font("Segoe UI", Font.BOLD, 14));

            JLabel price = new JLabel("$" + String.format("%.2f", p.getPrice()));
            price.setFont(new Font("Segoe UI", Font.BOLD, 22));
            price.setForeground(colorExito);

            infoPanel.add(name);
            infoPanel.add(price);

            body.add(lblFoto, BorderLayout.WEST);
            body.add(infoPanel, BorderLayout.CENTER);

            mainPanel.add(header, BorderLayout.NORTH);
            mainPanel.add(body, BorderLayout.CENTER);
            add(mainPanel);
            pack();

            // --- UBICACIÓN CORREGIDA PARA NO TAPAR EL CARRITO ---
            if (parent != null) {
                // Aparece a la izquierda de la zona del carrito
                int x = parent.getX() + parent.getWidth() - getWidth() - 340;
                int y = parent.getY() + parent.getHeight() - getHeight() - 30;
                setLocation(x, y);
            }

            // Timer de auto-cierre (3 segundos es suficiente si se actualizan rápido)
            Timer timer = new Timer(3000, e -> dispose());
            timer.setRepeats(false);
            timer.start();
        }
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
                java.math.BigDecimal precioBD = p.getPrice();

                // Si por alguna razón el precio viene nulo de la base de datos, le ponemos CERO
                if (precioBD == null) {
                    precioBD = java.math.BigDecimal.ZERO;
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

// Agregamos el parámetro BigDecimal cantidad
    public void agregarAlCarrito(ProductoDTO p, java.math.BigDecimal cantidad) {

        // Ya no inicializamos en 1 ni preguntamos isGranel aquí, 
        // porque 'alSeleccionarProducto' ya nos mandó la cantidad correcta.
        boolean existe = false;
        for (Component c : panelCarritoContenedor.getComponents()) {
            if (c instanceof ItemCarritoVisual icv) {
                if (icv.getProducto().getSku().equals(p.getSku())) {
                    // 1. Sumamos la cantidad que recibimos por parámetro
                    icv.setCantidad(icv.getCantidad().add(cantidad));

                    // 2. IMPORTANTE: Notificar al componente que se actualice visualmente
                    // (Esto hará que el subtotal de la fila se refresque)
                    icv.revalidate();
                    icv.repaint();

                    icv.requestFocusInWindow();
                    existe = true;
                    break;
                }
            }
        }

        if (!existe) {
            ItemCarritoVisual nuevo = new ItemCarritoVisual(p);
            // Seteamos la cantidad recibida
            nuevo.setCantidad(cantidad);

            panelCarritoContenedor.add(nuevo);
            panelCarritoContenedor.revalidate();
            panelCarritoContenedor.repaint();
            nuevo.requestFocusInWindow();
        }

        // 3. Recalcular el gran total de la venta
        calcularTotales();

        // Notificación Toast
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) {
            Notification toast = new Notification(win, "Agregado al Carrito", p);
            toast.setVisible(true);
        }
    }

    private void calcularTotales() {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (Component c : panelCarritoContenedor.getComponents()) {
            if (c instanceof ItemCarritoVisual icv) {
                // Convertimos el precio a BigDecimal
                BigDecimal precio = (icv.getProducto().getPrice());
                // Obtenemos la cantidad (que ya es BigDecimal)
                java.math.BigDecimal cantidad = icv.getCantidad();

                // Multiplicamos y sumamos al total acumulado
                java.math.BigDecimal importe = precio.multiply(cantidad);
                total = total.add(importe);
            }
        }

        // 2. Cálculos de IVA (1.16)
        java.math.BigDecimal divisorIVA = new java.math.BigDecimal("1.16");

        // Subtotal = total / 1.16
        java.math.BigDecimal subtotal = total.divide(divisorIVA, 2, java.math.RoundingMode.HALF_UP);

        // IVA = total - subtotal
        java.math.BigDecimal iva = total.subtract(subtotal);

        // 3. Pintamos en los labels (usamos doubleValue() solo para el formato del String)
        lblSubtotal.setText(String.format("$%.2f", subtotal.doubleValue()));
        lblIVA.setText(String.format("$%.2f", iva.doubleValue()));
        btnFinalizar.setText(String.format("PAGAR: $%.2f MXN", total.doubleValue()));
    }

    private void finalizarVenta() {
        if (panelCarritoContenedor.getComponentCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }

        BigDecimal totalVentaActual = BigDecimal.ZERO;
        List<ProductoDTO> productosEnCarrito = new java.util.ArrayList<>();

        for (Component c : panelCarritoContenedor.getComponents()) {
            if (c instanceof ItemCarritoVisual icv) {
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

        // Buscamos en la lista maestra por ID (SKU) o por Nombre exacto/parcial
        for (ProductoDTO p : listaProductos) {
            if (p.getSku().toLowerCase().equals(query) || p.getName().toLowerCase().equals(query)) {
                encontrado = p;
                break;
            }
        }

        if (encontrado != null) {
            agregarAlCarrito(encontrado, java.math.BigDecimal.ONE);
            txtBuscadorDirecto.setText(""); // Limpiar para el siguiente
            txtBuscadorDirecto.requestFocusInWindow();
        } else {
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
                    // Llamamos al "cerebro" para que decida si pide dinero o agrega 1
                    alSeleccionarProducto(p);
                }
            });
            // ... (continúa el código original)
        }
    }

    /* ========== ITEM CARRITO DINÁMICO ========== */
    class ItemCarritoVisual extends JPanel {

        private ProductoDTO producto;
        private JTextField txtCant;
        private JLabel lblImporte;

        public ItemCarritoVisual(ProductoDTO p) {
            this.producto = p;
            setLayout(new BorderLayout(15, 5));
            setBackground(Color.WHITE);
            setFocusable(true);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                }
            });
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            setMaximumSize(new Dimension(480, 85));

            txtCant = new JTextField("1");
            txtCant.setPreferredSize(new Dimension(40, 25));
            txtCant.setHorizontalAlignment(JTextField.CENTER);

            lblImporte = new JLabel(String.format("$%.2f", p.getPrice()));
            lblImporte.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblImporte.setPreferredSize(new Dimension(90, 25));
            lblImporte.setHorizontalAlignment(SwingConstants.RIGHT);

            JButton btnMenos = new JButton("-");
            JButton btnMas = new JButton("+");
            btnMenos.setFocusable(false);
            btnMas.setFocusable(false);

            JButton btnEliminar = new JButton(crearIconoBasura());
            btnEliminar.setFocusable(false);
            btnEliminar.setBorderPainted(false);
            btnEliminar.setContentAreaFilled(false);
            btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));

            Runnable eliminarAccion = () -> {
                panelCarritoContenedor.remove(this);
                panelCarritoContenedor.revalidate();
                panelCarritoContenedor.repaint();
                calcularTotales();
                txtBuscador.requestFocusInWindow();
            };
            btnEliminar.addActionListener(e -> eliminarAccion.run());

            ((AbstractDocument) txtCant.getDocument()).setDocumentFilter(new DocumentFilter() {
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if ((fb.getDocument().getText(0, fb.getDocument().getLength()).substring(0, offset) + text).matches("\\d{0,3}")) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });

            txtCant.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    act();
                }

                public void removeUpdate(DocumentEvent e) {
                    act();
                }

                public void changedUpdate(DocumentEvent e) {
                    act();
                }

                private void act() {
                    SwingUtilities.invokeLater(() -> {
                        actualizarImporte();
                        calcularTotales();
                    });
                }
            });

            btnMenos.addActionListener(e -> {
                java.math.BigDecimal actual = getCantidad();
                if (actual.compareTo(java.math.BigDecimal.ONE) > 0) {
                    setCantidad(actual.subtract(java.math.BigDecimal.ONE));
                }
            });

            btnMas.addActionListener(e -> {
                // Sumamos 1 usando .add()
                setCantidad(getCantidad().add(java.math.BigDecimal.ONE));
            });

            InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "borrar");
            am.put("borrar", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    eliminarAccion.run();
                }
            });

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "bajar");
            am.put("bajar", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    int index = getComponentIndex();
                    if (index + 1 < panelCarritoContenedor.getComponentCount()) {
                        moverFoco(index + 1);
                    } else {
                        txtBuscador.requestFocusInWindow();
                    }
                }
            });

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "subir");
            am.put("subir", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    int index = getComponentIndex();
                    if (index > 0) {
                        moverFoco(index - 1);
                    } else {
                        // Si es el primero, sube al buscador de SKU (carrito)
                        txtBuscadorDirecto.requestFocusInWindow();
                    }
                }
            });

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "izquierda");
            am.put("izquierda", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    // Salto directo al buscador de la izquierda (Catálogo)
                    txtBuscador.requestFocusInWindow();
                    txtBuscador.selectAll(); // Opcional: selecciona el texto para buscar rápido
                }
            });

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "derecha");
            am.put("derecha", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    // Se mantiene en el área del carrito saltando al buscador SKU
                    txtBuscadorDirecto.requestFocusInWindow();
                }
            });

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "entrar");
            am.put("entrar", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (txtCant.hasFocus()) {
                        txtBuscador.requestFocusInWindow();
                    } else {
                        txtCant.requestFocus();
                        txtCant.selectAll();
                    }
                }
            });

            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    setBackground(COLOR_SELECCION);
                }

                public void focusLost(FocusEvent e) {
                    setBackground(Color.WHITE);
                }
            });

            JPanel pMid = new JPanel(new GridLayout(2, 1));
            pMid.setOpaque(false);
            pMid.add(new JLabel(p.getName()) {
                {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
            });
            pMid.add(new JLabel("Unit: $" + p.getPrice()));

            JPanel pDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 12));
            pDer.setOpaque(false);
            pDer.add(btnMenos);
            pDer.add(txtCant);
            pDer.add(btnMas);
            pDer.add(lblImporte);

            JPanel pIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            pIzquierda.setOpaque(false);
            pIzquierda.add(btnEliminar);
            pIzquierda.add(new JLabel(cargarImagen(p.getImagePath(), 45, 45, p.getName())));

            add(pIzquierda, BorderLayout.WEST);
            add(pMid, BorderLayout.CENTER);
            add(pDer, BorderLayout.EAST);
        }

        public void setCantidad(java.math.BigDecimal n) {
            if (n == null) {
                n = java.math.BigDecimal.ZERO;
            }
            java.math.BigDecimal ajustado = n.setScale(3, java.math.RoundingMode.HALF_UP);
            txtCant.setText(ajustado.toPlainString());
            actualizarImporte();
        }

        public java.math.BigDecimal getCantidad() {
            try {
                String t = txtCant.getText().trim();
                return t.isEmpty() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(t);
            } catch (Exception e) {
                return java.math.BigDecimal.ZERO;
            }
        }

        public ProductoDTO getProducto() {
            return producto;
        }

        private void actualizarImporte() {
            BigDecimal precio = producto.getPrice();
            java.math.BigDecimal cantidad = getCantidad();
            java.math.BigDecimal total = precio.multiply(cantidad);
            lblImporte.setText(String.format("$%.2f", total.doubleValue()));
        }

        private int getComponentIndex() {
            Component[] comps = panelCarritoContenedor.getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i] == this) {
                    return i;
                }
            }
            return -1;
        }

        private void moverFoco(int i) {
            if (i >= 0 && i < panelCarritoContenedor.getComponentCount()) {
                panelCarritoContenedor.getComponent(i).requestFocusInWindow();
            } else if (i < 0) {
                txtBuscador.requestFocusInWindow();
            }
        }

        private Icon crearIconoBasura() {
            BufferedImage bi = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            g.setColor(new Color(220, 53, 69));
            g.fillRect(5, 7, 10, 9);
            g.fillRect(4, 5, 12, 2);
            g.fillRect(8, 3, 4, 2);
            g.dispose();
            return new ImageIcon(bi);
        }
    }

    private ImageIcon cargarImagen(String ruta, int w, int h, String t) {
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

    /* ========== DIÁLOGO DE DETALLES PREMIUM - ESTÁTICO E IMAGEN GRANDE ========== */
    class DialogoDetalleProducto extends JDialog {

        private int indiceImagenActual = 0;

        public DialogoDetalleProducto(java.awt.Window parent, ProductoDTO p) {
            super(parent, "Detalles del Producto", ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            setLayout(new BorderLayout());

            // Ventana amplia para la imagen de 350x350
            setSize(800, 500);
            setLocationRelativeTo(parent);
            setBackground(new Color(0, 0, 0, 0));

            // Panel Principal con Fondo White Smoke y Sombra
            JPanel mainPanel = new JPanel(new BorderLayout(40, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Sombra
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 35, 35);

                    // Fondo
                    g2.setColor(new Color(248, 249, 250));
                    g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 35, 35);
                    g2.dispose();
                }
            };
            mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

            // --- IZQUIERDA: IMAGEN GRANDE (350x350) ---
            JPanel panelImagen = new JPanel(new BorderLayout());
            panelImagen.setOpaque(false);

            String[] rutas = (p.getImagePath() != null && !p.getImagePath().isEmpty())
                    ? p.getImagePath().split(",")
                    : new String[]{""};

            JLabel lblImagen = new JLabel(cargarImagenInterna(rutas[0].trim(), 350, 350, p.getName()));
            lblImagen.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblImagen.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(225, 225, 225), 1, true),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));

            // Lógica Hover para cambio de imágenes
            lblImagen.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (rutas.length > 1) {
                        indiceImagenActual = (indiceImagenActual + 1) % rutas.length;
                        lblImagen.setIcon(cargarImagenInterna(rutas[indiceImagenActual].trim(), 350, 350, p.getName()));
                    }
                }
            });

            panelImagen.add(lblImagen, BorderLayout.NORTH);
            mainPanel.add(panelImagen, BorderLayout.WEST);

            // --- DERECHA: INFORMACIÓN APILADA ARRIBA ---
            JPanel pInfo = new JPanel();
            pInfo.setLayout(new BoxLayout(pInfo, BoxLayout.Y_AXIS));
            pInfo.setOpaque(false);

            // 1. SKU + COPIAR
            JPanel panelSKU = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            panelSKU.setOpaque(false);
            panelSKU.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblSKU = new JLabel("  SKU: " + p.getSku() + "  ");
            lblSKU.setOpaque(true);
            lblSKU.setBackground(new Color(230, 240, 255));
            lblSKU.setForeground(new Color(0, 102, 255));
            lblSKU.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblSKU.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 255), 1, true));

            JButton btnCopiar = new JButton("📋");
            btnCopiar.setPreferredSize(new Dimension(40, 26));
            btnCopiar.setFocusPainted(false);
            btnCopiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCopiar.setBackground(Color.WHITE);
            btnCopiar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, new Color(180, 200, 255)));
            btnCopiar.addActionListener(e -> {
                StringSelection ss = new StringSelection(p.getSku());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
                btnCopiar.setText("✅");
                new Timer(1000, ev -> btnCopiar.setText("📋")).start();
            });
            panelSKU.add(lblSKU);
            panelSKU.add(btnCopiar);

            // 2. NOMBRE (Se eliminó el ancho fijo de 300px para que se muestre completo)
            JLabel lblNom = new JLabel("<html><body>" + p.getName() + "</body></html>");
            lblNom.setFont(new Font("Segoe UI", Font.BOLD, 28));
            lblNom.setForeground(new Color(45, 52, 54));
            lblNom.setAlignmentX(Component.LEFT_ALIGNMENT);

            // 3. PRECIO
            JLabel lblPre = new JLabel("$" + String.format("%.2f", p.getPrice()) + " MXN");
            lblPre.setFont(new Font("Segoe UI", Font.BOLD, 32));
            lblPre.setForeground(new Color(40, 167, 69));
            lblPre.setAlignmentX(Component.LEFT_ALIGNMENT);

            // 4. DESCRIPCIÓN
            JLabel lblDescT = new JLabel("DESCRIPCIÓN:");
            lblDescT.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblDescT.setForeground(new Color(160, 160, 160));
            lblDescT.setAlignmentX(Component.LEFT_ALIGNMENT);

            JTextArea txtDesc = new JTextArea("Sin descripción.");
            txtDesc.setWrapStyleWord(true);
            txtDesc.setLineWrap(true);
            txtDesc.setEditable(false);
            txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtDesc.setOpaque(false);
            txtDesc.setForeground(new Color(80, 80, 80));
            txtDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

            pInfo.add(panelSKU);
            pInfo.add(Box.createVerticalStrut(15));
            pInfo.add(lblNom);
            pInfo.add(Box.createVerticalStrut(10));
            pInfo.add(lblPre);
            pInfo.add(Box.createVerticalStrut(25));
            pInfo.add(lblDescT);
            pInfo.add(Box.createVerticalStrut(8));
            pInfo.add(txtDesc);
            pInfo.add(Box.createVerticalGlue()); // Empuja todo hacia arriba

            mainPanel.add(pInfo, BorderLayout.CENTER);

            // --- BOTÓN INFERIOR ---
            JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panelBoton.setOpaque(false);
            JButton btnCerrar = new JButton("ENTENDIDO");
            btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnCerrar.setPreferredSize(new Dimension(140, 40));
            btnCerrar.setBackground(new Color(45, 52, 54));
            btnCerrar.setForeground(Color.WHITE);
            btnCerrar.setFocusPainted(false);
            btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCerrar.addActionListener(e -> dispose());
            panelBoton.add(btnCerrar);

            add(mainPanel, BorderLayout.CENTER);
            add(panelBoton, BorderLayout.SOUTH);
        }

        private ImageIcon cargarImagenInterna(String ruta, int w, int h, String nombre) {
            if (ruta != null && !ruta.isEmpty()) {
                java.io.File archivo = new java.io.File(ruta);
                if (archivo.exists()) {
                    return new ImageIcon(new ImageIcon(ruta).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
                }
            }
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(235, 238, 242));
            g.fillRoundRect(0, 0, w, h, 30, 30);
            g.setColor(new Color(170, 180, 190));
            g.setFont(new Font("Segoe UI", Font.BOLD, 90));
            String letra = (nombre != null && !nombre.isEmpty()) ? nombre.substring(0, 1).toUpperCase() : "?";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(letra, (w - fm.stringWidth(letra)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
            return new ImageIcon(bi);
        }
    }
}
