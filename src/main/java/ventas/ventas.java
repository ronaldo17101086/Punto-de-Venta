package ventas;

import com.mycompany.chancuellarpuntodeventa.services.dtos.Producto;
import com.mycompany.chancuellarpuntodeventa.services.repository.ProductoRepository;
import tools.PlaceholderTextField;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import tools.DialogoPago;

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

    private List<ProductoItem> listaProductos = new ArrayList<>();
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
        // --- CABECERA GLOBAL ---
        JPanel panelCabeceraGlobal = new JPanel(new BorderLayout(0, 0));
        panelCabeceraGlobal.setBackground(Color.WHITE);

// --- SECCIÓN IZQUIERDA (Catálogo + Botón Actualizar) ---
        JPanel panelIzquierdoCabecera = new JPanel(new BorderLayout(10, 0));
        panelIzquierdoCabecera.setBackground(Color.WHITE);
        panelIzquierdoCabecera.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        txtBuscador = new tools.PlaceholderTextField("Buscar en catálogo...");
        txtBuscador.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Listener para filtrar mientras escribes
        txtBuscador.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarCatalogo(txtBuscador.getText());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarCatalogo(txtBuscador.getText());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarCatalogo(txtBuscador.getText());
            }
        });
// NAVEGACIÓN DESDE BUSCADOR CATÁLOGO
        txtBuscador.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_DOWN -> { // Bajar al catálogo
                        if (panelCardsContenedor.getComponentCount() > 0) {
                            panelCardsContenedor.getComponent(0).requestFocusInWindow();
                        }
                    }
                    case java.awt.event.KeyEvent.VK_RIGHT -> { // Ir al buscador del carrito
                        txtBuscadorDirecto.requestFocusInWindow();
                    }
                }
            }
        });

        // Botón Actualizar (Refrescar)
        JButton btnActualizar = new JButton("🔄");
        btnActualizar.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btnActualizar.setToolTipText("Actualizar catálogo");
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.setBackground(new Color(240, 240, 240));
        btnActualizar.setFocusPainted(false);

// Al hacer clic, limpia el buscador y recarga todo
        btnActualizar.addActionListener(e -> {
            txtBuscador.setText("");
            cargarProductosCompletos();
            txtBuscador.requestFocusInWindow();
        });

