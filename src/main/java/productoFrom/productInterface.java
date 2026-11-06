package productoFrom;

// 1. Clases de tu proyecto
import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import com.mycompany.chancuellarpuntodeventa.services.repository.ProductoRepository;

// 2. Apache POI (Excel)
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.JFileChooser;

// 3. Swing y AWT
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;

// 4. Utilidades de Java e IO
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class productInterface extends JPanel {

    @org.springframework.beans.factory.annotation.Autowired
    private ProductoRepository productoRepository;

    private JList<ProductoDTO> listaProductos;
    private DefaultListModel<ProductoDTO> listModel;
    private List<ProductoDTO> todosLosProductos;

    private JLabel lblNombre, lblPrecioPrincipal, lblCodigo, lblCategoria, lblImagen;
    private JPanel panelListaPrecios;
    private JTextArea txtDescripcion;
    private JTextField txtBuscar;

    private final Color COLOR_PRINCIPAL = new Color(33, 37, 41);
    private final Color COLOR_ACCENTO = new Color(40, 167, 69);
    private final Color COLOR_TEXTO_HEADER = Color.WHITE;

    public productInterface() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        todosLosProductos = new ArrayList<>();
        listModel = new DefaultListModel<>();
        initUI();
    }

    @jakarta.annotation.PostConstruct
    private void init() {
        cargarProductosCompletos();
    }

    private void cargarProductosCompletos() {
        if (productoRepository == null) {
            return;
        }
        todosLosProductos.clear();
        try {
            List<ProductoDTO> productosBD = productoRepository.findAll();
            for (ProductoDTO p : productosBD) {
                // 1. Validamos el precio directamente como BigDecimal
                BigDecimal precioSeguro = (p.getPrice() != null) ? p.getPrice() : BigDecimal.ZERO;

                // 2. Creamos el objeto pasando el BigDecimal y el boolean de granel
                ProductoDTO item = new ProductoDTO();
                item.setId(p.getId()); // Si tiene ID
                item.setSku(p.getSku());
                item.setName(p.getName() != null ? p.getName() : "Sin nombre");
                item.setPrice(precioSeguro);
                item.setImagePath(p.getImagePath());
                item.setGranel(p.isGranel());

                todosLosProductos.add(item);
            }
            todosLosProductos.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
            actualizarLista(todosLosProductos);
        } catch (Exception e) {
            System.err.println("Error al cargar productos: " + e.getMessage());
        }
    }

  private void importarDesdeExcel() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Seleccionar catálogo de productos (Excel)");
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos Excel (*.xlsx, *.xls)", "xlsx", "xls"));

    final JPanel panelActual = this;
    int seleccion = fileChooser.showOpenDialog(panelActual);

    if (seleccion == JFileChooser.APPROVE_OPTION) {
        File archivo = fileChooser.getSelectedFile();

        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(panelActual);
        
        // Diálogo flotante sin bordes con transparencia nativa
        JDialog dialogoProgreso = new JDialog(parentFrame, "", true);
        dialogoProgreso.setUndecorated(true);
        dialogoProgreso.setBackground(new Color(0, 0, 0, 0)); // Fondo 100% transparente
        dialogoProgreso.setSize(420, 310);
        dialogoProgreso.setLocationRelativeTo(panelActual);
        dialogoProgreso.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialogoProgreso.setLayout(new BorderLayout());

        // Componente visual personalizado del loader circular neón
        TransparentLoaderPanel loaderPanel = new TransparentLoaderPanel();
        dialogoProgreso.add(loaderPanel, BorderLayout.CENTER);

        File carpetaImagenes = new File(System.getProperty("user.home"), "Pictures/imagenes");

        SwingWorker<int[], Integer> worker = new SwingWorker<int[], Integer>() {
            @Override
            protected int[] doInBackground() throws Exception {
                int nuevos = 0;
                int actualizados = 0;

                try (FileInputStream fis = new FileInputStream(archivo); Workbook workbook = new XSSFWorkbook(fis)) {

                    Sheet sheet = workbook.getSheetAt(0);
                    int totalFilas = sheet.getLastRowNum();

                    if (totalFilas <= 0) {
                        return new int[]{0, 0};
                    }

                    DataFormatter formatter = new DataFormatter();

                    for (int i = 1; i <= totalFilas; i++) {
                        Row fila = sheet.getRow(i);
                        if (fila != null) {
                            Cell celdaSku = fila.getCell(0);
                            String skuExcel = "";

                            if (celdaSku != null) {
                                skuExcel = formatter.formatCellValue(celdaSku).trim();

                                if (skuExcel.endsWith(".0")) {
                                    skuExcel = skuExcel.substring(0, skuExcel.length() - 2);
                                }
                                skuExcel = skuExcel.replace("+1", "").trim();
                            }

                            String nombreExcel = formatter.formatCellValue(fila.getCell(1)).trim();
                            String precioExcel = formatter.formatCellValue(fila.getCell(2)).trim();
                            String granelExcel = formatter.formatCellValue(fila.getCell(3)).trim();

                            if (skuExcel != null && !skuExcel.isEmpty()) {
                                ProductoDTO p = productoRepository.findBySku(skuExcel);

                                if (p != null) {
                                    actualizados++;
                                } else {
                                    p = new ProductoDTO();
                                    p.setSku(skuExcel);
                                    nuevos++;
                                }

                                p.setSku(skuExcel);
                                p.setName(nombreExcel);

                                try {
                                    String limpio = precioExcel.replace(",", "").trim();
                                    p.setPrice(limpio.isEmpty() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(limpio));
                                } catch (Exception e) {
                                    p.setPrice(java.math.BigDecimal.ZERO);
                                }

                                boolean esGranel = (granelExcel != null && granelExcel.trim().equalsIgnoreCase("s"));
                                p.setGranel(esGranel);

                                String rutaRelativa = buscarImagenMasivaPorSku(carpetaImagenes, skuExcel);

                                if (rutaRelativa != null) {
                                    p.setImagePath(rutaRelativa);
                                    System.out.println("[IMAGEN VINCULADA] SKU: " + skuExcel + " -> " + rutaRelativa);
                                } else {
                                    p.setImagePath(null);
                                }

                                productoRepository.save(p);
                            }
                        }

                        int porcentaje = (int) ((double) i / totalFilas * 100);
                        publish(porcentaje);
                    }
                }

                return new int[]{nuevos, actualizados};
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int ultimoPorcentaje = chunks.get(chunks.size() - 1);
                loaderPanel.setPorcentaje(ultimoPorcentaje);
                loaderPanel.setMensajeEstado("Sincronizando productos... " + ultimoPorcentaje + "%");
            }

            @Override
            protected void done() {
                loaderPanel.setPorcentaje(100);
                loaderPanel.setMensajeEstado("¡Sincronización completada!");

                // Pequeña pausa visual de 500ms para mostrar el 100% completado
                Timer pauseTimer = new Timer(500, evt -> {
                    dialogoProgreso.dispose();

                    try {
                        int[] resultados = get();
                        JOptionPane.showMessageDialog(
                                panelActual,
                                "¡Catálogo e imágenes sincronizados masivamente con éxito!\nNuevos: " + resultados[0] + "\nActualizados: " + resultados[1],
                                "Importación Exitosa",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        cargarProductosCompletos();

                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                                panelActual,
                                "Error al finalizar importación: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        e.printStackTrace();
                    }
                });
                pauseTimer.setRepeats(false);
                pauseTimer.start();
            }
        };

        worker.execute();
        dialogoProgreso.setVisible(true);
    }
}

