package productoFrom;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DialogoDetalleProducto extends JDialog {

    private JLabel lblImagen;
    private static final int ANCHO_IMG = 350;
    private static final int ALTO_IMG = 350;

    public DialogoDetalleProducto(java.awt.Window parent, ProductoDTO p) {
        super(parent, "Detalles del Producto", Dialog.ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setLayout(new BorderLayout());

        setSize(800, 520);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));

        // --- PANEL PRINCIPAL (Fondo Redondeado y Sombra) ---
        JPanel mainPanel = new JPanel(new BorderLayout(40, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar sombra
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 35, 35);

                // Dibujar fondo panel
                g2.setColor(new Color(248, 249, 250));
                g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 35, 35);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // --- IZQUIERDA: IMAGEN Y BOTÓN DE BÚSQUEDA ---
        JPanel panelIzquierdo = new JPanel(new GridBagLayout());
        panelIzquierdo.setOpaque(false);
        GridBagConstraints gbcImg = new GridBagConstraints();

        lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(ANCHO_IMG, ALTO_IMG));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        actualizarIconoImagen(p.getImagePath(), p.getName());

        gbcImg.gridx = 0;
        gbcImg.gridy = 0;
        gbcImg.insets = new Insets(0, 0, 15, 0);
        panelIzquierdo.add(lblImagen, gbcImg);

        // Botón Buscar Imagen Aleatoria (Moderno)
        JButton btnBuscarWeb = new JButton();
        btnBuscarWeb.setText("<html><body style='text-align: center; color: white;'>"
                + "<span style='font-size: 18px;'>⟳</span> &nbsp; "
                + "<span style='font-size: 13px;'>Buscar Imagen Aleatoria</span>"
                + "</body></html>");
        btnBuscarWeb.setBackground(new Color(79, 70, 229));
        btnBuscarWeb.setPreferredSize(new Dimension(ANCHO_IMG, 50));
        btnBuscarWeb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscarWeb.putClientProperty("JButton.buttonType", "roundRect");
        btnBuscarWeb.setFocusPainted(false);
        btnBuscarWeb.setBorderPainted(false);

        btnBuscarWeb.addActionListener(e -> {
            btnBuscarWeb.setEnabled(false);
            btnBuscarWeb.setText("Buscando...");

            new Thread(() -> {
                String urlFinal = buscarImagenPerfecta(p.getName());
                SwingUtilities.invokeLater(() -> {
                    if (urlFinal != null) {
                        p.setImagePath(urlFinal);
                        actualizarIconoImagen(urlFinal, p.getName());

                        // --- ESTO ES LO QUE FALTA ---
                        lblImagen.revalidate();
                        lblImagen.repaint();
                    } else {
                        JOptionPane.showMessageDialog(this, "No se encontraron imágenes para: " + p.getName(), "Búsqueda Fallida", JOptionPane.WARNING_MESSAGE);
                    }
                    btnBuscarWeb.setEnabled(true);
                    btnBuscarWeb.setText("<html><span style='font-size:16px'>⟳</span> Buscar Imagen Real</html>");
                });
            }).start();
        });

        gbcImg.gridy = 1;
        panelIzquierdo.add(btnBuscarWeb, gbcImg);
        mainPanel.add(panelIzquierdo, BorderLayout.WEST);

        // --- DERECHA: INFORMACIÓN DEL PRODUCTO ---
        JPanel pInfo = new JPanel();
        pInfo.setLayout(new BoxLayout(pInfo, BoxLayout.Y_AXIS));
        pInfo.setOpaque(false);

        // 1. Panel SKU + Botón Copiar
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
        btnCopiar.setBackground(Color.WHITE);
        btnCopiar.addActionListener(e -> {
            StringSelection ss = new StringSelection(p.getSku());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
            btnCopiar.setText("✅");
            new Timer(1000, ev -> btnCopiar.setText("📋")).start();
        });
        panelSKU.add(lblSKU);
        panelSKU.add(btnCopiar);

        // 2. Nombre y Precio
        JLabel lblNom = new JLabel("<html><body>" + p.getName() + "</body></html>");
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblNom.setForeground(new Color(45, 52, 54));

        JLabel lblPre = new JLabel("$" + String.format("%.2f", p.getPrice()) + " MXN");
        lblPre.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblPre.setForeground(new Color(40, 167, 69));

        pInfo.add(panelSKU);
        pInfo.add(Box.createVerticalStrut(15));
        pInfo.add(lblNom);
        pInfo.add(Box.createVerticalStrut(10));
        pInfo.add(lblPre);
        pInfo.add(Box.createVerticalGlue());

        mainPanel.add(pInfo, BorderLayout.CENTER);

        // --- BOTÓN INFERIOR DE CIERRE ---
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setOpaque(false);
        JButton btnCerrar = new JButton("ENTENDIDO");
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

    private void actualizarIconoImagen(String ruta, String nombre) {
        lblImagen.setIcon(cargarImagenInterna(ruta, ANCHO_IMG, ALTO_IMG, nombre));
    }

    // MEJORA DE NITIDEZ: Usa RenderingHints para evitar lo borroso
    private ImageIcon cargarImagenInterna(String ruta, int w, int h, String nombre) {
        if (ruta != null && !ruta.isEmpty()) {
            try {
                Image imgRaw = (ruta.startsWith("http"))
                        ? new ImageIcon(new URL(ruta)).getImage()
                        : new ImageIcon(ruta).getImage();

                BufferedImage dimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = dimg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(imgRaw, 0, 0, w, h, null);
                g2.dispose();
                return new ImageIcon(dimg);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        // Placeholder moderno
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
public String buscarImagenPerfecta(String nombreProducto) {
        // 1. Limpiamos y enfocamos la búsqueda para forzar un producto real
        String query = nombreProducto + " producto oficial alta resolucion";
        List<String> poolDeImagenes = new ArrayList<>();

        try {
            String queryCodificada = URLEncoder.encode(query, StandardCharsets.UTF_8);
            
            // Usamos Bing porque su HTML permite extraer la imagen ORIGINAL, no la miniatura.
            String url = "https://www.bing.com/images/search?q=" + queryCodificada + "&form=HDRSC2";

            // 2. Conexión simulando navegador real
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .referrer("https://www.bing.com/")
                    .timeout(10000)
                    .get();

            // 3. EL SECRETO: Bing oculta los datos de la imagen de alta calidad en anclas con clase "iusc"
            Elements enlacesImagenes = doc.select("a.iusc");

            for (Element enlace : enlacesImagenes) {
                // El atributo "m" contiene un pseudo-JSON con la URL en alta resolución bajo la clave "murl"
                String mData = enlace.attr("m");

                if (mData != null && mData.contains("\"murl\":\"")) {
                    // Extraemos la URL real recortando el String
                    String imgUrl = mData.split("\"murl\":\"")[1].split("\"")[0];

                    // FILTRO DE CALIDAD EXTREMO:
                    // Exigimos que termine en extensión de imagen y rechazamos iconos o basura de UI
                    String urlLower = imgUrl.toLowerCase();
                    if (imgUrl.startsWith("http") 
                            && !urlLower.contains("logo") 
                            && !urlLower.contains("icon")
                            && !urlLower.contains("placeholder")
                            && (urlLower.endsWith(".jpg") || urlLower.endsWith(".png") || urlLower.endsWith(".jpeg") || urlLower.endsWith(".webp"))) {
                        
                        poolDeImagenes.add(imgUrl);
                    }
                }

                // Guardamos un buen grupo para poder hacer el random (10 es un buen número)
                if (poolDeImagenes.size() >= 10) {
                    break;
                }
            }

            // 4. ALEATORIEDAD REAL
            if (!poolDeImagenes.isEmpty()) {
                Collections.shuffle(poolDeImagenes); // Mezclamos la lista de 10 imágenes
                return poolDeImagenes.get(0);        // Devolvemos una al azar
            }

        } catch (Exception e) {
            System.err.println("Error en el motor de búsqueda: " + e.getMessage());
        }

        // Si todo falla, devuelve null (o podrías devolver un String con la URL de tu imagen genérica)
        return null;
    }
}
