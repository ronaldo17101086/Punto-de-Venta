package producto;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NuevoProductoDialog extends JDialog {

    private JPanel panelPreciosContenedor;
    private JButton btnAgregarPrecio;
    private int contadorPrecios = 0;
    private final int MAX_PRECIOS = 10;

    public NuevoProductoDialog(Frame parent) {
        super(parent, "Nuevo producto", true);
        setSize(800, 850); // Un poco más alto para los precios
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(242, 244, 246));

        // --- HEADER AZUL ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(25, 88, 156));
        JLabel lblTitulo = new JLabel("< Nuevo producto");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(lblTitulo);
        add(header, BorderLayout.NORTH);

        // --- CUERPO SCROLLABLE ---
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(Color.WHITE);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // 1. Imágenes
        JPanel panelImagenes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelImagenes.setOpaque(false);
        panelImagenes.add(crearCuadroImagen(true));
        for(int i=0; i<4; i++) panelImagenes.add(crearCuadroImagen(false));
        panelContenido.add(panelImagenes);
        panelContenido.add(Box.createVerticalStrut(20));

        // 2. Datos Generales
        panelContenido.add(crearCampoEstilizado("Clave *", "7501259289444", true));
        panelContenido.add(crearCampoEstilizado("Nombre de producto *", "", false));
        panelContenido.add(crearCampoEstilizado("Características", "", false));
        
        JPanel panelDoble = new JPanel(new GridLayout(1, 2, 20, 0));
        panelDoble.setOpaque(false);
        panelDoble.add(crearSelectorEstilizado("Departamento *", "UNDEFINED"));
        panelDoble.add(crearSelectorEstilizado("Categoría *", "UNDEFINED"));
        panelContenido.add(panelDoble);

        panelContenido.add(crearSelectorEstilizado("Clave SAT", ""));
        panelContenido.add(crearSelectorEstilizado("Unidad de venta *", "PIEZA"));
        panelContenido.add(crearFilaSwitch("Controlar inventario de este producto", true));
        panelContenido.add(crearSelectorEstilizado("Impuestos", ""));
        panelContenido.add(crearFilaSwitch("Costo de producto", false));

        // --- SECCIÓN DE PRECIOS ---
        JLabel lblPreciosTit = new JLabel("PRECIOS");
        lblPreciosTit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPreciosTit.setForeground(Color.GRAY);
        lblPreciosTit.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelContenido.add(Box.createVerticalStrut(20));
        panelContenido.add(lblPreciosTit);
        panelContenido.add(Box.createVerticalStrut(10));

        panelPreciosContenedor = new JPanel();
        panelPreciosContenedor.setLayout(new BoxLayout(panelPreciosContenedor, BoxLayout.Y_AXIS));
        panelPreciosContenedor.setOpaque(false);
        panelContenido.add(panelPreciosContenedor);

        // Botón para agregar más precios
        btnAgregarPrecio = new JButton("+ Agregar precio");
        btnAgregarPrecio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnAgregarPrecio.setForeground(new Color(25, 88, 156));
        btnAgregarPrecio.setContentAreaFilled(false);
        btnAgregarPrecio.setBorderPainted(false);
        btnAgregarPrecio.setFocusPainted(false);
        btnAgregarPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAgregarPrecio.addActionListener(e -> agregarNuevoPrecio());
        
        panelContenido.add(btnAgregarPrecio);

        // Agregamos el primer precio por defecto
        agregarNuevoPrecio();

        JScrollPane scroll = new JScrollPane(panelContenido);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // --- BOTÓN GUARDAR ---
        JButton btnGuardar = new JButton("GUARDAR");
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setPreferredSize(new Dimension(0, 50));
        btnGuardar.addActionListener(e -> dispose());
        add(btnGuardar, BorderLayout.SOUTH);
    }

    private void agregarNuevoPrecio() {
        if (contadorPrecios < MAX_PRECIOS) {
            contadorPrecios++;
            JPanel cardPrecio = crearCardPrecio(contadorPrecios);
            panelPreciosContenedor.add(cardPrecio);
            panelPreciosContenedor.add(Box.createVerticalStrut(15));
            
            if (contadorPrecios == MAX_PRECIOS) {
                btnAgregarPrecio.setEnabled(false);
            }
            
            panelPreciosContenedor.revalidate();
            panelPreciosContenedor.repaint();
        }
    }

    private JPanel crearCardPrecio(int numero) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(1000, 180));

        // Header del precio (Título y botón eliminar)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitulo = new JLabel("PRECIO " + numero);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(new Color(150, 150, 150));
        header.add(lblTitulo, BorderLayout.WEST);

        if (numero > 1) {
            JButton btnEliminar = new JButton("🗑"); // Icono de basurita
            btnEliminar.setForeground(Color.RED);
            btnEliminar.setBorderPainted(false);
            btnEliminar.setContentAreaFilled(false);
            btnEliminar.addActionListener(e -> {
                panelPreciosContenedor.remove(card);
                contadorPrecios--;
                btnAgregarPrecio.setEnabled(true);
                panelPreciosContenedor.revalidate();
                panelPreciosContenedor.repaint();
            });
            header.add(btnEliminar, BorderLayout.EAST);
        }

        // Grid de valores (Utilidad, Descuento, Precio, Con Impuesto)
        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 10));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        grid.add(crearItemPrecio("Utilidad %", "0.000000"));
        grid.add(crearItemPrecio("Descuento %", "0.000000"));
        grid.add(crearItemPrecio("Precio", "$0.000000"));
        grid.add(crearItemPrecio("Con impuesto", "$0.000000"));

        card.add(header, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearItemPrecio(String label, String valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Color.GRAY);
        JTextField t = new JTextField(valor);
        t.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(l, BorderLayout.NORTH);
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    // --- MÉTODOS DE SOPORTE (IMÁGENES, CAMPOS, SWITCH) ---
    private JPanel crearCuadroImagen(boolean conIcono) {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(100, 100));
        p.setBackground(new Color(240, 240, 240));
        p.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        if (conIcono) {
            p.setBackground(Color.WHITE);
            JLabel lbl = new JLabel("Imagen", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lbl.setForeground(new Color(25, 88, 156));
            p.add(new JLabel("📷", SwingConstants.CENTER), BorderLayout.CENTER);
            p.add(lbl, BorderLayout.SOUTH);
        }
        return p;
    }

    private JPanel crearCampoEstilizado(String titulo, String valor, boolean conIconoBusqueda) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(1000, 60));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        JTextField txt = new JTextField(valor);
        txt.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(lbl, BorderLayout.NORTH);
        p.add(txt, BorderLayout.CENTER);
        if(conIconoBusqueda) p.add(new JLabel("🔍 "), BorderLayout.EAST);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return p;
    }

    private JPanel crearSelectorEstilizado(String titulo, String valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(1000, 60));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        JLabel val = new JLabel(valor.isEmpty() ? " " : valor);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        p.add(lbl, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        p.add(new JLabel(" > "), BorderLayout.EAST);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return p;
    }

    private JPanel crearFilaSwitch(String texto, boolean activo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(1000, 50));
        p.add(new JLabel(texto), BorderLayout.WEST);
        JLabel lblSwitch = new JLabel(activo ? "🔵" : "⚪"); 
        p.add(lblSwitch, BorderLayout.EAST);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
        return p;
    }
}