package productoFrom;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

class ProductListRenderer extends JPanel implements ListCellRenderer<ProductoDTO> {

    private JLabel lblNombre = new JLabel();
    private JLabel lblPrecio = new JLabel();
    private JLabel lblSku = new JLabel();
    private JLabel lblFoto = new JLabel(); // Label para la imagen

    public ProductListRenderer() {
        setLayout(new BorderLayout(15, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Configuración de la miniatura de la foto
        lblFoto.setPreferredSize(new Dimension(50, 50));
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setVerticalAlignment(SwingConstants.CENTER);
        lblFoto.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        // Panel para los textos (Nombre y SKU)
        JPanel panelInfo = new JPanel(new GridLayout(2, 1));
        panelInfo.setOpaque(false);

        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSku.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSku.setForeground(Color.GRAY);

        panelInfo.add(lblNombre);
        panelInfo.add(lblSku);

        // Estilo del precio (Derecha)
        lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrecio.setForeground(new Color(40, 167, 69)); // Color verde

        // Agregamos los componentes al panel principal del renderer
        add(lblFoto, BorderLayout.WEST); // Foto a la izquierda
        add(panelInfo, BorderLayout.CENTER);
        add(lblPrecio, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends ProductoDTO> list,
            ProductoDTO value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        // 1. Seteamos los datos de texto
        lblNombre.setText(value.getName());
        lblSku.setText("SKU: " + value.getSku());
        lblPrecio.setText("$" + String.format("%.2f", value.getPrice()));

        // 2. BÚSQUEDA BLINDADA: Ruta guardada O búsqueda directa por SKU en la carpeta imgTienda
        File archivoImagen = null;

        // Intentar con la ruta que trae el DTO
        if (value.getImagePath() != null && !value.getImagePath().trim().isEmpty()) {
            File f = new File(value.getImagePath().trim().replace("\"", ""));
            if (f.exists()) {
                archivoImagen = f;
            }
        }

        // Si no existe, buscar directamente por el SKU en la carpeta "imgTienda" (ej: 1.png, 1.jpg)
        if (archivoImagen == null && value.getSku() != null && !value.getSku().trim().isEmpty()) {
            String skuLimpio = value.getSku().trim();
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

        // 3. Renderizar la miniatura si se encontró el archivo físico
        if (archivoImagen != null && archivoImagen.exists()) {
            try {
                ImageIcon icon = new ImageIcon(archivoImagen.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                lblFoto.setIcon(new ImageIcon(img));
                lblFoto.setText("");
            } catch (Exception e) {
                setIconDefault();
            }
        } else {
            setIconDefault();
        }

        // Colores de selección de la celda
        if (isSelected) {
            setBackground(new Color(232, 240, 254));
            lblNombre.setForeground(new Color(28, 78, 134));
        } else {
            setBackground(Color.WHITE);
            lblNombre.setForeground(Color.BLACK);
        }

        return this;
    }

    // Método auxiliar para poner un icono por defecto si no hay imagen
    private void setIconDefault() {
        lblFoto.setIcon(null);
        lblFoto.setText("📦"); 
        lblFoto.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
    }
}