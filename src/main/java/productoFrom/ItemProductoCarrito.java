package productoFrom;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ventas.ventas;
import static ventas.ventas.calcularTotales;
import static ventas.ventas.cargarImagen;
import static ventas.ventas.panelCarritoContenedor;
import static ventas.ventas.txtBuscador;

public class ItemProductoCarrito extends JPanel {

    public ProductoDTO producto;
    public JTextField txtCant;
    public JLabel lblImporte;
    public BigDecimal cantidad;

    public ItemProductoCarrito(ProductoDTO p) {
        this.producto = p;
        this.cantidad = BigDecimal.ZERO;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setFocusable(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // CORRECCIÓN DE ANCHO: Ajustado a 415px para que encaje perfectamente dentro del panel del carrito (430px) sin recortarse
        setMinimumSize(new Dimension(380, 90));
        setPreferredSize(new Dimension(415, 90));
        setMaximumSize(new Dimension(430, 90));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // 1. COLUMNA BOTONES GESTIÓN (IZQUIERDA)
        JPanel pGestion = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        pGestion.setOpaque(false);

        JButton btnEliminar = new JButton(crearIconoBasura());
        JButton btnEditar = new JButton(crearIconoEditar());
        estilizarBotonIcono(btnEliminar);
        estilizarBotonIcono(btnEditar);

        pGestion.add(btnEliminar);
        pGestion.add(btnEditar);

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(pGestion, gbc);

        // 2. COLUMNA IMAGEN
        JLabel lblImg = new JLabel(cargarImagen(p.getImagePath(), 45, 45, p.getName()));
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 4, 0, 6);
        add(lblImg, gbc);

        // 3. COLUMNA INFO (NOMBRE MULTILÍNEA AJUSTADO)
        JPanel pInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        pInfo.setOpaque(false);

        // Ancho optimizado a 110px para balancear el espacio con los controles y el precio
        String nombreFormateado = "<html><body style='width: 110px;'>" + p.getName() + "</body></html>";
        JLabel lblNombre = new JLabel(nombreFormateado);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblPrecioU = new JLabel("Unit: $" + p.getPrice());
        lblPrecioU.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPrecioU.setForeground(new Color(100, 116, 139));

        pInfo.add(lblNombre);
        pInfo.add(lblPrecioU);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 4);
        add(pInfo, gbc);

        // 4. COLUMNA CONTROLES (BOTONES + Y -)
        JPanel pControles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        pControles.setOpaque(false);

        JButton btnMenos = new JButton("-");
        JButton btnMas = new JButton("+");
        estilizarBotonMasMenos(btnMenos);
        estilizarBotonMasMenos(btnMas);

        txtCant = new JTextField("1");
        txtCant.setPreferredSize(new Dimension(45, 26));
        txtCant.setHorizontalAlignment(JTextField.CENTER);
        txtCant.putClientProperty("JComponent.roundRect", true);

        pControles.add(btnMenos);
        pControles.add(txtCant);
        pControles.add(btnMas);

        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(pControles, gbc);

        // 5. COLUMNA IMPORTE (DERECHA EXTREMA)
        lblImporte = new JLabel("$0.00");
        lblImporte.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblImporte.setForeground(new Color(79, 70, 229));
        lblImporte.setPreferredSize(new Dimension(75, 25));
        lblImporte.setHorizontalAlignment(SwingConstants.RIGHT);

        gbc.gridx = 4;
        gbc.insets = new Insets(0, 4, 0, 2);
        add(lblImporte, gbc);

        // --- LÓGICA DE FUNCIONAMIENTO ---
        btnEliminar.addActionListener(e -> {
            panelCarritoContenedor.remove(this);
            panelCarritoContenedor.revalidate();
            panelCarritoContenedor.repaint();
            calcularTotales();
            txtBuscador.requestFocusInWindow();
        });

        btnEditar.addActionListener(e -> abrirDialogoEdicionBascula());

        btnMenos.addActionListener(e -> {
            BigDecimal salto = producto.isGranel() ? new BigDecimal("0.100") : BigDecimal.ONE;
            if (cantidad.compareTo(salto) >= 0) {
                setCantidad(cantidad.subtract(salto));
            }
        });

        btnMas.addActionListener(e -> {
            BigDecimal salto = producto.isGranel() ? new BigDecimal("0.100") : BigDecimal.ONE;
            setCantidad(cantidad.add(salto));
        });

        txtCant.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { act(); }
            public void removeUpdate(DocumentEvent e) { act(); }
            public void changedUpdate(DocumentEvent e) { act(); }

            public void act() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String t = txtCant.getText().trim();
                        if (!t.isEmpty()) {
                            cantidad = new BigDecimal(t);
                            actualizarImporte();
                        }
                    } catch (Exception ex) {
                    }
                });
            }
        });

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                setBackground(new Color(245, 247, 251));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(79, 70, 229)),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
            }

            public void focusLost(FocusEvent e) {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
            }
        });
    }

    public void estilizarBotonIcono(JButton b) {
        b.setFocusable(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void estilizarBotonMasMenos(JButton b) {
        b.setPreferredSize(new Dimension(26, 26));
        b.setFocusable(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(242, 244, 247));
        b.setForeground(new Color(51, 65, 85));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
    }

    public void abrirDialogoEdicionBascula() {
        Container parent = this.getParent();
        while (parent != null && !(parent instanceof ventas)) {
            parent = parent.getParent();
        }
        if (parent instanceof ventas v) {
            panelCarritoContenedor.remove(this);
            panelCarritoContenedor.revalidate();
            panelCarritoContenedor.repaint();
            v.alSeleccionarProducto(this.producto);
            v.calcularTotales();
        }
    }

    public void setCantidad(BigDecimal nuevaCantidad) {
        this.cantidad = nuevaCantidad;
        String formato = producto.isGranel() ? "%.3f" : "%.0f";
        txtCant.setText(String.format(java.util.Locale.US, formato, cantidad));
        actualizarImporte();
    }

    public void actualizarImporte() {
        if (producto != null && cantidad != null) {
            BigDecimal importe = producto.getPrice().multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
            lblImporte.setText(String.format("$ %.2f", importe));
            Container parent = this.getParent();
            while (parent != null && !(parent instanceof ventas)) {
                parent = parent.getParent();
            }
            if (parent instanceof ventas v) {
                v.calcularTotales();
            }
        }
    }

    public Icon crearIconoEditar() {
        BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0, 123, 255));
        g.rotate(Math.toRadians(45), 8, 8);
        g.fillRect(6, 2, 4, 12);
        g.dispose();
        return new ImageIcon(bi);
    }

    public Icon crearIconoBasura() {
        BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(239, 68, 68));
        g.fillRect(4, 5, 8, 8);
        g.fillRect(2, 3, 12, 2);
        g.dispose();
        return new ImageIcon(bi);
    }

    public BigDecimal getCantidad() {
        return this.cantidad;
    }

    public ProductoDTO getProducto() {
        return producto;
    }
}