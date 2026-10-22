package com.mycompany.chancuellarpuntodeventa.login;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.chancuellarpuntodeventa.services.dashboard.dashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;

@Component
public class LoginInterface extends JFrame {

    @Autowired
    private ApplicationContext context;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginInterface() {
        setTitle("Punto de Venta Chancuellar");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));

        initLeftPanel();
        initRightPanel();
    }

    private void initLeftPanel() {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                java.net.URL imgURL = getClass().getResource("/images/Logotipo.jpg");
                if (imgURL != null) {
                    Image imagenFondo = new ImageIcon(imgURL).getImage();
                    g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(15, 70, 15));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        add(leftPanel);
    }

    private void initRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Contenedor principal del formulario con un ancho controlado (para que no se estire feo)
        JPanel formContainer = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Opcional: Si quieres un fondo de tarjeta muy sutil con bordes redondeados
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(252, 253, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(235, 240, 245));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        formContainer.setOpaque(false);
        formContainer.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        GridBagConstraints fcGbc = new GridBagConstraints();
        fcGbc.gridx = 0;
        fcGbc.fill = GridBagConstraints.HORIZONTAL;
        fcGbc.weightx = 1.0;

        // Título estilizado
        JLabel mainTitle = new JLabel("<html><div style='text-align: center;'>Punto de Venta<br><span style='color: #005AB4; font-size: 24px;'>Chancuellar</span></div></html>", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Serif", Font.BOLD, 32));
        fcGbc.gridy = 0;
        fcGbc.insets = new Insets(0, 0, 30, 0);
        formContainer.add(mainTitle, fcGbc);

        // Usuario
        JLabel userLabel = new JLabel("Usuario");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLabel.setForeground(new Color(110, 120, 130));
        fcGbc.gridy = 1;
        fcGbc.insets = new Insets(0, 0, 5, 0);
        formContainer.add(userLabel, fcGbc);

        usernameField = new JTextField();
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su usuario");
        usernameField.putClientProperty(FlatClientProperties.STYLE, "arc:12; margin:5,10,5,10");
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        usernameField.setPreferredSize(new Dimension(320, 42));
        fcGbc.gridy = 2;
        fcGbc.insets = new Insets(0, 0, 18, 0);
        formContainer.add(usernameField, fcGbc);

        // Contraseña
        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        passLabel.setForeground(new Color(110, 120, 130));
        fcGbc.gridy = 3;
        fcGbc.insets = new Insets(0, 0, 5, 0);
        formContainer.add(passLabel, fcGbc);

        passwordField = new JPasswordField();
        passwordField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true; arc:12; margin:5,10,5,10");
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su contraseña");
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        passwordField.setPreferredSize(new Dimension(320, 42));
        fcGbc.gridy = 4;
        fcGbc.insets = new Insets(0, 0, 25, 0);
        formContainer.add(passwordField, fcGbc);

        // Botón Ingresar con estilo moderno
        JButton loginButton = new JButton("INGRESAR");
        loginButton.setBackground(new Color(0, 90, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.putClientProperty(FlatClientProperties.STYLE, "arc:14; borderWidth:0; focusWidth:0");
        loginButton.setPreferredSize(new Dimension(320, 46));

        loginButton.addActionListener(e -> {
            if (usernameField.getText().equals("admin") && new String(passwordField.getPassword()).equals("admin")) {
                this.dispose();
                context.getBean(dashboard.class).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Acceso denegado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        fcGbc.gridy = 5;
        fcGbc.insets = new Insets(0, 0, 0, 0);
        formContainer.add(loginButton, fcGbc);

        // Añadimos la tarjeta al panel derecho centrada perfectamente
        rightPanel.add(formContainer, gbc);
        add(rightPanel);
    }
}