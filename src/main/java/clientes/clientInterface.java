package clientes;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.chancuellarpuntodeventa.services.dtos.ClienteDTO;
import com.mycompany.chancuellarpuntodeventa.services.querys.ClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

@Component
public class clientInterface extends JPanel {

    @Autowired
    private ClienteServicio clienteServicio;

    private JTextField txtBuscar;
    private JTable tableClientes;
    private DefaultTableModel tableModel;

    private JLabel lblTotalClientesVal;
    private JLabel lblDeudaTotalVal;
    private JLabel lblCreditoGlobalVal;

    public clientInterface() {
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(235, 238, 242));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== ENCABEZADO Y TÍTULO =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Gestión de Clientes y Créditos (Fíados)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(28, 35, 49));
        topPanel.add(lblTitulo, BorderLayout.WEST);

        // ===== PANEL DE ACCIONES Y BÚSQUEDA =====
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        txtBuscar = new JTextField(16);
        txtBuscar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar cliente...");
        txtBuscar.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        txtBuscar.addActionListener(e -> filtrarClientesTabla());
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarClientesTabla();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarClientesTabla();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarClientesTabla();
            }
        });
        actionPanel.add(txtBuscar);

        // ===== BOTÓN ACTUALIZAR =====
        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.setBackground(new Color(108, 117, 125));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        btnActualizar.addActionListener(e -> cargarDatosTabla());
        actionPanel.add(btnActualizar);

        JButton btnAbono = new JButton("💳 Abonar / Devolver");
        btnAbono.setBackground(new Color(23, 162, 184));
        btnAbono.setForeground(Color.WHITE);
        btnAbono.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAbono.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAbono.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        btnAbono.addActionListener(e -> abrirModalAbono());
        actionPanel.add(btnAbono);

        JButton btnEditar = new JButton("✏️ Editar");
        btnEditar.setBackground(new Color(255, 193, 7));
        btnEditar.setForeground(new Color(40, 40, 40));
        btnEditar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEditar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEditar.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        btnEditar.addActionListener(e -> abrirModalEditarCliente());
        actionPanel.add(btnEditar);

        JButton btnEliminar = new JButton("🗑️ Eliminar");
        btnEliminar.setBackground(new Color(220, 53, 69));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminar.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        btnEliminar.addActionListener(e -> eliminarClienteSeleccionado());
        actionPanel.add(btnEliminar);

        JButton btnNuevo = new JButton("+ Nuevo Cliente");
        btnNuevo.setBackground(new Color(32, 201, 151));
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNuevo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNuevo.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        btnNuevo.addActionListener(e -> abrirModalNuevoCliente());
        actionPanel.add(btnNuevo);

        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ===== PANEL CENTRAL (TARJETAS DE RESUMEN + TABLA) =====
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 85));

        lblTotalClientesVal = new JLabel("0");
        lblDeudaTotalVal = new JLabel("$0.00");
        lblCreditoGlobalVal = new JLabel("$0.00");

        statsPanel.add(createMiniStatCardCustom("Total Clientes", lblTotalClientesVal, new Color(0, 153, 255)));
        statsPanel.add(createMiniStatCardCustom("Deuda Total en Tienda", lblDeudaTotalVal, new Color(220, 53, 69)));
        statsPanel.add(createMiniStatCardCustom("Crédito Global Disp.", lblCreditoGlobalVal, new Color(40, 167, 69)));

        centerPanel.add(statsPanel, BorderLayout.NORTH);

        // ===== TABLA DE CLIENTES CON LA NUEVA COLUMNA DE DETALLE =====
        String[] columnas = {
            "ID",
            "Nombre",
            "Teléfono",
            "Límite Crédito",
            "Deuda Actual (Debe)",
            "Crédito Disponible",
            "PZAS",
            "Detalle de Productos"
        };

        tableModel = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // ===== TABLA DE CLIENTES =====
        // ... (tu código actual de definición de tabla) ...
        tableClientes = new JTable(tableModel);
        tableClientes.setRowHeight(35);
        // ... (tus estilos actuales) ...

        // --- AGREGA ESTE BLOQUE AQUÍ ---
        tableClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { // Doble clic
                    int fila = tableClientes.getSelectedRow();
                    if (fila != -1) {
                        String nombreCliente = (String) tableModel.getValueAt(fila, 1);
                        String detalleCompleto = (String) tableModel.getValueAt(fila, 7);
                        mostrarDialogoDetalleEnvases(nombreCliente, detalleCompleto);
                    }
                }
            }
        });
        // ---------------------------------

        JScrollPane scrollPane = new JScrollPane(tableClientes);
        // ...
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        cargarDatosTabla();
    }

    // ===== DIÁLOGO FLOTANTE PARA VER DETALLE COMPLETO DE ENVASES =====
    private void mostrarDialogoDetalleEnvases(String nombreCliente, String detalle) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Desglose de Envases y Artículos - " + nombreCliente, true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel("Artículos prestados pendientes de devolución:");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeader.setForeground(new Color(30, 41, 59));

        JTextArea txtArea = new JTextArea(detalle.replace(", ", "\n• "));
        if (!detalle.equals("Sin productos prestados")) {
            txtArea.setText("• " + txtArea.getText());
        }
        txtArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtArea.setEditable(false);
        txtArea.setLineWrap(true);
        txtArea.setWrapStyleWord(true);
        txtArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(txtArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBackground(new Color(108, 117, 125));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar.setPreferredSize(new Dimension(120, 38));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        btnCerrar.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnCerrar);

        panel.add(lblHeader, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JPanel createMiniStatCardCustom(String title, JLabel lblValue, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(Color.GRAY);

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(color);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    public void cargarDatosTabla() {
        tableModel.setRowCount(0);
        List<ClienteDTO> clientes = clienteServicio.obtenerTodosLosClientes();

        BigDecimal deudaTotalTienda = BigDecimal.ZERO;
        BigDecimal creditoGlobalDisp = BigDecimal.ZERO;

        for (ClienteDTO c : clientes) {
            BigDecimal limite = c.getLimiteCredito() != null ? c.getLimiteCredito() : BigDecimal.ZERO;
            BigDecimal deuda = c.getDeudaActual() != null ? c.getDeudaActual() : BigDecimal.ZERO;
            BigDecimal disponible = limite.subtract(deuda);

            int envasesDebe = c.getEnvasesPendientes() != null ? c.getEnvasesPendientes() : 0;
            String detalleProd = (c.getDetalleProductos() != null && !c.getDetalleProductos().isEmpty())
                    ? c.getDetalleProductos()
                    : "Sin productos prestados";

            tableModel.addRow(new Object[]{
                c.getId(),
                c.getNombre(),
                c.getTelefono() != null ? c.getTelefono() : "N/A",
                "$" + limite,
                "$" + deuda,
                "$" + disponible,
                envasesDebe + " pzas",
                detalleProd
            });

            deudaTotalTienda = deudaTotalTienda.add(deuda);
            creditoGlobalDisp = creditoGlobalDisp.add(disponible);
        }

        lblTotalClientesVal.setText(String.valueOf(clientes.size()));
        lblDeudaTotalVal.setText("$" + deudaTotalTienda);
        lblCreditoGlobalVal.setText("$" + creditoGlobalDisp);
    }

    private void filtrarClientesTabla() {
        tableModel.setRowCount(0);
        List<ClienteDTO> clientes = clienteServicio.buscarClientes(txtBuscar.getText());

        BigDecimal deudaTotalTienda = BigDecimal.ZERO;
        BigDecimal creditoGlobalDisp = BigDecimal.ZERO;

        for (ClienteDTO c : clientes) {
            BigDecimal limite = c.getLimiteCredito() != null ? c.getLimiteCredito() : BigDecimal.ZERO;
            BigDecimal deuda = c.getDeudaActual() != null ? c.getDeudaActual() : BigDecimal.ZERO;
            BigDecimal disponible = limite.subtract(deuda);

            int envasesDebe = c.getEnvasesPendientes() != null ? c.getEnvasesPendientes() : 0;
            String detalleProd = (c.getDetalleProductos() != null && !c.getDetalleProductos().isEmpty())
                    ? c.getDetalleProductos()
                    : "Sin productos prestados";

            tableModel.addRow(new Object[]{
                c.getId(),
                c.getNombre(),
                c.getTelefono() != null ? c.getTelefono() : "N/A",
                "$" + limite,
                "$" + deuda,
                "$" + disponible,
                envasesDebe + " pzas",
                detalleProd
            });

            deudaTotalTienda = deudaTotalTienda.add(deuda);
            creditoGlobalDisp = creditoGlobalDisp.add(disponible);
        }

        lblTotalClientesVal.setText(String.valueOf(clientes.size()));
        lblDeudaTotalVal.setText("$" + deudaTotalTienda);
        lblCreditoGlobalVal.setText("$" + creditoGlobalDisp);
    }

    // ==========================================
    // MÉTODO AUXILIAR DE VALIDACIÓN DE CAMPOS
    // ==========================================
    private boolean validarCamposCliente(JDialog dialog, String nombre, String telefono, String limiteCreditoStr) {
        // 1. Validar Nombre Requerido
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "El Nombre / Razón Social es obligatorio.", "Campo Requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 2. Validar Teléfono (Opcional o de 10 dígitos)
        if (!telefono.isEmpty()) {
            if (!telefono.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(dialog, "El número de teléfono debe contener exactamente 10 dígitos numéricos.", "Teléfono Inválido", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        // 3. Validar Límite de Crédito Requerido y Válido
        if (limiteCreditoStr.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "El Límite de Crédito es obligatorio.", "Campo Requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            BigDecimal limite = new BigDecimal(limiteCreditoStr);
            if (limite.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(dialog, "El límite de crédito no puede ser negativo.", "Monto Inválido", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Ingrese un número válido para el límite de crédito.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    // ==========================================
    // MODAL: REGISTRAR NUEVO CLIENTE
    // ==========================================
    private void abrirModalNuevoCliente() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Registrar Nuevo Cliente", true);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 0));

        JPanel panelPrincipal = new JPanel(new BorderLayout(16, 16));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panelPrincipal.setBackground(Color.WHITE);

        // --- ENCABEZADO ---
        JLabel lblTitulo = new JLabel("Información del Cliente");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(30, 41, 59));

        // --- FORMULARIO CENTRAL ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Color colorLabel = new Color(71, 85, 105);

        JTextField txtNombre = new JTextField();
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNombre.setPreferredSize(new Dimension(0, 38));
        txtNombre.putClientProperty("JTextField.placeholderText", "Ej. Ronaldo Isaias Murillo Aguilar");

        JTextField txtTelefono = new JTextField();
        txtTelefono.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTelefono.setPreferredSize(new Dimension(0, 38));
        txtTelefono.putClientProperty("JTextField.placeholderText", "10 dígitos numéricos");

        JTextField txtDireccion = new JTextField();
        txtDireccion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDireccion.setPreferredSize(new Dimension(0, 38));
        txtDireccion.putClientProperty("JTextField.placeholderText", "Calle, Número, Colonia");

        JTextField txtLimiteCredito = new JTextField("0.00");
        txtLimiteCredito.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtLimiteCredito.setPreferredSize(new Dimension(0, 38));

        // Agregar Componentes al Formulario
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(crearEtiqueta("Nombre / Razón Social *:", fontLabel, colorLabel), gbc);
        gbc.gridy = 1;
        form.add(txtNombre, gbc);

        gbc.gridy = 2;
        form.add(crearEtiqueta("Teléfono (10 dígitos):", fontLabel, colorLabel), gbc);
        gbc.gridy = 3;
        form.add(txtTelefono, gbc);

        gbc.gridy = 4;
        form.add(crearEtiqueta("Dirección:", fontLabel, colorLabel), gbc);
        gbc.gridy = 5;
        form.add(txtDireccion, gbc);

        gbc.gridy = 6;
        form.add(crearEtiqueta("Límite de Crédito ($) *:", fontLabel, colorLabel), gbc);
        gbc.gridy = 7;
        form.add(txtLimiteCredito, gbc);

        // --- BOTÓN DE ACCIÓN ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);

        JButton btnGuardar = new JButton("Guardar Cliente");
        btnGuardar.setBackground(new Color(16, 185, 129)); // Verde Esmeralda Moderno
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setPreferredSize(new Dimension(160, 42));
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "arc:12; borderWidth:0");

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtDireccion.getText().trim();
            String limiteStr = txtLimiteCredito.getText().trim();

            if (!validarCamposCliente(dialog, nombre, telefono, limiteStr)) {
                return;
            }

            try {
                ClienteDTO nuevo = new ClienteDTO();
                nuevo.setNombre(nombre);
                nuevo.setTelefono(telefono);
                nuevo.setDireccion(direccion);
                nuevo.setLimiteCredito(new BigDecimal(limiteStr));
                nuevo.setDeudaActual(BigDecimal.ZERO);

                clienteServicio.guardarCliente(nuevo);
                JOptionPane.showMessageDialog(dialog, "¡Cliente guardado exitosamente!");
                dialog.dispose();
                cargarDatosTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al guardar el cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnGuardar);

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        panelPrincipal.add(form, BorderLayout.CENTER);
        panelPrincipal.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(panelPrincipal);
        dialog.setVisible(true);
    }

    // ==========================================
    // MODAL: EDITAR CLIENTE
    // ==========================================
    private void abrirModalEditarCliente() {
        int filaSeleccionada = tableClientes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un cliente de la tabla para editar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long clienteId = (Long) tableModel.getValueAt(filaSeleccionada, 0);
        List<ClienteDTO> clientes = clienteServicio.obtenerTodosLosClientes();
        ClienteDTO clienteActual = clientes.stream().filter(c -> c.getId().equals(clienteId)).findFirst().orElse(null);

        if (clienteActual == null) {
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Editar Cliente", true);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 0));

        JPanel panelPrincipal = new JPanel(new BorderLayout(16, 16));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panelPrincipal.setBackground(Color.WHITE);

        // --- ENCABEZADO ---
        JLabel lblTitulo = new JLabel("Editar Información del Cliente");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(30, 41, 59));

        // --- FORMULARIO CENTRAL ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Color colorLabel = new Color(71, 85, 105);

        JTextField txtNombre = new JTextField(clienteActual.getNombre());
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNombre.setPreferredSize(new Dimension(0, 38));

        JTextField txtTelefono = new JTextField(clienteActual.getTelefono());
        txtTelefono.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTelefono.setPreferredSize(new Dimension(0, 38));

        JTextField txtDireccion = new JTextField(clienteActual.getDireccion());
        txtDireccion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDireccion.setPreferredSize(new Dimension(0, 38));

        JTextField txtLimiteCredito = new JTextField(String.valueOf(clienteActual.getLimiteCredito()));
        txtLimiteCredito.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtLimiteCredito.setPreferredSize(new Dimension(0, 38));

        // Agregar Componentes al Formulario
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(crearEtiqueta("Nombre / Razón Social *:", fontLabel, colorLabel), gbc);
        gbc.gridy = 1;
        form.add(txtNombre, gbc);

        gbc.gridy = 2;
        form.add(crearEtiqueta("Teléfono (10 dígitos):", fontLabel, colorLabel), gbc);
        gbc.gridy = 3;
        form.add(txtTelefono, gbc);

        gbc.gridy = 4;
        form.add(crearEtiqueta("Dirección:", fontLabel, colorLabel), gbc);
        gbc.gridy = 5;
        form.add(txtDireccion, gbc);

        gbc.gridy = 6;
        form.add(crearEtiqueta("Límite de Crédito ($) *:", fontLabel, colorLabel), gbc);
        gbc.gridy = 7;
        form.add(txtLimiteCredito, gbc);

        // --- BOTÓN DE ACCIÓN ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);

        JButton btnActualizar = new JButton("Actualizar Cliente");
        btnActualizar.setBackground(new Color(245, 158, 11)); // Ámbar / Naranja Moderno
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnActualizar.setPreferredSize(new Dimension(170, 42));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "arc:12; borderWidth:0");

        btnActualizar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtDireccion.getText().trim();
            String limiteStr = txtLimiteCredito.getText().trim();

            if (!validarCamposCliente(dialog, nombre, telefono, limiteStr)) {
                return;
            }

            try {
                clienteServicio.actualizarCliente(
                        clienteId,
                        nombre,
                        telefono,
                        direccion,
                        new BigDecimal(limiteStr)
                );
                JOptionPane.showMessageDialog(dialog, "¡Cliente actualizado correctamente!");
                dialog.dispose();
                cargarDatosTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnActualizar);

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        panelPrincipal.add(form, BorderLayout.CENTER);
        panelPrincipal.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(panelPrincipal);
        dialog.setVisible(true);
    }

    // Método de soporte rápido para estilizar etiquetas
    private JLabel crearEtiqueta(String texto, Font font, Color color) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    private void eliminarClienteSeleccionado() {
        int filaSeleccionada = tableClientes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un cliente de la tabla para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long clienteId = (Long) tableModel.getValueAt(filaSeleccionada, 0);
        String nombreCliente = (String) tableModel.getValueAt(filaSeleccionada, 1);

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al cliente " + nombreCliente + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                clienteServicio.eliminarCliente(clienteId);
                JOptionPane.showMessageDialog(this, "Cliente eliminado exitosamente.");
                cargarDatosTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void abrirModalAbono() {
        // 1. Obtener todos los clientes de la BD previamente
        List<ClienteDTO> listaClientesOriginales = new ArrayList<>();
        if (clienteServicio != null) {
            try {
                List<ClienteDTO> clientes = clienteServicio.obtenerTodosLosClientes();
                if (clientes != null) {
                    listaClientesOriginales.addAll(clientes);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. Configuración del diálogo
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Registrar Abono y/o Devolución de Envases", true);
        dialog.setSize(640, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 0));

        JPanel panelPrincipal = new JPanel(new BorderLayout(14, 14));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelPrincipal.setBackground(Color.WHITE);

        // --- ENCABEZADO Y BUSCADOR ---
        JPanel panelNorte = new JPanel(new BorderLayout(8, 8));
        panelNorte.setOpaque(false);

        JLabel lblTituloModal = new JLabel("Control de Cartera y Envases - Búsqueda de Cliente");
        lblTituloModal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTituloModal.setForeground(new Color(30, 41, 59));

        JTextField txtFiltro = new JTextField();
        txtFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtFiltro.putClientProperty("JTextField.placeholderText", "Escribe el nombre del cliente para filtrar...");
        txtFiltro.putClientProperty("JTextField.showClearButton", true);
        txtFiltro.setPreferredSize(new Dimension(0, 38));

        panelNorte.add(lblTituloModal, BorderLayout.NORTH);
        panelNorte.add(txtFiltro, BorderLayout.CENTER);

        // --- TABLA DE CLIENTES ---
        String[] columnas = {"ID", "Nombre del Cliente", "Deuda Actual", "Envases Pendientes"};
        javax.swing.table.DefaultTableModel modeloTabla = new javax.swing.table.DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        javax.swing.JTable tablaClientes = new javax.swing.JTable(modeloTabla);
        tablaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaClientes.setRowHeight(32);
        tablaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaClientes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(60);

        JComboBox<ItemCombo> cmbProductosPrestados = new JComboBox<>();
        cmbProductosPrestados.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextField txtMontoAbono = new JTextField("0.00");
        txtMontoAbono.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMontoAbono.putClientProperty("JTextField.showClearButton", true);

        JTextField txtEnvasesDevueltos = new JTextField("0");
        txtEnvasesDevueltos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtEnvasesDevueltos.putClientProperty("JTextField.showClearButton", true);

        // Función para actualizar los campos según el cliente seleccionado en la tabla (Ya no sobreescribe el abono con la deuda)
        Runnable actualizarCamposClienteSeleccionado = () -> {
            int filaSel = tablaClientes.getSelectedRow();
            if (filaSel != -1) {
                cargarProductosEnCombo(tablaClientes, modeloTabla, cmbProductosPrestados);
            }
        };

        // Función para filtrar y llenar la tabla
        Runnable actualizarTabla = () -> {
            modeloTabla.setRowCount(0);
            String filtro = txtFiltro.getText().toLowerCase().trim();

            for (ClienteDTO c : listaClientesOriginales) {
                String nombre = c.getNombre() != null ? c.getNombre() : "";
                String telefono = c.getTelefono() != null ? c.getTelefono() : "";
                BigDecimal deuda = c.getDeudaActual() != null ? c.getDeudaActual() : BigDecimal.ZERO;
                int envases = c.getEnvasesPendientes() != null ? c.getEnvasesPendientes() : 0;

                if (filtro.isEmpty() || nombre.toLowerCase().contains(filtro) || telefono.toLowerCase().contains(filtro)) {
                    modeloTabla.addRow(new Object[]{c.getId(), nombre, "$" + deuda, envases + " pzas"});
                }
            }
            if (modeloTabla.getRowCount() > 0) {
                tablaClientes.setRowSelectionInterval(0, 0);
                actualizarCamposClienteSeleccionado.run();
            } else {
                cmbProductosPrestados.removeAllItems();
            }
        };

        actualizarTabla.run();

        // Listener cuando el usuario selecciona otro cliente en la tabla
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaClientes.getSelectedRow() != -1) {
                actualizarCamposClienteSeleccionado.run();
            }
        });

        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizarTabla.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizarTabla.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizarTabla.run();
            }
        });

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(tablaClientes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.setPreferredSize(new Dimension(0, 160));

        // --- PANEL DE FORMULARIO ---
        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 12, 10));
        panelFormulario.setOpaque(false);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Color colorLabel = new Color(71, 85, 105);

        JLabel lblMonto = new JLabel("Monto a Abonar ($):");
        lblMonto.setFont(fontLabel);
        lblMonto.setForeground(colorLabel);

        JLabel lblProductoDev = new JLabel("Artículo a Devolver:");
        lblProductoDev.setFont(fontLabel);
        lblProductoDev.setForeground(colorLabel);

        JLabel lblEnvases = new JLabel("Cantidad a Devolver (pzas):");
        lblEnvases.setFont(fontLabel);
        lblEnvases.setForeground(colorLabel);

        panelFormulario.add(lblMonto);
        panelFormulario.add(lblProductoDev);
        panelFormulario.add(txtMontoAbono);
        panelFormulario.add(cmbProductosPrestados);
        panelFormulario.add(lblEnvases);
        panelFormulario.add(txtEnvasesDevueltos);

        // --- BOTÓN DE ACCIÓN ---
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setOpaque(false);

        JButton btnAplicar = new JButton("Registrar Operación");
        btnAplicar.setBackground(new Color(13, 148, 136));
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAplicar.setPreferredSize(new Dimension(0, 46));
        btnAplicar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAplicar.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "arc:12; borderWidth:0");

        btnAplicar.addActionListener(e -> {
            try {
                int filaSeleccionada = tablaClientes.getSelectedRow();
                if (filaSeleccionada == -1) {
                    JOptionPane.showMessageDialog(dialog, "Seleccione un cliente de la tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Long clienteId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
                BigDecimal monto = new BigDecimal(txtMontoAbono.getText().trim().isEmpty() ? "0" : txtMontoAbono.getText().trim());
                int envases = Integer.parseInt(txtEnvasesDevueltos.getText().trim().isEmpty() ? "0" : txtEnvasesDevueltos.getText().trim());

                ItemCombo itemSeleccionado = (ItemCombo) cmbProductosPrestados.getSelectedItem();
                Long productoId = (itemSeleccionado != null) ? itemSeleccionado.getId() : null;

                if (envases > 0 && productoId == null) {
                    JOptionPane.showMessageDialog(dialog, "Debe seleccionar el producto específico que está devolviendo.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                clienteServicio.registrarAbonoYDevolucion(clienteId, monto, productoId, envases);

                JOptionPane.showMessageDialog(dialog, "¡Operación registrada correctamente!");
                dialog.dispose();
                cargarDatosTabla();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Por favor, ingrese cantidades numéricas válidas.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(btnAplicar, BorderLayout.CENTER);

        JPanel panelCentro = new JPanel(new BorderLayout(0, 10));
        panelCentro.setOpaque(false);
        panelCentro.add(scrollPane, BorderLayout.CENTER);
        panelCentro.add(panelFormulario, BorderLayout.SOUTH);

        panelPrincipal.add(panelNorte, BorderLayout.NORTH);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
        panelPrincipal.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(panelPrincipal);
        dialog.setVisible(true);
    }
    private static class ItemCombo {

        private final Long id;
        private final String nombre;

        public ItemCombo(Long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public Long getId() {
            return id;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    // Método auxiliar para llenar el ComboBox de productos prestados del cliente seleccionado
    private void cargarProductosEnCombo(javax.swing.JTable tablaClientes, javax.swing.table.DefaultTableModel modeloTabla, JComboBox<ItemCombo> cmbProductosPrestados) {
        cmbProductosPrestados.removeAllItems();
        cmbProductosPrestados.addItem(new ItemCombo(null, "-- Seleccione producto (Opcional) --"));

        int filaSel = tablaClientes.getSelectedRow();
        if (filaSel != -1) {
            Long cliId = (Long) modeloTabla.getValueAt(filaSel, 0);
            if (clienteServicio != null) {
                try {
                    java.util.Map<Long, String> productosMap = clienteServicio.obtenerProductosPrestadosCliente(cliId);
                    for (java.util.Map.Entry<Long, String> entry : productosMap.entrySet()) {
                        cmbProductosPrestados.addItem(new ItemCombo(entry.getKey(), entry.getValue()));
                    }
                } catch (Exception ex) {
                    System.err.println("Error al cargar productos del cliente: " + ex.getMessage());
                }
            }
        }
    }

    public static class ItemClienteCombo {

        private final Long id;
        private final String texto;

        public ItemClienteCombo(Long id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        public Long getId() {
            return id;
        }

        @Override
        public String toString() {
            return texto;
        }
    }
}
