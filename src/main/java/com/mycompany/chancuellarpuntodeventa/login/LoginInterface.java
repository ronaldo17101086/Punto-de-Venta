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
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        JPanel mainSplitPanel = new JPanel(new GridLayout(1, 2));
        initLeftPanel(mainSplitPanel);
        initRightPanel(mainSplitPanel);
        add(mainSplitPanel, BorderLayout.CENTER);
    }

    private void initLeftPanel(JPanel container) {
        // Creamos un panel personalizado que dibuja la imagen de fondo
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                java.net.URL imgURL = getClass().getResource("/images/logo.jpg");
                if (imgURL != null) {
                    Image imagenFondo = new ImageIcon(imgURL).getImage();
                    // Esto dibuja la imagen cubriendo TODO el panel izquierdo
                    g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fondo de respaldo por si no carga la imagen
                    g.setColor(new Color(15, 70, 15));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        container.add(leftPanel);
    }

    private void initRightPanel(JPanel container) {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 100, 10, 100);
        gbc.gridx = 0;

        JLabel mainTitle = new JLabel("Chancuellar", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Serif", Font.BOLD, 70));
        mainTitle.setForeground(new Color(40, 40, 40));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 100, 60, 100);
        rightPanel.add(mainTitle, gbc);

        gbc.insets = new Insets(10, 100, 5, 100);
        JLabel userLabel = new JLabel("Usuario / Correo");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        gbc.gridy = 1;
        rightPanel.add(userLabel, gbc);

        usernameField = new JTextField();
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "admin");
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 20));
        gbc.gridy = 2;
        rightPanel.add(usernameField, gbc);

        gbc.insets = new Insets(20, 100, 5, 100);
        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        gbc.gridy = 3;
        rightPanel.add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "admin");
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 20));
        gbc.gridy = 4;
        rightPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("INGRESAR");
        loginButton.setBackground(new Color(0, 90, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        gbc.gridy = 5;
        gbc.ipady = 30;
        gbc.insets = new Insets(50, 100, 20, 100);
        rightPanel.add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            if (usernameField.getText().equals("admin") && new String(passwordField.getPassword()).equals("admin")) {
                this.dispose();
                context.getBean(dashboard.class).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Acceso denegado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton registerButton = new JButton("CREAR CUENTA");
        registerButton.setBackground(new Color(40, 167, 69));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        gbc.gridy = 6;
        gbc.ipady = 30;
        gbc.insets = new Insets(0, 100, 20, 100);
        rightPanel.add(registerButton, gbc);

        container.add(rightPanel);
    }
}
