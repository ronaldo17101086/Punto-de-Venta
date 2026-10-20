package tools;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DialogoPago extends JDialog {

    private final BigDecimal totalVenta;
    private final List<?> productos;
    private final EnumMap<FormaPagoDto, JTextField> camposPago = new EnumMap<>(FormaPagoDto.class);
    private JLabel lblPagado, lblFalta, lblCambio;
    private JButton btnConfirmar;
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private boolean ventaConfirmada = false;

    // --- VARIABLES DE CONTROL DE CRÉDITO Y CLIENTE ---
    private boolean esPublicoGeneral = true;
    private BigDecimal creditoDisponible = BigDecimal.ZERO;
    private JPanel cardCreditoRef; // Referencia para modificar su estado visual

    // --- COLORES ---
    private final Color COLOR_FONDO = new Color(245, 247, 251);
    private final Color COLOR_LATERAL = new Color(52, 58, 64);
    private final Color COLOR_ACCENTO = new Color(40, 167, 69);
    private final Color COLOR_PELIGRO = new Color(220, 53, 69);
    private final Color COLOR_PRIMARIO = new Color(0, 123, 255);
    private final Color COLOR_BORDE = new Color(225, 230, 239);
    private final Color COLOR_BLOQUEADO = new Color(180, 185, 190);

    public DialogoPago(Window parent, BigDecimal total, List<?> productos) {
        super(parent);
        this.totalVenta = total;
        this.productos = productos;
        setModal(true);
        setUndecorated(true);
        setSize(1100, 750);
        setLocationRelativeTo(parent);
        setShape(new RoundRectangle2D.Double(0, 0, 1100, 750, 30, 30));
        initUI();
    }

    public void setClienteActual(boolean esPublicoGeneral, BigDecimal creditoDisponible) {
        this.esPublicoGeneral = esPublicoGeneral;
        this.creditoDisponible = creditoDisponible != null
                ? creditoDisponible.max(BigDecimal.ZERO)
                : BigDecimal.ZERO;

        boolean tieneCreditoValido = !this.esPublicoGeneral
                && this.creditoDisponible.compareTo(BigDecimal.ZERO) > 0;

        JTextField txtCredito = camposPago.get(FormaPagoDto.CREDITO);

        if (txtCredito != null) {
            // Habilitamos o deshabilitamos la edición de acuerdo a si el cliente tiene crédito
            txtCredito.setEditable(tieneCreditoValido);
            txtCredito.setText("0");
        }

        if (cardCreditoRef != null) {
            cardCreditoRef.setBackground(
                    tieneCreditoValido ? Color.WHITE : new Color(240, 243, 246)
            );
            cardCreditoRef.setOpaque(true);
            cardCreditoRef.repaint();
        }

        calcularTotales();
    }

    private void initUI() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(COLOR_FONDO);

        // --- BARRA LATERAL ---
        JPanel sideBar = new JPanel(new BorderLayout());
        sideBar.setPreferredSize(new Dimension(100, 0));
        sideBar.setBackground(COLOR_LATERAL);
        JButton btnAtras = new JButton("<html><center><font color='white' size='6'>←</font><br><font color='white'>ATRÁS</font></center></html>");
        btnAtras.setContentAreaFilled(false);
        btnAtras.setBorder(new EmptyBorder(30, 0, 0, 0));
        btnAtras.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAtras.addActionListener(e -> dispose());
        sideBar.add(btnAtras, BorderLayout.NORTH);

        // --- PANEL PRINCIPAL ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);

        // Header con indicadores
        JPanel header = new JPanel(new GridLayout(1, 4, 30, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE),
                new EmptyBorder(40, 50, 40, 50)));

        crearIndicadorEstatico(header, "TOTAL A COBRAR", totalVenta, Color.BLACK);
        lblPagado = crearIndicadorVariable(header, "RECIBIDO", COLOR_PRIMARIO);
        lblFalta = crearIndicadorVariable(header, "PENDIENTE", COLOR_PELIGRO);
        lblCambio = crearIndicadorVariable(header, "CAMBIO", COLOR_ACCENTO);

        // Grid de campos
        JPanel gridPagos = new JPanel(new GridLayout(4, 2, 20, 20));
        gridPagos.setOpaque(false);
        gridPagos.setBorder(new EmptyBorder(30, 50, 30, 50));

        for (FormaPagoDto forma : FormaPagoDto.values()) {
            JPanel card = crearCardPago(forma);
            gridPagos.add(card);

            // Guardar referencia específica de la tarjeta de crédito
            if (forma == FormaPagoDto.CREDITO) {
                cardCreditoRef = card;
            }
        }

        // Botón Confirmar
        btnConfirmar = new JButton("CONFIRMAR Y FINALIZAR VENTA");
        btnConfirmar.setBackground(COLOR_BLOQUEADO);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnConfirmar.setPreferredSize(new Dimension(0, 90));
        btnConfirmar.setBorder(null);
        btnConfirmar.setEnabled(false);
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.addActionListener(e -> {
            // Validación final antes de confirmar: si usa crédito, validar contra límite disponible
            JTextField txtCred = camposPago.get(FormaPagoDto.CREDITO);
            if (txtCred != null) {
                BigDecimal montoCredito = obtenerNumero(txtCred);
                if (montoCredito.compareTo(BigDecimal.ZERO) > 0) {
                    if (esPublicoGeneral) {
                        JOptionPane.showMessageDialog(this, "El Público General no puede comprar a crédito.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (montoCredito.compareTo(creditoDisponible) > 0) {
                        JOptionPane.showMessageDialog(this, "El monto a crédito excede el disponible ($" + creditoDisponible + ").", "Crédito Insuficiente", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            ventaConfirmada = true;
            dispose();
        });

        mainContent.add(header, BorderLayout.NORTH);
        mainContent.add(gridPagos, BorderLayout.CENTER);
        mainContent.add(btnConfirmar, BorderLayout.SOUTH);

        container.add(sideBar, BorderLayout.WEST);
        container.add(mainContent, BorderLayout.CENTER);
        add(container);

        calcularTotales();
    }

    public java.util.Map<String, BigDecimal> obtenerDesglosePagos() {
        java.util.Map<String, BigDecimal> desglose = new java.util.HashMap<>();
        for (Map.Entry<FormaPagoDto, JTextField> entry : camposPago.entrySet()) {
            desglose.put(entry.getKey().name(), obtenerNumero(entry.getValue()));
        }
        return desglose;
    }

    public boolean isEsCredito() {
        JTextField txtCredito = camposPago.get(FormaPagoDto.CREDITO);
        if (txtCredito != null) {
            BigDecimal montoCredito = obtenerNumero(txtCredito);
            return montoCredito.compareTo(BigDecimal.ZERO) > 0;
        }
        return false;
    }

    private JPanel crearCardPago(FormaPagoDto forma) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel lblIcono = new JLabel(getIcono(forma));
        lblIcono.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 25));

        JLabel lblNombre = new JLabel(forma.name().replace("_", " "));
        lblNombre.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lblNombre.setForeground(Color.GRAY);

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlLeft.setOpaque(false);
        pnlLeft.add(lblIcono);
        pnlLeft.add(lblNombre);

        JTextField txt = new JTextField("0");
        txt.setFont(new Font("Segoe UI", Font.BOLD, 28));
        txt.setBorder(null);
        txt.setHorizontalAlignment(JTextField.RIGHT);

        // Validación de teclas: permite números y puntos en todos los campos (incluyendo crédito para pagos mixtos)
        txt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (forma == FormaPagoDto.CREDITO && esPublicoGeneral) {
                    e.consume();
                    return;
                }

                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != '.') {
                    e.consume();
                }
                if (c == '.' && txt.getText().contains(".")) {
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                calcularTotales();
            }
        });

        // Al hacer foco, selecciona todo el texto para evitar que se encimen los números (ej: "15.000")
        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (forma == FormaPagoDto.CREDITO && esPublicoGeneral) {
                    JOptionPane.showMessageDialog(DialogoPago.this,
                            "El Público General no cuenta con opciones de crédito.",
                            "Restricción", JOptionPane.WARNING_MESSAGE);
                    txt.transferFocus();
                    return;
                }
                txt.selectAll();
            }
        });

        camposPago.put(forma, txt);
        card.add(pnlLeft, BorderLayout.WEST);
        card.add(txt, BorderLayout.CENTER);
        return card;
    }

    private void calcularTotales() {
        BigDecimal pagado = BigDecimal.ZERO;
        for (JTextField txt : camposPago.values()) {
            pagado = pagado.add(obtenerNumero(txt));
        }

        BigDecimal falta = totalVenta.subtract(pagado).max(BigDecimal.ZERO);
        BigDecimal cambio = pagado.subtract(totalVenta).max(BigDecimal.ZERO);

        lblPagado.setText(formatoMoneda.format(pagado));
        lblFalta.setText(formatoMoneda.format(falta));
        lblCambio.setText(formatoMoneda.format(cambio));

        boolean puedeFinalizar = pagado.compareTo(totalVenta) >= 0 && pagado.compareTo(BigDecimal.ZERO) > 0;
        btnConfirmar.setEnabled(puedeFinalizar);
        btnConfirmar.setBackground(puedeFinalizar ? COLOR_ACCENTO : COLOR_BLOQUEADO);
    }

    private String getIcono(FormaPagoDto f) {
        return switch (f) {
            case EFECTIVO -> "💵";
            case TARJETA -> "💳";
            case TRANSFERENCIA -> "🏦";
            case QR -> "📱";
            case VALES -> "🎟️";
            case CREDITO -> "⏳";
            case CHEQUE -> "✍️";
            case MIXTO -> "🔄";
            default -> "💰";
        };
    }

    private BigDecimal obtenerNumero(JTextField txt) {
        try {
            String s = txt.getText().replaceAll("[^0-9.]", "");
            return s.isEmpty() ? BigDecimal.ZERO : new BigDecimal(s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void crearIndicadorEstatico(JPanel p, String t, BigDecimal v, Color c) {
        JPanel s = new JPanel(new GridLayout(2, 1));
        s.setOpaque(false);
        JLabel l1 = new JLabel(t);
        l1.setFont(new Font("Segoe UI", 1, 12));
        l1.setForeground(Color.GRAY);
        JLabel l2 = new JLabel(formatoMoneda.format(v));
        l2.setFont(new Font("Segoe UI", 1, 38));
        l2.setForeground(c);
        s.add(l1);
        s.add(l2);
        p.add(s);
    }

    private JLabel crearIndicadorVariable(JPanel p, String t, Color c) {
        JPanel s = new JPanel(new GridLayout(2, 1));
        s.setOpaque(false);
        JLabel l1 = new JLabel(t);
        l1.setFont(new Font("Segoe UI", 1, 12));
        l1.setForeground(Color.GRAY);
        JLabel v = new JLabel("$0.00");
        v.setFont(new Font("Segoe UI", 1, 30));
        v.setForeground(c);
        s.add(l1);
        s.add(v);
        p.add(s);
        return v;
    }

    public boolean isVentaConfirmada() {
        return ventaConfirmada;
    }

    public BigDecimal getCambio() {
        BigDecimal pagado = BigDecimal.ZERO;
        for (JTextField txt : camposPago.values()) {
            pagado = pagado.add(obtenerNumero(txt));
        }
        return pagado.subtract(totalVenta).max(BigDecimal.ZERO);
    }

    // =========================================================================
    // DIÁLOGO DE ÉXITO PREMIUM
    // =========================================================================
    public static class DialogoExitoVenta extends JDialog {

        public DialogoExitoVenta(Window parent, String folio, BigDecimal cambio) {
            super(parent);
            setModal(true);
            setUndecorated(true);
            setSize(500, 700);
            setLocationRelativeTo(parent);
            setShape(new RoundRectangle2D.Double(0, 0, 500, 700, 40, 40));

            JPanel main = new JPanel(new BorderLayout());
            main.setBackground(new Color(242, 244, 248));
            main.setBorder(new EmptyBorder(40, 40, 40, 40));

            JPanel checkPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(40, 167, 69, 30));
                    g2.fillOval((getWidth() - 100) / 2, 10, 100, 100);
                    g2.setColor(new Color(40, 167, 69));
                    g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int x = (getWidth() - 100) / 2;
                    Path2D p = new Path2D.Double();
                    p.moveTo(x + 25, 60);
                    p.lineTo(x + 45, 80);
                    p.lineTo(x + 75, 40);
                    g2.draw(p);
                    g2.dispose();
                }
            };
            checkPanel.setPreferredSize(new Dimension(120, 120));
            checkPanel.setOpaque(false);

            JLabel lblOk = new JLabel("¡VENTA EXITOSA!", SwingConstants.CENTER);
            lblOk.setFont(new Font("Segoe UI", Font.BOLD, 32));
            lblOk.setForeground(new Color(40, 167, 69));

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 225, 230), 1, true),
                    new EmptyBorder(40, 30, 40, 30)));

            JLabel f1 = new JLabel("FOLIO DE VENTA");
            f1.setAlignmentX(0.5f);
            f1.setForeground(Color.LIGHT_GRAY);
            JLabel f2 = new JLabel("#" + folio);
            f2.setAlignmentX(0.5f);
            f2.setFont(new Font("Segoe UI", 1, 22));

            JLabel c1 = new JLabel("CAMBIO A ENTREGAR");
            c1.setAlignmentX(0.5f);
            c1.setForeground(Color.LIGHT_GRAY);
            JLabel c2 = new JLabel(NumberFormat.getCurrencyInstance(new Locale("es", "MX")).format(cambio));
            c2.setAlignmentX(0.5f);
            c2.setFont(new Font("Segoe UI Black", 1, 65));

            card.add(f1);
            card.add(Box.createVerticalStrut(10));
            card.add(f2);
            card.add(Box.createVerticalStrut(30));
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(300, 1));
            card.add(sep);
            card.add(Box.createVerticalStrut(30));
            card.add(c1);
            card.add(Box.createVerticalStrut(10));
            card.add(c2);

            JButton btn = new JButton("CONTINUAR A SIGUIENTE VENTA");
            btn.setBackground(new Color(30, 35, 40));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(0, 70));
            btn.addActionListener(e -> dispose());

            JPanel top = new JPanel(new BorderLayout(0, 15));
            top.setOpaque(false);
            top.add(checkPanel, BorderLayout.CENTER);
            top.add(lblOk, BorderLayout.SOUTH);

            main.add(top, BorderLayout.NORTH);
            main.add(card, BorderLayout.CENTER);
            main.add(btn, BorderLayout.SOUTH);
            add(main);
        }
    }
}