// Unimos el buscador y el botón en un subpanel para que estén juntos al centro
        JPanel panelBusquedaMasBoton = new JPanel(new BorderLayout(5, 0));
        panelBusquedaMasBoton.setOpaque(false);
        panelBusquedaMasBoton.add(txtBuscador, BorderLayout.CENTER);
        panelBusquedaMasBoton.add(btnActualizar, BorderLayout.EAST);

        panelIzquierdoCabecera.add(new JLabel(" 🔍 "), BorderLayout.WEST);
        panelIzquierdoCabecera.add(panelBusquedaMasBoton, BorderLayout.CENTER);

        // --- SECCIÓN DERECHA (Buscador Directo al Carrito) ---
        JPanel panelDerechoCabecera = new JPanel(new BorderLayout(5, 0));
        panelDerechoCabecera.setBackground(Color.WHITE);
        panelDerechoCabecera.setPreferredSize(new Dimension(480, 0));
        panelDerechoCabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        txtBuscadorDirecto = new PlaceholderTextField("SKU o Nombre + ENTER para agregar...");
        txtBuscadorDirecto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscadorDirecto.addActionListener(e -> buscarYAgregarDirecto());

        // NAVEGACIÓN DESDE BUSCADOR CARRITO
        txtBuscadorDirecto.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_LEFT -> { // Volver al buscador de catálogo
                        txtBuscador.requestFocusInWindow();
                    }
                    case java.awt.event.KeyEvent.VK_DOWN -> { // Bajar a los items del carrito
                        if (panelCarritoContenedor.getComponentCount() > 0) {
                            panelCarritoContenedor.getComponent(0).requestFocusInWindow();
                        }
                    }
                }
            }
        });

        panelDerechoCabecera.add(new JLabel(" 🛒+ "), BorderLayout.WEST);
        panelDerechoCabecera.add(txtBuscadorDirecto, BorderLayout.CENTER);

        panelCabeceraGlobal.add(panelIzquierdoCabecera, BorderLayout.CENTER);
        panelCabeceraGlobal.add(panelDerechoCabecera, BorderLayout.EAST);
        add(panelCabeceraGlobal, BorderLayout.NORTH);

        // --- CONTENIDO CENTRAL ---
        JPanel contenedorPrincipal = new JPanel(new BorderLayout(5, 0));
        contenedorPrincipal.setOpaque(false);

        // Panel de tarjetas
        panelCardsContenedor = new JPanel(new GridLayout(0, COLUMNAS, 10, 10));
        panelCardsContenedor.setBackground(new Color(242, 244, 247));
        panelCardsContenedor.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel panelAjustador = new JPanel(new BorderLayout());
        panelAjustador.setBackground(new Color(242, 244, 247));
        panelAjustador.add(panelCardsContenedor, BorderLayout.NORTH);

        JScrollPane scrollCatalogo = new JScrollPane(panelAjustador);
        scrollCatalogo.setBorder(null);
        scrollCatalogo.getVerticalScrollBar().setUnitIncrement(25);

        // Carrito
        panelCarritoContenedor = new JPanel();
        panelCarritoContenedor.setLayout(new BoxLayout(panelCarritoContenedor, BoxLayout.Y_AXIS));
        panelCarritoContenedor.setBackground(Color.WHITE);

        JScrollPane scrollCarrito = new JScrollPane(panelCarritoContenedor);
        scrollCarrito.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 220, 220)));

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setPreferredSize(new Dimension(480, 0));
        panelDerecho.setBackground(Color.WHITE);

        JLabel lblTituloCarrito = new JLabel("  Carrito de Ventas", SwingConstants.LEFT);
        lblTituloCarrito.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloCarrito.setPreferredSize(new Dimension(0, 35));
        lblTituloCarrito.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, new Color(220, 220, 220)));

        panelDerecho.add(lblTituloCarrito, BorderLayout.NORTH);
        panelDerecho.add(scrollCarrito, BorderLayout.CENTER);

        // Panel Inferior (Totales)
        JPanel panelInfDer = new JPanel(new BorderLayout());
        panelInfDer.setBackground(Color.WHITE);

        JPanel panelImpuestos = new JPanel(new GridLayout(2, 2, 5, 2));
        panelImpuestos.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        panelImpuestos.setBackground(Color.WHITE);
        lblSubtotal = new JLabel("$0.00");
        lblIVA = new JLabel("$0.00");
        panelImpuestos.add(new JLabel("Subtotal:"));
        panelImpuestos.add(lblSubtotal);
        panelImpuestos.add(new JLabel("IVA (16%):"));
        panelImpuestos.add(lblIVA);

        btnFinalizar = new JButton("COBRAR: $0.00 MXN");
        btnFinalizar.setBackground(new Color(40, 167, 69));
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnFinalizar.setPreferredSize(new Dimension(0, 80));
        btnFinalizar.addActionListener(e -> finalizarVenta());

        panelInfDer.add(panelImpuestos, BorderLayout.CENTER);
        panelInfDer.add(btnFinalizar, BorderLayout.SOUTH);
        panelDerecho.add(panelInfDer, BorderLayout.SOUTH);

        contenedorPrincipal.add(scrollCatalogo, BorderLayout.CENTER);
        contenedorPrincipal.add(panelDerecho, BorderLayout.EAST);
        add(contenedorPrincipal, BorderLayout.CENTER);
    }

    private void cargarProductosCompletos() {
        // 1. Limpiamos la lista lógica
        listaProductos.clear();

        // 2. Limpiamos el panel REAL que declaraste arriba
        // En tu código se llama: panelCardsContenedor
        if (panelCardsContenedor != null) {
            panelCardsContenedor.removeAll();
        }

        try {
            List<Producto> productosBD = productoRepository.findAll();

            for (Producto p : productosBD) {
                // Creamos el objeto de datos
                ProductoItem item = new ProductoItem(
                        p.getSku(),
                        p.getName() != null ? p.getName() : "Sin nombre",
                        p.getPrice() != null ? p.getPrice().doubleValue() : 0.0,
                        p.getImagePath()
                );

                // Lo guardamos en la lista para el buscador
                listaProductos.add(item);
            }

            // 3. Usamos tu función renderizarCards para que dibuje todo ordenado
            renderizarCards(listaProductos);

        } catch (Exception e) {
            System.err.println("Error al cargar productos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderizarCards(List<ProductoItem> productos) {
        panelCardsContenedor.removeAll();
        for (int i = 0; i < productos.size(); i++) {
            panelCardsContenedor.add(new CardProducto(productos.get(i), i));
        }
        panelCardsContenedor.revalidate();
        panelCardsContenedor.repaint();
    }

    private void filtrarCatalogo(String t) {
        List<ProductoItem> filtrados = new ArrayList<>();
        for (ProductoItem p : listaProductos) {
            if (p.nombre.toLowerCase().contains(t.toLowerCase())) {
                filtrados.add(p);
            }
        }
        Collections.sort(filtrados, Comparator.comparing(p -> p.nombre));
        renderizarCards(filtrados);
    }

    public void agregarAlCarrito(ProductoItem p) {
        for (Component c : panelCarritoContenedor.getComponents()) {
            if (c instanceof ItemCarritoVisual icv) {
                if (icv.getProducto().id.equals(p.id)) {
                    icv.setCantidad(icv.getCantidad() + 1);
                    icv.requestFocusInWindow();
                    return;
                }
            }
        }
        ItemCarritoVisual nuevo = new ItemCarritoVisual(p);
        panelCarritoContenedor.add(nuevo);
        panelCarritoContenedor.revalidate();
        panelCarritoContenedor.repaint();
        nuevo.requestFocusInWindow();
        calcularTotales();
    }

    private void calcularTotales() {
        double total = 0;
        for (Component c : panelCarritoContenedor.getComponents()) {
            if (c instanceof ItemCarritoVisual icv) {
                total += icv.getProducto().precio * icv.getCantidad();
            }
        }
        lblSubtotal.setText(String.format("$%.2f", total / 1.16));
        lblIVA.setText(String.format("$%.2f", total - (total / 1.16)));
        btnFinalizar.setText(String.format("PAGAR: $%.2f MXN", total));
    }

    private void finalizarVenta() {
        // 1. Validar si hay productos
        if (panelCarritoContenedor.getComponentCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Calcular el total y preparar la lista de productos
        double totalVentaActual = 0; // La declaramos aquí para que el diálogo la vea
        List<ProductoItem> productosEnCarrito = new ArrayList<>();

        for (Component c : panelCarritoContenedor.getComponents()) {
            if (c instanceof ItemCarritoVisual icv) {
                totalVentaActual += icv.getProducto().precio * icv.getCantidad();
                productosEnCarrito.add(icv.getProducto());
            }
        }

        // 3. Lanzar el Diálogo de Pago (Aquí está la corrección de las variables)
        Window parentWindow = SwingUtilities.getWindowAncestor(this);

        // Pasamos parent, el total y la lista (con el cast <?> para que tools lo acepte)
        DialogoPago dialogo = new DialogoPago(parentWindow, totalVentaActual, (List<?>) productosEnCarrito);
        dialogo.setVisible(true);

        // 4. Lógica después de cerrar el diálogo
        if (dialogo.isVentaConfirmada()) {
            panelCarritoContenedor.removeAll();
            panelCarritoContenedor.revalidate();
            panelCarritoContenedor.repaint();
            calcularTotales();
            txtBuscador.setText("");
            txtBuscador.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, "Venta realizada con éxito.", "Sistema", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void buscarYAgregarDirecto() {
        String query = txtBuscadorDirecto.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            return;
        }

        ProductoItem encontrado = null;

        // Buscamos en la lista maestra por ID (SKU) o por Nombre exacto/parcial
        for (ProductoItem p : listaProductos) {
            if (p.id.toLowerCase().equals(query) || p.nombre.toLowerCase().equals(query)) {
                encontrado = p;
                break;
            }
        }

        if (encontrado != null) {
            agregarAlCarrito(encontrado);
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

        CardProducto(ProductoItem p, int idx) {
            this.index = idx;
            setLayout(new BorderLayout(5, 5));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            setPreferredSize(new Dimension(180, 220)); // Aumentamos un poco el alto para el SKU
            setFocusable(true);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel img = new JLabel(cargarImagen(p.img, 85, 85, p.nombre));
            img.setHorizontalAlignment(JLabel.CENTER);

            // --- PANEL DE INFORMACIÓN (3 FILAS: SKU, NOMBRE, PRECIO) ---
            // --- PANEL DE INFORMACIÓN (3 FILAS: SKU, NOMBRE, PRECIO) ---
            JPanel pInfo = new JPanel(new GridLayout(3, 1, 2, 2));
            pInfo.setOpaque(false);
            pInfo.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

// Etiqueta para el SKU
            JLabel lblSku = new JLabel("SKU: " + p.id, SwingConstants.CENTER);
            lblSku.setFont(new Font("Arial", Font.ITALIC, 11)); // <--- Cambiado a Arial 11
            lblSku.setForeground(Color.GRAY);

// Etiqueta para el Nombre
            JLabel lblN = new JLabel(p.nombre, SwingConstants.CENTER);
            lblN.setFont(new Font("Arial", Font.BOLD, 14)); // <--- Cambiado a Arial 14 (Más grande)

// Etiqueta para el Precio
            JLabel lblP = new JLabel("$" + String.format("%.2f", p.precio), SwingConstants.CENTER);
            lblP.setFont(new Font("Arial", Font.BOLD, 16)); // <--- Cambiado a Arial 16 (Resaltado)
            lblP.setForeground(new Color(0, 102, 204));

            pInfo.add(lblSku);
            pInfo.add(lblN);
            pInfo.add(lblP);

            add(img, BorderLayout.CENTER);
            add(pInfo, BorderLayout.SOUTH);

            // --- Los listeners (Mouse, Focus, Key) se mantienen igual ---
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    agregarAlCarrito(p);
                }
            });

            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    setBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2));
                }

                public void focusLost(FocusEvent e) {
                    setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
                }
            });

            addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    Component next = null;
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_RIGHT -> {
                            if ((index + 1) % COLUMNAS == 0 && panelCarritoContenedor.getComponentCount() > 0) {
                                panelCarritoContenedor.getComponent(0).requestFocusInWindow();
                            } else {
                                next = getComponentAtIdx(index + 1);
                            }
                        }
                        case KeyEvent.VK_LEFT ->
                            next = getComponentAtIdx(index - 1);
                        case KeyEvent.VK_DOWN ->
                            next = getComponentAtIdx(index + COLUMNAS);
                        case KeyEvent.VK_UP -> {
                            if (index < COLUMNAS) {
                                txtBuscador.requestFocusInWindow();
                            } else {
                                next = getComponentAtIdx(index - COLUMNAS);
                            }
                        }
                        case KeyEvent.VK_ENTER ->
                            agregarAlCarrito(p);
                    }
                    if (next != null) {
                        next.requestFocusInWindow();
                    }
                }
            });
        }

        private Component getComponentAtIdx(int i) {
            if (i >= 0 && i < panelCardsContenedor.getComponentCount()) {
                return panelCardsContenedor.getComponent(i);
            }
            return null;
        }
    }

    /* ========== ITEM CARRITO DINÁMICO ========== */
    class ItemCarritoVisual extends JPanel {

        private ProductoItem producto;
        private JTextField txtCant;
        private JLabel lblImporte;

        public ItemCarritoVisual(ProductoItem p) {
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

            lblImporte = new JLabel(String.format("$%.2f", p.precio));
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
                if (getCantidad() > 1) {
                    setCantidad(getCantidad() - 1);
                }
            });
            btnMas.addActionListener(e -> setCantidad(getCantidad() + 1));

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
            pMid.add(new JLabel(p.nombre) {
                {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
            });
            pMid.add(new JLabel("Unit: $" + p.precio));

            JPanel pDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 12));
            pDer.setOpaque(false);
            pDer.add(btnMenos);
            pDer.add(txtCant);
            pDer.add(btnMas);
            pDer.add(lblImporte);

            JPanel pIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            pIzquierda.setOpaque(false);
            pIzquierda.add(btnEliminar);
            pIzquierda.add(new JLabel(cargarImagen(p.img, 45, 45, p.nombre)));

            add(pIzquierda, BorderLayout.WEST);
            add(pMid, BorderLayout.CENTER);
            add(pDer, BorderLayout.EAST);
        }

        public void setCantidad(int n) {
            txtCant.setText(String.valueOf(n));
        }

        public int getCantidad() {
            try {
                String t = txtCant.getText().trim();
                return t.isEmpty() ? 0 : Integer.parseInt(t);
            } catch (Exception e) {
                return 0;
            }
        }

        public ProductoItem getProducto() {
            return producto;
        }

        private void actualizarImporte() {
            lblImporte.setText(String.format("$%.2f", producto.precio * getCantidad()));
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

    class ProductoItem {

        String id, nombre, img;
        double precio;

        ProductoItem(String id, String n, double p, String i) {
            this.id = id;
            this.nombre = n;
            this.precio = p;
            this.img = i;
        }
    }
}
