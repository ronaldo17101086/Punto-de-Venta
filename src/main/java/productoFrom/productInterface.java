package productoFrom;

// 1. Clases de tu proyecto
import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import com.mycompany.chancuellarpuntodeventa.services.repository.ProductoRepository;

// 2. Apache POI (Excel) - Asegúrate de borrar SheetCollate e importar estas
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

// 4. Utilidades de Java e IO
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
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
                // Ahora pasamos 6 parámetros para que coincida con el nuevo constructor
                todosLosProductos.add(new ProductoDTO(
                        p.getName() != null ? p.getName() : "Sin nombre",
                        p.getSku(),
                        p.getPrice() != null ? p.getPrice().doubleValue() : 0.0,
                        p.getImagePath() // <--- ESTE ES EL QUE FALTABA
                ));
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
        int seleccion = fileChooser.showOpenDialog(this);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(archivo); Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> filas = sheet.iterator();

                if (filas.hasNext()) {
                    filas.next(); // Saltar cabecera (SKU, DESCRIPCION, PRECIO)
                }

                DataFormatter formatter = new DataFormatter();
                int nuevos = 0;
                int actualizados = 0;

                while (filas.hasNext()) {
                    Row fila = filas.next();

                    // SEGÚN TU IMAGEN DE EXCEL:
                    // Columna A (0) = Parece ser un ID o SKU numérico
                    // Columna B (1) = Descripción (Coca Cola...)
                    // Columna C (2) = Precio (15)
                    String skuExcel = formatter.formatCellValue(fila.getCell(0));
                    String nombreExcel = formatter.formatCellValue(fila.getCell(1));
                    String precioExcel = formatter.formatCellValue(fila.getCell(2));

                    if (skuExcel != null && !skuExcel.trim().isEmpty()) {

                        // 1. Intentamos buscar si ya existe para no duplicar
                        ProductoDTO p = productoRepository.findBySku(skuExcel);

                        if (p != null) {
                            actualizados++;
                        } else {
                            // 2. Si es nuevo, lo creamos
                            p = new ProductoDTO();
                            p.setSku(skuExcel);
                            // IMPORTANTE: Si te sigue dando el error del "Identifier", 
                            // asegúrate que en Producto.java el ID tenga @GeneratedValue
                            nuevos++;
                        }

                        p.setName(nombreExcel);

                        // 3. Limpieza de precio para evitar que truene
                        try {
                            String limpio = precioExcel.replace(",", "").trim();
                            p.setPrice(limpio.isEmpty() ? 0.0 : Double.parseDouble(limpio));
                        } catch (Exception e) {
                            p.setPrice(0.0);
                        }

                        // 4. Guardar en la base de datos
                        productoRepository.save(p);
                    }
                }

                JOptionPane.showMessageDialog(this, "¡Por fin quedó!\nNuevos: " + nuevos + "\nActualizados: " + actualizados);
                cargarProductosCompletos();

            } catch (Exception e) {
                // Esto te dirá exactamente qué columna o dato está fallando
                JOptionPane.showMessageDialog(this, "Error en el proceso: " + e.getMessage());
                e.printStackTrace();
            }
        }
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

        JLabel lblModulo = new JLabel("  ☰  Producto");
        lblModulo.setForeground(COLOR_TEXTO_HEADER);
        lblModulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel panelAccionesHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelAccionesHeader.setOpaque(false);

        // NUEVO BOTÓN EXCEL
        panelAccionesHeader.add(crearBotonIcono("📊", "Importar Excel", e -> importarDesdeExcel()));
        panelAccionesHeader.add(crearBotonIcono("🔄", "Actualizar", e -> cargarProductosCompletos()));
        panelAccionesHeader.add(crearBotonIcono("⋮", "Más opciones", null));

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

        // --- PANEL DERECHO (DETALLES) ---
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
        panelIconosEdit.add(crearBotonIcono("📝", "Editar", null));
        panelIconosEdit.add(crearBotonIcono("🗑", "Eliminar", null));
        panelIconosEdit.add(crearBotonIcono("📋", "Copiar", null));

        panelIconosEdit.add(crearBotonIcono("📝", "Editar", e -> {
            ProductoDTO seleccionado = listaProductos.getSelectedValue();
            if (seleccionado != null) {
                // Obtenemos el JFrame principal (Dashboard)
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

                // Ocultamos el Dashboard para dar efecto de navegación
                parentFrame.setVisible(false);

                // Abrimos el diálogo de edición
                EditarProductoDialog dialogo = new EditarProductoDialog(parentFrame, seleccionado, productoRepository);
                dialogo.setVisible(true);

                // Al cerrar el diálogo (por guardar o regresar), refrescamos datos
                cargarProductosCompletos();
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto");
            }
        }));

        subBarra.add(lblDetalleTitulo, BorderLayout.WEST);
        subBarra.add(panelIconosEdit, BorderLayout.EAST);

        JPanel panelBlancoCentral = new JPanel();
        panelBlancoCentral.setLayout(new BoxLayout(panelBlancoCentral, BoxLayout.Y_AXIS));
        panelBlancoCentral.setBackground(Color.WHITE);
        panelBlancoCentral.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel infoSuperior = new JPanel(new BorderLayout(25, 0));
        infoSuperior.setOpaque(false);
        lblImagen = new JLabel("☕", SwingConstants.CENTER);
        lblImagen.setFont(new Font("Segoe UI", Font.PLAIN, 50));
        lblImagen.setPreferredSize(new Dimension(150, 150));
        lblImagen.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));

        JPanel datosTxt = new JPanel();
        datosTxt.setLayout(new BoxLayout(datosTxt, BoxLayout.Y_AXIS));
        datosTxt.setOpaque(false);

        lblCodigo = new JLabel("ID PRODUCTO");
        lblCodigo.setForeground(COLOR_ACCENTO);
        lblNombre = new JLabel("Seleccione un producto");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPrecioPrincipal = new JLabel("$ 0.00");
        lblPrecioPrincipal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblCategoria = new JLabel("CATEGORIA");
        lblCategoria.setForeground(Color.GRAY);

        datosTxt.add(lblCodigo);
        datosTxt.add(lblNombre);
        datosTxt.add(lblPrecioPrincipal);
        datosTxt.add(lblCategoria);
        datosTxt.add(Box.createVerticalStrut(10));

        txtDescripcion = new JTextArea(2, 20);
        txtDescripcion.setEditable(false);
        txtDescripcion.setOpaque(false);
        txtDescripcion.setForeground(new Color(100, 100, 100));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        datosTxt.add(txtDescripcion);

        infoSuperior.add(lblImagen, BorderLayout.WEST);
        infoSuperior.add(datosTxt, BorderLayout.CENTER);
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

        // --- LÓGICA DE IMAGEN PARA EL DETALLE ---
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            File file = new File(p.getImagePath());
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(p.getImagePath());
                // Redimensionar la imagen para que encaje en el label (150x150)
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(img));
                lblImagen.setText(""); // Quitar el emoji si hay imagen
            } else {
                lblImagen.setIcon(null);
                lblImagen.setText("☕"); // Default si no encuentra el archivo
            }
        } else {
            lblImagen.setIcon(null);
            lblImagen.setText("☕");
        }

        panelListaPrecios.removeAll();
        panelListaPrecios.add(crearFilaPrecio("PRECIO 1", 0, p.getPrice() / 1.16, p.getPrice()));
        panelListaPrecios.revalidate();
        panelListaPrecios.repaint();
    }

    private JPanel crearFilaPrecio(String titulo, double desc, double sinImp, double conImp) {
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
}
