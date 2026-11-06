package com.mycompany.chancuellarpuntodeventa.login;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.chancuellarpuntodeventa.services.dashboard.dashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

@Component
public class LoginInterface extends JFrame {

    @Autowired
    private ApplicationContext context;
    private JButton iniciarButton; // Lo hacemos global de la clase para poder rehabilitarlo

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

    // Método para resetear el botón cuando se cierra sesión y se regresa aquí
    public void reiniciarLogin() {
        if (iniciarButton != null) {
            iniciarButton.setEnabled(true);
        }
        setVisible(true);
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

        JPanel formContainer = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
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
        fcGbc.insets = new Insets(0, 0, 40, 0);
        formContainer.add(mainTitle, fcGbc);

        // Botón Iniciar
        iniciarButton = new JButton("INICIAR");
        iniciarButton.setBackground(new Color(0, 90, 180));
        iniciarButton.setForeground(Color.WHITE);
        iniciarButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        iniciarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        iniciarButton.putClientProperty(FlatClientProperties.STYLE, "arc:14; borderWidth:0; focusWidth:0");
        iniciarButton.setPreferredSize(new Dimension(320, 50));

        iniciarButton.addActionListener(e -> {
            iniciarButton.setEnabled(false);
            iniciarCargaYMedirTiempoTotal();
        });

        fcGbc.gridy = 1;
        fcGbc.insets = new Insets(0, 0, 0, 0);
        formContainer.add(iniciarButton, fcGbc);

        rightPanel.add(formContainer, gbc);
        add(rightPanel);
    }

    private void iniciarCargaYMedirTiempoTotal() {
        JDialog dialogoCarga = new JDialog(this, "", true);
        dialogoCarga.setUndecorated(true);
        dialogoCarga.setBackground(new Color(0, 0, 0, 0));
        dialogoCarga.setSize(420, 310);
        dialogoCarga.setLocationRelativeTo(this);
        dialogoCarga.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialogoCarga.setLayout(new BorderLayout());

        TransparentLoaderPanel loaderPanel = new TransparentLoaderPanel();
        dialogoCarga.add(loaderPanel, BorderLayout.CENTER);

        final dashboard[] dashHolder = new dashboard[1];
        final boolean[] procesoTerminado = {false};
        final long tiempoInicio = System.currentTimeMillis();

        // Hilo en segundo plano para inicializar el dashboard y sus componentes pesados
        Thread hiloCargaTotal = new Thread(() -> {
            try {
                dashHolder[0] = context.getBean(dashboard.class);
                procesoTerminado[0] = true;
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    dialogoCarga.dispose();
                    iniciarButton.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Error crítico al cargar el sistema: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
        hiloCargaTotal.start();

        // Timer de UI sincronizado milisegundo a milisegundo
        Timer timerUI = new Timer(30, null);
        timerUI.addActionListener(e -> {
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
            double segundosActuales = tiempoTranscurrido / 1000.0;

            if (procesoTerminado[0]) {
                timerUI.stop();
                loaderPanel.setPorcentaje(100);
                loaderPanel.setMensajeEstado(String.format("¡Listo en %.1f segundos!", segundosActuales));

                // Pausa visual para mostrar el tiempo final antes de mostrar el dashboard
                Timer pauseTimer = new Timer(700, evt -> {
                    dialogoCarga.dispose();
                    this.setVisible(false); // Ocultamos el login en lugar de destruirlo bruscamente
                    if (dashHolder[0] != null) {
                        dashHolder[0].setVisible(true);
                    }
                });
                pauseTimer.setRepeats(false);
                pauseTimer.start();
            } else {
                int estimado = (int) Math.min(95, (tiempoTranscurrido / 100.0));
                if (estimado <= loaderPanel.getPorcentaje()) {
                    estimado = loaderPanel.getPorcentaje() + 1;
                    if (estimado > 95) estimado = 95;
                }
                loaderPanel.setPorcentaje(estimado);
                loaderPanel.setMensajeEstado(String.format("Cargando base de datos... (%.1fs)", segundosActuales));
            }
        });

        timerUI.start();
        dialogoCarga.setVisible(true);
    }

    // Panel visual con diseño Glassmorphism y anillo circular neón transparente
    private static class TransparentLoaderPanel extends JPanel {
        private int porcentaje = 0;
        private int anguloRotacion = 0;
        private String mensajeEstado = "Iniciando Sistema...";
        private Timer timerSpin;

        public TransparentLoaderPanel() {
            setOpaque(false);
            timerSpin = new Timer(15, e -> {
                anguloRotacion = (anguloRotacion + 6) % 360;
                repaint();
            });
            timerSpin.start();
        }

        public void setPorcentaje(int p) {
            this.porcentaje = Math.min(100, p);
            repaint();
            if (this.porcentaje >= 100 && timerSpin.isRunning()) {
                timerSpin.stop();
            }
        }

        public int getPorcentaje() {
            return this.porcentaje;
        }

        public void setMensajeEstado(String msg) {
            this.mensajeEstado = msg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Fondo flotante translúcido (Glassmorphism oscuro elegante)
            g2.setColor(new Color(15, 23, 42, 220));
            g2.fillRoundRect(20, 20, w - 40, h - 40, 24, 24);

            g2.setColor(new Color(255, 255, 255, 45));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(20, 20, w - 40, h - 40, 24, 24);

            // Spinner Circular Central
            int size = 110;
            int x = (w - size) / 2;
            int y = (h - size) / 2 - 15;

            g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawOval(x, y, size, size);

            g2.setColor(new Color(56, 189, 248));
            int arcAngle = - (int) ((porcentaje / 100.0) * 360);
            g2.draw(new Arc2D.Double(x, y, size, size, anguloRotacion, arcAngle, Arc2D.OPEN));

            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2.setColor(Color.WHITE);
            String textoPorcentaje = porcentaje + "%";
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(textoPorcentaje)) / 2;
            int ty = y + (size / 2) + 8;
            g2.drawString(textoPorcentaje, tx, ty);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.setColor(new Color(148, 163, 184));
            FontMetrics fm2 = g2.getFontMetrics();
            int tsx = (w - fm2.stringWidth(mensajeEstado)) / 2;
            g2.drawString(mensajeEstado, tsx, y + size + 28);

            g2.dispose();
        }
    }
}