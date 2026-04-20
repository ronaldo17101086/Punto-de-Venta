package com.mycompany.chancuellarpuntodeventa.login;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.chancuellarpuntodeventa.services.dashboard.dashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;

@Component
public class LoginInterface extends JFrame {

    @Autowired
    private dashboard mainDashboard;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginInterface() {
        // Configuración de la ventana principal
        setTitle("SICAR X v1.5.10");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));

        initLeftPanel();
        initRightPanel();
    }

    private void initLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(0, 82, 175)); // Azul SICAR

        // Logo y texto central
        JLabel mainLogo = new JLabel("<html><div style='text-align: center;'>SICARX<br><font size='5'>Hunab Ku</font></div></html>", SwingConstants.CENTER);
        mainLogo.setForeground(Color.WHITE);
        mainLogo.setFont(new Font("SansSerif", Font.BOLD, 35));

        leftPanel.add(mainLogo, BorderLayout.CENTER);
        add(leftPanel);
    }

    private void initRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 60, 10, 60);
        gbc.gridx = 0;

        // 1. Logo Texto
        JLabel logoLabel = new JLabel("SICARX", SwingConstants.CENTER);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 45));
        logoLabel.setForeground(new Color(50, 50, 50));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 60, 30, 60);
        rightPanel.add(logoLabel, gbc);

        // 2. Campo de Usuario
        gbc.insets = new Insets(5, 60, 5, 60);
        JLabel userLabel = new JLabel("Usuario / Correo");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        gbc.gridy = 1;
        rightPanel.add(userLabel, gbc);

        usernameField = new JTextField();
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "admin");
        gbc.gridy = 2;
        rightPanel.add(usernameField, gbc);

        // 3. Campo de Contraseña
        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        gbc.gridy = 3;
        rightPanel.add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "admin");
        gbc.gridy = 4;
        rightPanel.add(passwordField, gbc);

        // 4. Botón Ingresar
        JButton loginButton = new JButton("INGRESAR");
        loginButton.setBackground(new Color(0, 90, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        gbc.gridy = 5;
        gbc.ipady = 15;
        gbc.insets = new Insets(20, 60, 10, 60);
        rightPanel.add(loginButton, gbc);

        // Lógica de acceso
        loginButton.addActionListener(e -> {
            String userValue = usernameField.getText();
            String passValue = new String(passwordField.getPassword());

            if (userValue.equals("admin") && passValue.equals("admin")) {
                this.dispose();
                mainDashboard.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Acceso denegado: Usuario o contraseña inválidos.",
                        "Error de Login",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // 5. Botón Crear Cuenta
        JButton registerButton = new JButton("CREAR CUENTA");
        registerButton.setBackground(new Color(40, 167, 69));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        gbc.gridy = 6;
        gbc.ipady = 15;
        gbc.insets = new Insets(0, 60, 10, 60);
        rightPanel.add(registerButton, gbc);

        add(rightPanel);
    }
}
