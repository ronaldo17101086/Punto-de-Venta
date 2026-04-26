package productoFrom;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import static ventas.ventas.cargarImagen;

public class NotificationInterfice extends JDialog {

    private static NotificationInterfice instanciaActual = null;

    public NotificationInterfice(Window parent, String mensaje, ProductoDTO p) {
        super(parent);

        if (instanciaActual != null && instanciaActual.isVisible()) {
            instanciaActual.dispose();
        }
        instanciaActual = this;

        setUndecorated(true);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));
        setFocusableWindowState(false);
        setAlwaysOnTop(true);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0, 102, 255));
                g2.fillRoundRect(0, 0, 10, getHeight(), 20, 20);
                g2.fillRect(5, 0, 5, getHeight());
                g2.dispose();
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitulo = new JLabel(mensaje);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(new Color(0, 102, 255));

        // --- USANDO TUS VARIABLES ---
        String ruta = p.getImagePath(); //
        if (ruta != null && ruta.contains(",")) {
            ruta = ruta.split(",")[0].trim();
        }

        // Carga la imagen usando el nombre de tu variable name
        JLabel lblFoto = new JLabel(cargarImagen(ruta, 80, 80, p.getName())); //

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 0));
        info.setOpaque(false);

        JLabel name = new JLabel(p.getName()); //
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel price = new JLabel("$" + String.format("%.2f", p.getPrice())); //
        price.setFont(new Font("Segoe UI", Font.BOLD, 22));
        price.setForeground(new Color(40, 167, 69));

        info.add(name);
        info.add(price);

        JPanel contenido = new JPanel(new BorderLayout(15, 0));
        contenido.setOpaque(false);
        contenido.add(lblFoto, BorderLayout.WEST);
        contenido.add(info, BorderLayout.CENTER);

        mainPanel.add(lblTitulo, BorderLayout.NORTH);
        mainPanel.add(contenido, BorderLayout.CENTER);

        add(mainPanel);
        pack();

        if (parent != null) {
            setLocation(parent.getX() + parent.getWidth() - getWidth() - 340,
                    parent.getY() + parent.getHeight() - getHeight() - 30);
        }

        new Timer(3000, e -> dispose()).start();
    }
}
