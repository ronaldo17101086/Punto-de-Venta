package tools;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

public class DialogoPago extends JDialog {

    private BigDecimal totalVenta;
    private List<?> productos;
    private EnumMap<FormaPagoDto, JTextField> camposPago = new EnumMap<>(FormaPagoDto.class);
    private JLabel lblPagado, lblFalta, lblCambio;
    private final NumberFormat formatoMoneda = NumberFormat.getNumberInstance(new Locale("es", "MX"));
    private boolean ventaConfirmada = false;

    // --- COLORES GENERALES ---
    private final Color COLOR_FONDO = new Color(245, 247, 251);
    private final Color COLOR_LATERAL = new Color(52, 58, 64);
    private final Color COLOR_ACCENTO = new Color(40, 167, 69);
    private final Color COLOR_PELIGRO = new Color(220, 53, 69);
    private final Color COLOR_PRIMARIO = new Color(0, 123, 255);
    private final Color COLOR_CARD = Color.WHITE;
    private final Color COLOR_BORDE = new Color(225, 230, 239);

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

    private void initUI() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(COLOR_FONDO);

        /* --- 1. BARRA LATERAL --- */
        JPanel sideBar = new JPanel(new BorderLayout());
        sideBar.setPreferredSize(new Dimension(100, 0));
        sideBar.setBackground(COLOR_LATERAL);

        JButton btnAtras = new JButton("<html><center><font color='white' size='6'>←</font><br><font color='white'>ATRÁS</font></center></html>");
        btnAtras.setContentAreaFilled(false);
        btnAtras.setBorder(new EmptyBorder(30, 0, 0, 0));
        btnAtras.setFocusPainted(false);
        btnAtras.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAtras.addActionListener(e -> dispose());
        sideBar.add(btnAtras, BorderLayout.NORTH);

        /* --- 2. PANEL PRINCIPAL --- */
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);

        JPanel header = new JPanel(new GridLayout(1, 4, 30, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE),
                new EmptyBorder(40, 50, 40, 50)));

        JPanel pTotal = new JPanel(new GridLayout(2, 1));
        pTotal.setOpaque(false);
        JLabel t1 = new JLabel("TOTAL A COBRAR");
        t1.setForeground(Color.GRAY);
        JLabel t2 = new JLabel("$" + totalVenta.setScale(2, java.math.RoundingMode.HALF_UP).toString());
        t2.setFont(new Font("Segoe UI", Font.BOLD, 42));
        pTotal.add(t1);
        pTotal.add(t2);

        header.add(pTotal);
        lblPagado = crearIndicador(header, "RECIBIDO", "$0.00", COLOR_PRIMARIO);
// Opción A: Si tu método formatear ya acepta BigDecimal
        lblFalta = crearIndicador(header, "PENDIENTE", "$" + formatear(totalVenta), COLOR_PELIGRO);

