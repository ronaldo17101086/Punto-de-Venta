package tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Window;
import java.util.EnumMap;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

public class DialogoPago extends JDialog {

    private double totalVenta;
    private List<?> productos; // Para guardar los productos del carrito

    private EnumMap<FormaPago, JTextField> camposPago
            = new EnumMap<>(FormaPago.class);

    private JLabel lblPagado;
    private JLabel lblFalta;
    private JLabel lblCambio;

    private final NumberFormat formatoMoneda
            = NumberFormat.getNumberInstance(new Locale("es", "MX"));
    private boolean ventaConfirmada = false;

    public DialogoPago(Window parent, double total, List<?> productos) {
        super(parent);
        this.totalVenta = total;
        this.productos = productos;

        setModal(true);
        initUI();

        // === AGREGA ESTO PARA EL TAMAÑO ===
        this.setSize(1024, 720); // Tamaño estándar de tablet
        this.setLocationRelativeTo(parent); // Centrar respecto a la ventana principal
        this.setResizable(true); // Permitir que lo maximices si quieres
    }

    public boolean isVentaConfirmada() {
        return ventaConfirmada;
    }

    private void initUI() {
        // Configuramos el layout principal
        setLayout(new BorderLayout(0, 0));

        /* ==========================================================
       1. ÁREA DE COBRO (Ocupa todo el centro)
       ========================================================== */
        JPanel panelCobroPrincipal = new JPanel(new BorderLayout());
        panelCobroPrincipal.setBackground(new Color(245, 245, 245));

        // --- ENCABEZADO: TOTAL, PAGADO, FALTA Y CAMBIO ---
        // Ahora con 4 columnas para que quepan todos los indicadores arriba
        JPanel headerInfo = new JPanel(new GridLayout(1, 4, 10, 0));
        headerInfo.setBackground(Color.WHITE);
        headerInfo.setPreferredSize(new Dimension(0, 90));
        headerInfo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        Font fuenteTitulos = new Font("Segoe UI", Font.PLAIN, 14);
        Font fuenteMontos = new Font("Segoe UI", Font.BOLD, 26);

        // Caja Total
        JPanel cajaTotal = new JPanel(new GridLayout(2, 1));
        cajaTotal.setOpaque(false);
        JLabel lblT = new JLabel("Total a cobrar", SwingConstants.CENTER);
        lblT.setFont(fuenteTitulos);
        JLabel lblMontoTotal = new JLabel("$" + String.format("%.2f", totalVenta), SwingConstants.CENTER);
        lblMontoTotal.setFont(fuenteMontos);
        cajaTotal.add(lblT);
        cajaTotal.add(lblMontoTotal);

        // Caja Pagado
        JPanel cajaPagado = new JPanel(new GridLayout(2, 1));
        cajaPagado.setOpaque(false);
        JLabel lblP = new JLabel("Pagado", SwingConstants.CENTER);
        lblP.setFont(fuenteTitulos);
        lblPagado = new JLabel("$0.00", SwingConstants.CENTER);
        lblPagado.setFont(fuenteMontos);
        cajaPagado.add(lblP);
        cajaPagado.add(lblPagado);

        // Caja Falta
        JPanel cajaFalta = new JPanel(new GridLayout(2, 1));
        cajaFalta.setOpaque(false);
        JLabel lblF = new JLabel("Falta", SwingConstants.CENTER);
        lblF.setFont(fuenteTitulos);
        lblFalta = new JLabel("$" + formatear(totalVenta), SwingConstants.CENTER);
        lblFalta.setFont(fuenteMontos);
        lblFalta.setForeground(Color.RED);
        cajaFalta.add(lblF);
        cajaFalta.add(lblFalta);

        // Caja Cambio
        JPanel cajaCambio = new JPanel(new GridLayout(2, 1));
        cajaCambio.setOpaque(false);
        JLabel lblC = new JLabel("Cambio", SwingConstants.CENTER);
        lblC.setFont(fuenteTitulos);
        lblCambio = new JLabel("$0.00", SwingConstants.CENTER);
        lblCambio.setFont(fuenteMontos);
        lblCambio.setForeground(new Color(220, 53, 69));
        cajaCambio.add(lblC);
        cajaCambio.add(lblCambio);

        headerInfo.add(cajaTotal);
        headerInfo.add(cajaPagado);
        headerInfo.add(cajaFalta);
        headerInfo.add(cajaCambio);
        panelCobroPrincipal.add(headerInfo, BorderLayout.NORTH);

        // --- CUERPO: GRILLA DE PAGOS ---
        JPanel panelPagos = new JPanel(new GridLayout(4, 2, 15, 15));
        panelPagos.setBackground(new Color(245, 245, 245));
        panelPagos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (FormaPago forma : FormaPago.values()) {
            JPanel card = new JPanel(new BorderLayout(5, 5));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            card.setBackground(Color.WHITE);
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel titulo = new JLabel("<html>" + iconoFormaPago(forma) + "&nbsp;&nbsp;" + forma.getNombre() + "</html>");
            titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

            JTextField txt = new JTextField("0.00");
            txt.setHorizontalAlignment(JTextField.RIGHT);
            txt.setFont(new Font("Segoe UI", Font.BOLD, 24));
            txt.setOpaque(false);
            txt.setBorder(null);

            // Listeners originales
            txt.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    calcularTotales();
                }
            });

            txt.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    for (Component c : panelPagos.getComponents()) {
                        if (c instanceof JPanel p) {
                            p.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
                        }
                    }
                    card.setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69), 2));
                    txt.selectAll();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    double valor = obtenerNumero(txt);
                    txt.setText(formatear(valor < 0 ? 0 : valor));
                    calcularTotales();
                }
            });

            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    txt.requestFocusInWindow();
                }
            });

            card.add(titulo, BorderLayout.NORTH);
            card.add(txt, BorderLayout.CENTER);
            camposPago.put(forma, txt);
            panelPagos.add(card);
        }

        panelCobroPrincipal.add(new JScrollPane(panelPagos), BorderLayout.CENTER);

        /* ==========================================================
       2. BOTÓN INFERIOR
       ========================================================== */
        JButton btnConfirmar = new JButton("CONFIRMAR COBRO");
        btnConfirmar.setBackground(new Color(40, 167, 69));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 26));
        btnConfirmar.setPreferredSize(new Dimension(0, 100));
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.addActionListener(e -> confirmar());

        // Agregar componentes al JDialog
        add(panelCobroPrincipal, BorderLayout.CENTER);
        add(btnConfirmar, BorderLayout.SOUTH);

        agregarNavegacionTeclado(panelPagos, btnConfirmar);
    }

    private void calcularTotales() {
        double pagado = 0;

        for (JTextField txt : camposPago.values()) {
            pagado += obtenerNumero(txt);
        }

        double falta = totalVenta - pagado;
        double cambio = Math.max(pagado - totalVenta, 0);

        // Actualizamos los labels del encabezado
        lblPagado.setText("$" + formatear(pagado));
        lblFalta.setText("$" + formatear(Math.max(falta, 0)));
        lblCambio.setText("$" + formatear(cambio));

        // Lógica de colores para Falta
        if (falta <= 0) {
            lblFalta.setForeground(Color.BLACK); // Negro si ya no falta nada
        } else {
            lblFalta.setForeground(Color.RED);   // Rojo mientras falte
        }

        // Lógica de colores para Cambio
        if (cambio > 0) {
            lblCambio.setForeground(new Color(220, 53, 69)); // Rojo si hay cambio
        } else {
            lblCambio.setForeground(Color.BLACK); // Negro si es 0
        }
    }

    private void confirmar() {

        double pagado = 0;

        for (JTextField txt : camposPago.values()) {
            pagado += obtenerNumero(txt);
        }

        if (pagado < totalVenta) {
            JOptionPane.showMessageDialog(this,
                    "El pago no cubre el total");
            return;
        }

        ventaConfirmada = true;
        dispose();
    }

    private String formatear(double valor) {
        return formatoMoneda.format(valor);
    }

    private double obtenerNumero(JTextField txt) {

        if (txt == null) {
            return 0.0;
        }

        String valor = txt.getText();

        if (valor == null || valor.isBlank()) {
            return 0.0;
        }

        try {
            valor = valor
                    .replace("$", "")
                    .replace(",", "")
                    .replace(" ", "")
                    .trim();

            // evitar casos como "." o "-"
            if (valor.equals(".") || valor.equals("-")) {
                return 0.0;
            }

            return Double.parseDouble(valor);

        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String iconoFormaPago(FormaPago forma) {

        switch (forma) {
            case EFECTIVO:
                return "💵";
            case TARJETA:
                return "💳";
            case TRANSFERENCIA:
                return "🔁";
            case VALES:
                return "🎟";
            case CREDITO:
                return "🧾";
            case QR:
                return "📱";
            case MIXTO:
                return "🔀";
            default:
                return "💰";
        }
    }

    private void agregarNavegacionTeclado(JPanel panelPagos, JButton btnConfirmar) {
        Component[] componentes = panelPagos.getComponents();

        for (int i = 0; i < componentes.length; i++) {
            if (componentes[i] instanceof JPanel card) {
                JTextField txt = (JTextField) card.getComponent(1); // asumiendo txt está en el índice 1
                final int index = i;

                txt.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_UP:
                                if (index - 2 >= 0) {
                                    ((JTextField) ((JPanel) componentes[index - 2]).getComponent(1)).requestFocusInWindow();
                                }
                                break;
                            case KeyEvent.VK_DOWN:
                                if (index + 2 < componentes.length) {
                                    ((JTextField) ((JPanel) componentes[index + 2]).getComponent(1)).requestFocusInWindow();
                                } else {
                                    btnConfirmar.requestFocusInWindow();
                                }
                                break;
                            case KeyEvent.VK_LEFT:
                                if (index % 2 == 1) { // columna derecha
                                    ((JTextField) ((JPanel) componentes[index - 1]).getComponent(1)).requestFocusInWindow();
                                }
                                break;
                            case KeyEvent.VK_RIGHT:
                                if (index % 2 == 0 && index + 1 < componentes.length) { // columna izquierda
                                    ((JTextField) ((JPanel) componentes[index + 1]).getComponent(1)).requestFocusInWindow();
                                }
                                break;
                        }
                    }
                });
            }
        }

        // Foco en el botón confirmar desde teclado
        btnConfirmar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    // ir al último campo de pago
                    ((JTextField) ((JPanel) componentes[componentes.length - 1]).getComponent(1)).requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnConfirmar.doClick();
                }
            }
        });
    }
}
