package productoFrom;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import com.mycompany.chancuellarpuntodeventa.services.repository.ProductoRepository;
import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;

public class EditarProductoDialog extends JFrame { // Cambiado a JFrame para tener botones de ventana

    private JTextField txtNombre, txtSku, txtPrecio, txtDescripcion;
    private JLabel lblPreviewImagen;
    private String rutaImagen = "";
    private ProductoRepository repo;
    private ProductoDTO dto;
    private JFrame dashboardFrame;

    private final Color COLOR_HEADER = new Color(28, 78, 134);
    private final Color COLOR_GUARDAR = new Color(40, 167, 69);
    private final Color COLOR_REGRESAR_HOVER = new Color(40, 90, 150);

    public EditarProductoDialog(JFrame parent, ProductoDTO dto, ProductoRepository repo) {
        this.dashboardFrame = parent;
        this.dto = dto;
        this.repo = repo;

        setTitle("Editar Producto - " + dto.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Copiamos el tamaño exacto del dashboard para que no se sienta el cambio
        setSize(parent.getWidth(), parent.getHeight());
        setLocation(parent.getLocation());

        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // --- 1. CABECERA CON BOTÓN "ATRAS" ESTILIZADO ---
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setPreferredSize(new Dimension(0, 60));

        // Botón Regresar tipo "Flat"
        JPanel btnBack = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        btnBack.setOpaque(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcono = new JLabel("⬅"); // Icono de flecha
        lblIcono.setForeground(Color.WHITE);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel lblTextoBack = new JLabel("Regresar al catálogo");
        lblTextoBack.setForeground(Color.WHITE);
        lblTextoBack.setFont(new Font("Segoe UI", Font.BOLD, 16));

        btnBack.add(lblIcono);
        btnBack.add(lblTextoBack);

        // Efecto Hover para el botón de regresar
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                regresar();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btnBack.setOpaque(true);
                btnBack.setBackground(COLOR_REGRESAR_HOVER);
                btnBack.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnBack.setOpaque(false);
                btnBack.repaint();
            }
        });

        panelHeader.add(btnBack, BorderLayout.WEST);
        add(panelHeader, BorderLayout.NORTH);

        // --- 2. CONTENIDO ---
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(30, 60, 30, 60));

        // SECCIÓN FOTOS (Con funcionalidad)
        container.add(crearSeccionFotos());
        container.add(Box.createVerticalStrut(30));

        // SECCIÓN DATOS
        txtSku = crearCampoMaterial(dto.getSku(), false);
        txtNombre = crearCampoMaterial(dto.getName(), true);
        txtPrecio = crearCampoMaterial(String.valueOf(dto.getPrice()), true);

        container.add(crearFila("CLAVE / SKU", txtSku));
        container.add(Box.createVerticalStrut(20));
        container.add(crearFila("NOMBRE DEL PRODUCTO", txtNombre));
        container.add(Box.createVerticalStrut(20));
        container.add(crearFila("DESCRIPCIÓN", txtDescripcion));
        container.add(Box.createVerticalStrut(30));

        // PANEL PRECIOS (Estilo Caja)
        container.add(crearCajaPrecios());

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // --- 3. BOTÓN GUARDAR ---
        JButton btnGuardar = new JButton("GUARDAR CAMBIOS");
        btnGuardar.setBackground(COLOR_GUARDAR);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.setPreferredSize(new Dimension(0, 65));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.addActionListener(e -> guardar());
        add(btnGuardar, BorderLayout.SOUTH);

        // Al final del constructor, después de cargar los campos de texto:
        if (dto.getImagePath() != null && !dto.getImagePath().isEmpty()) {
            this.rutaImagen = dto.getImagePath();
            ImageIcon icon = new ImageIcon(new ImageIcon(rutaImagen).getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));
            lblPreviewImagen.setIcon(icon);
            lblPreviewImagen.setText("");
        }
    }

    private JPanel crearSeccionFotos() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        p.setOpaque(false);

        // Cuadro Principal para agregar foto
        lblPreviewImagen = new JLabel("➕ Agregar Foto", SwingConstants.CENTER);
        lblPreviewImagen.setPreferredSize(new Dimension(150, 150));
        lblPreviewImagen.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 2, 2));
        lblPreviewImagen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPreviewImagen.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        lblPreviewImagen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarImagen();
            }
        });

        p.add(lblPreviewImagen);

        // Otros cuadros decorativos (estilo SICAR)
        for (int i = 0; i < 2; i++) {
            JPanel cuadro = new JPanel();
            cuadro.setPreferredSize(new Dimension(150, 150));
            cuadro.setBackground(new Color(248, 248, 248));
            cuadro.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            p.add(cuadro);
        }
        return p;
    }

    private JPanel crearCajaPrecios() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(252, 252, 252));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);
        grid.add(crearFila("PRECIO DE VENTA", txtPrecio));

        // Campo utilidad simulado
        JTextField txtUtil = crearCampoMaterial("0.00", false);
        grid.add(crearFila("UTILIDAD %", txtUtil));

        p.add(new JLabel("CONFIGURACIÓN DE PRECIOS"), BorderLayout.NORTH);
        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearFila(String titulo, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(120, 120, 120));
        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JTextField crearCampoMaterial(String texto, boolean editable) {
        JTextField tf = new JTextField(texto);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        tf.setEditable(editable);
        if (!editable) {
            tf.setBackground(new Color(245, 245, 245));
        }
        return tf;
    }

    private void seleccionarImagen() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            rutaImagen = file.getAbsolutePath();
            ImageIcon icon = new ImageIcon(new ImageIcon(rutaImagen).getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));
            lblPreviewImagen.setIcon(icon);
            lblPreviewImagen.setText("");
            lblPreviewImagen.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
    }

    private void regresar() {
        this.dispose();
        dashboardFrame.setVisible(true);
    }

    private void guardar() {
        try {
            ProductoDTO p = repo.findBySku(dto.getSku());
            if (p != null) {
                p.setName(txtNombre.getText());
                try {
                    // 1. Limpiamos el texto (quitamos espacios, comas y signos de pesos)
                    String precioTexto = txtPrecio.getText().replace("$", "").replace(",", "").trim();

                    if (precioTexto.isEmpty()) {
                        p.setPrice(java.math.BigDecimal.ZERO);
                    } else {
                        // 2. Convertimos el String directamente a BigDecimal
                        p.setPrice(new java.math.BigDecimal(precioTexto));
                    }
                } catch (NumberFormatException e) {
                    // 3. Si el usuario escribió letras o algo inválido, ponemos cero o mandamos error
                    p.setPrice(java.math.BigDecimal.ZERO);
                    JOptionPane.showMessageDialog(this, "Precio inválido. Use solo números y punto decimal.");
                }

                // --- ESTA ES LA LÍNEA QUE FALTA ---
                // Si la variable rutaImagen tiene algo (porque seleccionaste una foto), se guarda en la BD
                if (!rutaImagen.isEmpty()) {
                    p.setImagePath(rutaImagen);
                }

                repo.save(p);
                JOptionPane.showMessageDialog(this, "✅ Producto guardado correctamente.");
                regresar();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Esto te dirá en consola si hay un error real
            JOptionPane.showMessageDialog(this, "❌ Error al guardar.");
        }
    }
}
