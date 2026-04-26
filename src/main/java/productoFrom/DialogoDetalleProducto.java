package productoFrom;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

public class DialogoDetalleProducto extends JDialog {

    private int indiceImagenActual = 0;

    public DialogoDetalleProducto(java.awt.Window parent, ProductoDTO p) {
        super(parent, "Detalles del Producto", Dialog.ModalityType.APPLICATION_MODAL);
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