// Opción B: Si quieres formatearlo directamente con 2 decimales sin depender de otro método
        String totalTexto = totalVenta.setScale(2, java.math.RoundingMode.HALF_UP).toString();
        lblFalta = crearIndicador(header, "PENDIENTE", "$" + totalTexto, COLOR_PELIGRO);
        lblCambio = crearIndicador(header, "CAMBIO", "$0.00", COLOR_ACCENTO);

        JPanel gridPagos = new JPanel(new GridLayout(3, 2, 25, 25));
        gridPagos.setOpaque(false);
        gridPagos.setBorder(new EmptyBorder(40, 50, 40, 50));

        for (FormaPagoDto forma : FormaPagoDto.values()) {
            gridPagos.add(crearCardPago(forma));
        }

        JButton btnConfirmar = new JButton("CONFIRMAR Y FINALIZAR VENTA");
        btnConfirmar.setBackground(COLOR_ACCENTO);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnConfirmar.setPreferredSize(new Dimension(0, 90));
        btnConfirmar.setBorder(null);
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.addActionListener(e -> confirmar());

        mainContent.add(header, BorderLayout.NORTH);
        mainContent.add(gridPagos, BorderLayout.CENTER);
        mainContent.add(btnConfirmar, BorderLayout.SOUTH);

        container.add(sideBar, BorderLayout.WEST);
        container.add(mainContent, BorderLayout.CENTER);
        add(container);
    }

    private JLabel crearIndicador(JPanel parent, String titulo, String valor, Color colorMonto) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(Color.GRAY);
        JLabel v = new JLabel(valor);
        v.setFont(new Font("Segoe UI", Font.BOLD, 30));
        v.setForeground(colorMonto);
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        parent.add(p);
        return v;
    }

    private JPanel crearCardPago(FormaPagoDto forma) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(20, 25, 20, 25)));

        JLabel icon = new JLabel(iconoFormaPago(forma));
        icon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 40));

        JTextField txt = new JTextField("0.00");
        txt.setFont(new Font("Segoe UI", Font.BOLD, 32));
        txt.setBorder(null);
        txt.setHorizontalAlignment(JTextField.RIGHT);
        camposPago.put(forma, txt);

        card.add(icon, BorderLayout.WEST);
        card.add(txt, BorderLayout.CENTER);

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setText(formatear(obtenerNumero(txt)));
                calcularTotales();
            }
        });
        txt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calcularTotales();
            }
        });

        return card;
    }

    private void calcularTotales() {
        // 1. Acumulamos el pago total (Efectivo + Tarjeta + etc.)
        java.math.BigDecimal pagado = java.math.BigDecimal.ZERO;
        for (JTextField txt : camposPago.values()) {
            // Sumamos lo que devuelve obtenerNumero() que ya es BigDecimal
            pagado = pagado.add(obtenerNumero(txt));
        }

        // 2. Calculamos cuánto falta (Total - Pagado)
        java.math.BigDecimal falta = totalVenta.subtract(pagado);

        // 3. Calculamos el cambio (Pagado - Total)
        java.math.BigDecimal cambio = pagado.subtract(totalVenta);

        // 4. Aplicamos la lógica de "Mínimo Cero" usando compareTo
        // Si falta es menor a 0, ponemos ZERO
        java.math.BigDecimal faltaVisual = (falta.compareTo(java.math.BigDecimal.ZERO) < 0)
                ? java.math.BigDecimal.ZERO : falta;

        // Si el cambio es menor a 0, ponemos ZERO
        java.math.BigDecimal cambioVisual = (cambio.compareTo(java.math.BigDecimal.ZERO) < 0)
                ? java.math.BigDecimal.ZERO : cambio;

        // 5. Actualizamos los Labels con el formato correcto
        lblPagado.setText("$" + formatear(pagado));
        lblFalta.setText("$" + formatear(faltaVisual));
        lblCambio.setText("$" + formatear(cambioVisual));

        // Tip extra: Si quieres que el botón de "Cobrar" solo se active cuando ya completaron el pago:
        // btnCobrar.setEnabled(faltaVisual.compareTo(java.math.BigDecimal.ZERO) == 0);
    }

    private void confirmar() {
        // 1. Acumulador de lo pagado en BigDecimal
        java.math.BigDecimal totalPagado = java.math.BigDecimal.ZERO;

        for (JTextField txt : camposPago.values()) {
            // Sumamos cada campo (ya convertido a BigDecimal por obtenerNumero)
            totalPagado = totalPagado.add(obtenerNumero(txt));
        }

        // 2. Validamos si el pago es insuficiente
        // compareTo devuelve -1 si totalPagado es menor que totalVenta
        if (totalPagado.compareTo(totalVenta) < 0) {
            JOptionPane.showMessageDialog(this,
                    "Monto insuficiente. Faltan: $" + formatear(totalVenta.subtract(totalPagado)));
            return;
        }

        // 3. Si pasó la validación, procedemos
        ventaConfirmada = true;
        dispose();
    }

    private String formatear(BigDecimal val) {
        return formatoMoneda.format(val);
    }

    private java.math.BigDecimal obtenerNumero(JTextField txt) {
        try {
            String texto = txt.getText().replaceAll("[^0-9.]", "");
            if (texto.isEmpty()) {
                return java.math.BigDecimal.ZERO;
            }
            return new java.math.BigDecimal(texto);
        } catch (Exception e) {
            return java.math.BigDecimal.ZERO;
        }
    }

    private String iconoFormaPago(FormaPagoDto forma) {
        return switch (forma) {
            case EFECTIVO ->
                "💵";
            case TARJETA ->
                "💳";
            case TRANSFERENCIA ->
                "🏦";
            case VALES ->
                "🎟️";
            case CREDITO ->
                "⏳";
            case QR ->
                "📱";
            default ->
                "💰";
        };
    }

    public boolean isVentaConfirmada() {
        return ventaConfirmada;
    }

    public java.math.BigDecimal getCambio() {
        java.math.BigDecimal pagado = java.math.BigDecimal.ZERO;
        for (JTextField txt : camposPago.values()) {
            pagado = pagado.add(obtenerNumero(txt));
        }
        java.math.BigDecimal diferencia = pagado.subtract(totalVenta);
        if (diferencia.compareTo(java.math.BigDecimal.ZERO) < 0) {
            return java.math.BigDecimal.ZERO;
        }
        return diferencia;
    }

    // =========================================================================
    // DISEÑO PREMIUM: DIÁLOGO DE ÉXITO CORREGIDO
    // =========================================================================
    public static class DialogoExitoVenta extends JDialog {

        private final Color COLOR_EXITO = new Color(40, 167, 69);
        private final Color COLOR_SUBTITULO = new Color(130, 140, 150);
        private final Color COLOR_VALOR = new Color(45, 50, 55);
        private final Color COLOR_FONDO_VENTANA = new Color(242, 244, 248); // Fondo gris muy suave para que el blanco resalte
        private final Color COLOR_BOTON = new Color(30, 35, 40);

        public DialogoExitoVenta(Window parent, String folio, BigDecimal cambio) {
            super(parent);
            setModal(true);
            setUndecorated(true);
            setSize(500, 700);
            setLocationRelativeTo(parent);
            setShape(new RoundRectangle2D.Double(0, 0, 500, 700, 40, 40));

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(COLOR_FONDO_VENTANA);
            mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

            // --- CABECERA CON CHECK DIBUJADO (NO PIXELADO) ---
            JPanel topPanel = new JPanel(new BorderLayout(0, 15));
            topPanel.setOpaque(false);

            // Panel personalizado para dibujar el Check perfecto
            JPanel checkContainer = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_EXITO);

                    int size = 80;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;

                    // Dibujar círculo de fondo sutil
                    g2.setColor(new Color(40, 167, 69, 30));
                    g2.fillOval(x - 10, y - 10, size + 20, size + 20);

                    // Dibujar la palomita
                    g2.setColor(COLOR_EXITO);
                    g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D path = new Path2D.Double();
                    path.moveTo(x + size * 0.2, y + size * 0.5);
                    path.lineTo(x + size * 0.45, y + size * 0.75);
                    path.lineTo(x + size * 0.85, y + size * 0.25);
                    g2.draw(path);
                    g2.dispose();
                }
            };
            checkContainer.setPreferredSize(new Dimension(120, 120));
            checkContainer.setOpaque(false);

            JLabel lblMsg = new JLabel("¡VENTA EXITOSA!", SwingConstants.CENTER);
            lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 32));
            lblMsg.setForeground(COLOR_EXITO);

            topPanel.add(checkContainer, BorderLayout.CENTER);
            topPanel.add(lblMsg, BorderLayout.SOUTH);

            // --- ÁREA DE RECIBO (TARJETA BLANCA SOBRE FONDO GRIS) ---
            JPanel receiptCard = new JPanel();
            receiptCard.setLayout(new BoxLayout(receiptCard, BoxLayout.Y_AXIS));
            receiptCard.setBackground(Color.WHITE);
            receiptCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 225, 230), 1, true),
                    new EmptyBorder(40, 30, 40, 30)
            ));

            JLabel lblF1 = new JLabel("FOLIO DE VENTA");
            lblF1.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblF1.setForeground(COLOR_SUBTITULO);
            lblF1.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblF2 = new JLabel("#" + folio);
            lblF2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblF2.setForeground(COLOR_VALOR);
            lblF2.setAlignmentX(Component.CENTER_ALIGNMENT);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(235, 235, 235));
            sep.setMaximumSize(new Dimension(320, 1));

            JLabel lblC1 = new JLabel("CAMBIO A ENTREGAR");
            lblC1.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblC1.setForeground(COLOR_SUBTITULO);
            lblC1.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblC2 = new JLabel("$" + String.format("%.2f", cambio));
            lblC2.setFont(new Font("Segoe UI", Font.BOLD, 70));
            lblC2.setForeground(COLOR_VALOR);
            lblC2.setAlignmentX(Component.CENTER_ALIGNMENT);

            receiptCard.add(lblF1);
            receiptCard.add(Box.createVerticalStrut(8));
            receiptCard.add(lblF2);
            receiptCard.add(Box.createVerticalStrut(30));
            receiptCard.add(sep);
            receiptCard.add(Box.createVerticalStrut(30));
            receiptCard.add(lblC1);
            receiptCard.add(Box.createVerticalStrut(8));
            receiptCard.add(lblC2);

            // --- BOTÓN ACCIÓN ---
            JButton btnOk = new JButton("CONTINUAR A SIGUIENTE VENTA");
            btnOk.setBackground(COLOR_BOTON);
            btnOk.setForeground(Color.WHITE);
            btnOk.setFont(new Font("Segoe UI", Font.BOLD, 15));
            btnOk.setFocusPainted(false);
            btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOk.setBorder(null);
            btnOk.setPreferredSize(new Dimension(0, 70));
            btnOk.addActionListener(e -> dispose());

            // --- ESTRUCTURA FINAL ---
            JPanel content = new JPanel(new BorderLayout(0, 30));
            content.setOpaque(false);
            content.add(topPanel, BorderLayout.NORTH);
            content.add(receiptCard, BorderLayout.CENTER);
            content.add(btnOk, BorderLayout.SOUTH);

            mainPanel.add(content, BorderLayout.CENTER);
            add(mainPanel);
        }
    }
}
