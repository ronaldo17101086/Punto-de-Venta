package productoFrom;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

class ProductListRenderer extends JPanel implements ListCellRenderer<ProductoDTO> {

    private JLabel lblNombre = new JLabel();
    private JLabel lblPrecio = new JLabel();
    private JLabel lblSku = new JLabel();
    private JLabel lblFoto = new JLabel(); // Nuevo Label para la imagen

    public ProductListRenderer() {
        setLayout(new BorderLayout(15, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Configuración de la miniatura de la foto
        lblFoto.setPreferredSize(new Dimension(50, 50));
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
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
    public java.awt.Component getListCellRendererComponent(
            JList<? extends ProductoDTO> list,
            ProductoDTO value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        // 1. Seteamos los datos de texto
        lblNombre.setText(value.getName());
        lblSku.setText("SKU: " + value.getSku());
        lblPrecio.setText("$" + String.format("%.2f", value.getPrice()));

        // 2. Lógica para cargar la imagen en la lista
        if (value.getImagePath() != null && !value.getImagePath().isEmpty()) {
            File file = new File(value.getImagePath());
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(value.getImagePath());
                // Escalamos la imagen a 50x50 para la lista
                Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                lblFoto.setIcon(new ImageIcon(img));
                lblFoto.setText("");
            } else {
                setIconDefault();
            }
        } else {
            setIconDefault();
        }

        // Colores de selección
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
        lblFoto.setText("📦"); // Emoji o puedes usar una imagen default
        lblFoto.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
    }
}