// =========================================================================
// PANEL GRÁFICO DEL LOADER CIRCULAR TRANSPARENTE (GLASSMORPHISM NEÓN)
// =========================================================================
private static class TransparentLoaderPanel extends JPanel {
    private int porcentaje = 0;
    private int anguloRotacion = 0;
    private String mensajeEstado = "Importando catálogo...";
    private Timer timerSpin;

    public TransparentLoaderPanel() {
        setOpaque(false);
        timerSpin = new Timer(15, e -> {
            anguloRotacion = (anguloRotacion + 6) % 360;
            repaint();
        });
        timerSpin.start();
    }

    public void setPorcentaje(int p) {
        this.porcentaje = Math.min(100, p);
        repaint();
        if (this.porcentaje >= 100 && timerSpin.isRunning()) {
            timerSpin.stop();
        }
    }

    public void setMensajeEstado(String msg) {
        this.mensajeEstado = msg;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Fondo flotante translúcido (Glassmorphism oscuro elegante)
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillRoundRect(20, 20, w - 40, h - 40, 24, 24);

        g2.setColor(new Color(255, 255, 255, 45));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(20, 20, w - 40, h - 40, 24, 24);

        // 2. Spinner Circular Central
        int size = 110;
        int x = (w - size) / 2;
        int y = (h - size) / 2 - 15;

        // Pista de fondo translúcida
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawOval(x, y, size, size);

        // Arco activo neón brillante
        g2.setColor(new Color(56, 189, 248));
        int arcAngle = - (int) ((porcentaje / 100.0) * 360);
//        g2.draw(new Arc2D.Double(x, y, size, size, anguloRotacion, arcAngle, Arc2D.OPEN));

        // 3. Texto del Porcentaje en el centro
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        String textoPorcentaje = porcentaje + "%";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(textoPorcentaje)) / 2;
        int ty = y + (size / 2) + 8;
        g2.drawString(textoPorcentaje, tx, ty);

        // 4. Texto dinámico del estado
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(new Color(148, 163, 184));
        FontMetrics fm2 = g2.getFontMetrics();
        int tsx = (w - fm2.stringWidth(mensajeEstado)) / 2;
        g2.drawString(mensajeEstado, tsx, y + size + 28);

        g2.dispose();
    }
}
// Método auxiliar portable: Busca en la carpeta de imágenes oficial del usuario en el sistema

    private String buscarImagenMasivaPorSku(File carpetaDefault, String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return null;
        }

        String skuLimpio = sku.trim();
        String[] extensiones = {".png", ".jpg", ".jpeg", ".webp", ".jfif", ".PNG", ".JPG", ".JPEG"};

        // Obtenemos la ruta base del usuario (ej. C:\Users\NombreUsuario)
        String userHome = System.getProperty("user.home");

        // Directorios estándar de imágenes del sistema operativo a probar
        List<File> carpetasABuscar = new ArrayList<>();
        carpetasABuscar.add(new File(userHome, "Pictures" + File.separator + "imagenes"));
        carpetasABuscar.add(new File(userHome, "Imágenes" + File.separator + "imagenes"));
        carpetasABuscar.add(new File(userHome, "Pictures"));
        carpetasABuscar.add(new File(userHome, "Imágenes"));

        for (File carpeta : carpetasABuscar) {
            if (carpeta.exists() && carpeta.isDirectory()) {
                for (String ext : extensiones) {
                    File archivoImg = new File(carpeta, skuLimpio + ext);
                    if (archivoImg.exists()) {
                        // Retorna la ruta absoluta o el archivo encontrado para cargarlo correctamente
                        return archivoImg.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    private void actualizarLista(List<ProductoDTO> productos) {
        listModel.clear();
        for (ProductoDTO p : productos) {
            listModel.addElement(p);
        }
    }

    private void filtrarProductos() {
        String query = txtBuscar.getText().toLowerCase();
        List<ProductoDTO> filtrados = todosLosProductos.stream()
                .filter(p -> p.getName().toLowerCase().contains(query) || p.getSku().contains(query))
                .collect(Collectors.toList());
        actualizarLista(filtrados);
    }

    private void initUI() {
        // --- BARRA SUPERIOR ---
        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setBackground(COLOR_PRINCIPAL);
        barraSuperior.setPreferredSize(new Dimension(0, 50));

        JLabel lblModulo = new JLabel(" Producto");
        lblModulo.setForeground(COLOR_TEXTO_HEADER);
        lblModulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel panelAccionesHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelAccionesHeader.setOpaque(false);

        panelAccionesHeader.add(crearBotonIcono("📊", "Importar Excel", e -> importarDesdeExcel()));
        panelAccionesHeader.add(crearBotonIcono("🔄", "Actualizar", e -> cargarProductosCompletos()));

        barraSuperior.add(lblModulo, BorderLayout.WEST);
        barraSuperior.add(panelAccionesHeader, BorderLayout.EAST);
        add(barraSuperior, BorderLayout.NORTH);

        // --- PANEL IZQUIERDO ---
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setPreferredSize(new Dimension(350, 0));
        panelIzquierdo.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        panelBusqueda.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtBuscar = new JTextField();
        JButton btnAgregar = new JButton("+");
        btnAgregar.setBackground(COLOR_ACCENTO);
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(new Font("Arial", Font.BOLD, 20));
        btnAgregar.setPreferredSize(new Dimension(45, 40));

        btnAgregar.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            NuevoProductoDialog dialogo = new NuevoProductoDialog(parentFrame);
            dialogo.setVisible(true);
            cargarProductosCompletos();
        });

        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);
        panelBusqueda.add(btnAgregar, BorderLayout.EAST);

        listaProductos = new JList<>(listModel);
        listaProductos.setCellRenderer(new ProductListRenderer());
        listaProductos.setFixedCellHeight(80);
        JScrollPane scrollLista = new JScrollPane(listaProductos);
        scrollLista.setBorder(null);

        panelIzquierdo.add(panelBusqueda, BorderLayout.NORTH);
        panelIzquierdo.add(scrollLista, BorderLayout.CENTER);

        // --- PANEL DERECHO (DETALLES MEJORADO) ---
        JPanel panelDetalleContenedor = new JPanel(new BorderLayout());
        panelDetalleContenedor.setBackground(new Color(248, 249, 250));

        JPanel subBarra = new JPanel(new BorderLayout());
        subBarra.setBackground(new Color(52, 58, 64));
        subBarra.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));

        JLabel lblDetalleTitulo = new JLabel("Detalles del producto");
        lblDetalleTitulo.setForeground(Color.WHITE);
        lblDetalleTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel panelIconosEdit = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelIconosEdit.setOpaque(false);
        panelIconosEdit.add(crearBotonIcono("📝", "Editar", e -> {
            ProductoDTO seleccionado = listaProductos.getSelectedValue();
            if (seleccionado != null) {
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                parentFrame.setVisible(false);
                EditarProductoDialog dialogo = new EditarProductoDialog(parentFrame, seleccionado, productoRepository);
                dialogo.setVisible(true);
                cargarProductosCompletos();
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto");
            }
        }));
        //      panelIconosEdit.add(crearBotonIcono("🗑", "Eliminar", null));

        subBarra.add(lblDetalleTitulo, BorderLayout.WEST);
        subBarra.add(panelIconosEdit, BorderLayout.EAST);

        JPanel panelBlancoCentral = new JPanel();
        panelBlancoCentral.setLayout(new BoxLayout(panelBlancoCentral, BoxLayout.Y_AXIS));
        panelBlancoCentral.setBackground(Color.WHITE);
        panelBlancoCentral.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel infoSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 0));
        infoSuperior.setOpaque(false);

        lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(140, 140));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setVerticalAlignment(SwingConstants.CENTER);
        lblImagen.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 240), 1));

        // Imagen por defecto estilizada sin emojis para prevenir el cuadro blanco[cite: 10]
        lblImagen.setIcon(crearPlaceholderIcono(140, 140));

        JPanel datosTxt = new JPanel();
        datosTxt.setLayout(new BoxLayout(datosTxt, BoxLayout.Y_AXIS));
        datosTxt.setOpaque(false);

        lblCodigo = new JLabel("ID PRODUCTO");
        lblCodigo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCodigo.setForeground(COLOR_ACCENTO);

        lblNombre = new JLabel("Seleccione un producto");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblNombre.setForeground(new Color(33, 37, 41));

        lblPrecioPrincipal = new JLabel("$ 0.00");
        lblPrecioPrincipal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblPrecioPrincipal.setForeground(new Color(33, 37, 41));

        lblCategoria = new JLabel("CATEGORIA");
        lblCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCategoria.setForeground(new Color(108, 117, 125));

        datosTxt.add(lblCodigo);
        datosTxt.add(Box.createVerticalStrut(4));
        datosTxt.add(lblNombre);
        datosTxt.add(Box.createVerticalStrut(4));
        datosTxt.add(lblPrecioPrincipal);
        datosTxt.add(Box.createVerticalStrut(2));
        datosTxt.add(lblCategoria);
        datosTxt.add(Box.createVerticalStrut(10));

        txtDescripcion = new JTextArea(2, 20);
        txtDescripcion.setEditable(false);
        txtDescripcion.setOpaque(false);
        txtDescripcion.setForeground(new Color(100, 100, 100));
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        datosTxt.add(txtDescripcion);

        infoSuperior.add(lblImagen);
        infoSuperior.add(datosTxt);

        panelBlancoCentral.add(infoSuperior);
        panelBlancoCentral.add(Box.createVerticalStrut(25));

        JPanel panelTabs = new JPanel(new GridLayout(1, 3));
        panelTabs.setMaximumSize(new Dimension(2000, 40));
        panelTabs.add(crearTab("Detalles del producto", true));
        panelTabs.add(crearTab("Historial", false));
        panelTabs.add(crearTab("-- PZA", false));
        panelBlancoCentral.add(panelTabs);

        panelListaPrecios = new JPanel();
        panelListaPrecios.setLayout(new BoxLayout(panelListaPrecios, BoxLayout.Y_AXIS));
        panelListaPrecios.setOpaque(false);
        panelBlancoCentral.add(panelListaPrecios);

        JScrollPane scrollDetalle = new JScrollPane(panelBlancoCentral);
        scrollDetalle.setBorder(null);

        panelDetalleContenedor.add(subBarra, BorderLayout.NORTH);
        panelDetalleContenedor.add(scrollDetalle, BorderLayout.CENTER);

        add(panelIzquierdo, BorderLayout.WEST);
        add(panelDetalleContenedor, BorderLayout.CENTER);

        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarProductos();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarProductos();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarProductos();
            }
        });

        listaProductos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle(listaProductos.getSelectedValue());
            }
        });
    }

    private JButton crearBotonIcono(String icono, String toolTip, java.awt.event.ActionListener accion) {
        JButton btn = new JButton(icono);
        btn.setToolTipText(toolTip);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        if (accion != null) {
            btn.addActionListener(accion);
        }
        return btn;
    }

    private JButton crearTab(String titulo, boolean activo) {
        JButton b = new JButton(titulo);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createMatteBorder(0, 0, activo ? 3 : 1, 0, activo ? COLOR_PRINCIPAL : Color.LIGHT_GRAY));
        return b;
    }

    private void mostrarDetalle(ProductoDTO p) {
        if (p == null) {
            return;
        }

        lblNombre.setText(p.getName());
        lblCodigo.setText(p.getSku());
        lblPrecioPrincipal.setText(String.format("$%.2f", p.getPrice()));

        // --- BUSQUEDA BLINDADA DE IMAGEN (POR PATH O DIRECTA POR SKU EN imgTienda) ---
        boolean imagenCargada = false;
        File archivoImagen = null;

        // Intentar 1: Usar la ruta guardada en el DTO
        if (p.getImagePath() != null && !p.getImagePath().trim().isEmpty()) {
            File f = new File(p.getImagePath());
            if (f.exists()) {
                archivoImagen = f;
            }
        }

        // Intentar 2: Si la ruta guardada falló, buscar directamente en imgTienda usando el SKU (ej: 1.png, 1.jpg)
        if (archivoImagen == null && p.getSku() != null && !p.getSku().trim().isEmpty()) {
            String skuLimpio = p.getSku().trim();
            String[] extensiones = {".png", ".jpg", ".jpeg", ".webp", ".jfif"};
            File carpetaTienda = new File("imgTienda");

            if (carpetaTienda.exists() && carpetaTienda.isDirectory()) {
                for (String ext : extensiones) {
                    File f = new File(carpetaTienda, skuLimpio + ext);
                    if (f.exists()) {
                        archivoImagen = f;
                        break;
                    }
                }
            }
        }

        // Renderizar la imagen si la encontró por cualquiera de las dos vías
        if (archivoImagen != null && archivoImagen.exists()) {
            try {
                ImageIcon icon = new ImageIcon(archivoImagen.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(img));
                lblImagen.setText("");
                imagenCargada = true;
            } catch (Exception e) {
                System.err.println("Error al escalar imagen: " + e.getMessage());
            }
        }

        // Si de plano no existe archivo en imgTienda ni en ruta, muestra el placeholder
        if (!imagenCargada) {
            lblImagen.setIcon(crearPlaceholderIcono(140, 140));
            lblImagen.setText("");
        }

        panelListaPrecios.removeAll();
        BigDecimal divisorIVA = new BigDecimal("1.16");
        BigDecimal precioSinIVA = p.getPrice().divide(divisorIVA, 2, RoundingMode.HALF_UP);
        panelListaPrecios.add(crearFilaPrecio("PRECIO 1", BigDecimal.ZERO, precioSinIVA, p.getPrice()));
        panelListaPrecios.revalidate();
        panelListaPrecios.repaint();
    }

    private JPanel crearFilaPrecio(String titulo, BigDecimal desc, BigDecimal sinImp, BigDecimal conImp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(252, 252, 252));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));
        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblT.setForeground(Color.GRAY);
        JPanel grid = new JPanel(new GridLayout(1, 3));
        grid.setOpaque(false);
        grid.add(crearDatoDetalle("Descuento %", desc + " %"));
        grid.add(crearDatoDetalle("Sin impuesto", String.format("$%.2f", sinImp)));
        grid.add(crearDatoDetalle("Con impuesto", String.format("$%.2f", conImp)));
        p.add(lblT, BorderLayout.NORTH);
        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearDatoDetalle(String label, String valor) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setOpaque(false);
        JLabel l1 = new JLabel(label);
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l1.setForeground(Color.GRAY);
        JLabel l2 = new JLabel(valor);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(l1);
        p.add(l2);
        return p;
    }

    // --- GENERADOR DE PLACEHOLDER VECTORIAL (Evita cuadros blancos rotos[cite: 10]) ---
    private ImageIcon crearPlaceholderIcono(int ancho, int alto) {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo suave
        g2.setColor(new Color(245, 247, 250));
        g2.fillRect(0, 0, ancho, alto);

        // Borde interior fino
        g2.setColor(new Color(225, 230, 235));
        g2.drawRect(0, 0, ancho - 1, alto - 1);

        // Dibujo de icono de caja estilizado en el centro
        int cx = ancho / 2;
        int cy = alto / 2 - 5;

        g2.setColor(new Color(180, 190, 200));
        g2.setStroke(new BasicStroke(2.5f));

        // Caja 3D simplificada
        int w = 36;
        int h = 30;
        g2.drawRect(cx - w / 2, cy - h / 2 + 5, w, h);
        g2.drawLine(cx - w / 2, cy - h / 2 + 5, cx, cy - h / 2 - 8);
        g2.drawLine(cx + w / 2, cy - h / 2 + 5, cx, cy - h / 2 - 8);

        // Texto informativo "Sin Imagen"
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(new Color(150, 160, 170));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "SIN IMAGEN";
        int txtW = fm.stringWidth(txt);
        g2.drawString(txt, cx - txtW / 2, cy + 32);

        g2.dispose();
        return new ImageIcon(img);
    }
}
