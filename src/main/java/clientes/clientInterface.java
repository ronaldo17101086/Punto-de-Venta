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
import java.util.List;

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

    public clientInterface() {}

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
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarClientesTabla(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarClientesTabla(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarClientesTabla(); }
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

        JButton btnAbono = new JButton("💳 Abonar");
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

        // ===== TABLA DE CLIENTES (Sin la columna Estado) =====
        String[] columnas = {"ID", "Nombre / Razón Social", "Teléfono", "Límite Crédito", "Deuda Actual (Debe)", "Crédito Disponible"};
        
        tableModel = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableClientes = new JTable(tableModel);
        tableClientes.setRowHeight(35);
        tableClientes.setShowVerticalLines(false);
        tableClientes.setSelectionBackground(new Color(230, 240, 255));
        tableClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tableClientes);
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        cargarDatosTabla();
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

            tableModel.addRow(new Object[]{
                c.getId(),
                c.getNombre(),
                c.getTelefono() != null ? c.getTelefono() : "N/A",
                "$" + limite,
                "$" + deuda,
                "$" + disponible
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

            tableModel.addRow(new Object[]{
                c.getId(),
                c.getNombre(),
                c.getTelefono() != null ? c.getTelefono() : "N/A",
                "$" + limite,
                "$" + deuda,
                "$" + disponible
            });

            deudaTotalTienda = deudaTotalTienda.add(deuda);
            creditoGlobalDisp = creditoGlobalDisp.add(disponible);
        }

        lblTotalClientesVal.setText(String.valueOf(clientes.size()));
        lblDeudaTotalVal.setText("$" + deudaTotalTienda);
        lblCreditoGlobalVal.setText("$" + creditoGlobalDisp);
    }

    private void abrirModalNuevoCliente() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Registrar Nuevo Cliente", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 15));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);

        JTextField txtNombre = new JTextField();
        JTextField txtTelefono = new JTextField();
        JTextField txtDireccion = new JTextField();
        JTextField txtLimiteCredito = new JTextField("0.00");

        form.add(new JLabel("Nombre / Razón Social:"));
        form.add(txtNombre);
        form.add(new JLabel("Teléfono:"));
        form.add(txtTelefono);
        form.add(new JLabel("Dirección:"));
        form.add(txtDireccion);
        form.add(new JLabel("Límite de Crédito ($):"));
        form.add(txtLimiteCredito);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnGuardar = new JButton("Guardar Cliente");
        btnGuardar.setBackground(new Color(32, 201, 151));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        
        btnGuardar.addActionListener(e -> {
            try {
                ClienteDTO nuevo = new ClienteDTO();
                nuevo.setNombre(txtNombre.getText().trim());
                nuevo.setTelefono(txtTelefono.getText().trim());
                nuevo.setDireccion(txtDireccion.getText().trim());
                nuevo.setLimiteCredito(new BigDecimal(txtLimiteCredito.getText().trim()));
                nuevo.setDeudaActual(BigDecimal.ZERO);

                clienteServicio.guardarCliente(nuevo);
                JOptionPane.showMessageDialog(dialog, "¡Cliente guardado exitosamente!");
                dialog.dispose();
                cargarDatosTabla(); 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: Verifique que los campos numéricos sean válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnGuardar);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void abrirModalEditarCliente() {
        int filaSeleccionada = tableClientes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un cliente de la tabla para editar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long clienteId = (Long) tableModel.getValueAt(filaSeleccionada, 0);
        List<ClienteDTO> clientes = clienteServicio.obtenerTodosLosClientes();
        ClienteDTO clienteActual = clientes.stream().filter(c -> c.getId().equals(clienteId)).findFirst().orElse(null);

        if (clienteActual == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Editar Cliente", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 15));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);

        JTextField txtNombre = new JTextField(clienteActual.getNombre());
        JTextField txtTelefono = new JTextField(clienteActual.getTelefono());
        JTextField txtDireccion = new JTextField(clienteActual.getDireccion());
        JTextField txtLimiteCredito = new JTextField(String.valueOf(clienteActual.getLimiteCredito()));

        form.add(new JLabel("Nombre / Razón Social:"));
        form.add(txtNombre);
        form.add(new JLabel("Teléfono:"));
        form.add(txtTelefono);
        form.add(new JLabel("Dirección:"));
        form.add(txtDireccion);
        form.add(new JLabel("Límite de Crédito ($):"));
        form.add(txtLimiteCredito);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnActualizar = new JButton("Actualizar Cliente");
        btnActualizar.setBackground(new Color(255, 193, 7));
        btnActualizar.setForeground(new Color(40, 40, 40));
        btnActualizar.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        
        btnActualizar.addActionListener(e -> {
            try {
                clienteServicio.actualizarCliente(
                    clienteId,
                    txtNombre.getText().trim(),
                    txtTelefono.getText().trim(),
                    txtDireccion.getText().trim(),
                    new BigDecimal(txtLimiteCredito.getText().trim())
                );
                JOptionPane.showMessageDialog(dialog, "¡Cliente actualizado correctamente!");
                dialog.dispose();
                cargarDatosTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnActualizar);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Registrar Abono a Deuda", true);
        dialog.setSize(420, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);

        List<ClienteDTO> clientes = clienteServicio.obtenerTodosLosClientes();
        JComboBox<ItemClienteCombo> cmbClientes = new JComboBox<>();
        for (ClienteDTO c : clientes) {
            BigDecimal deuda = c.getDeudaActual() != null ? c.getDeudaActual() : BigDecimal.ZERO;
            cmbClientes.addItem(new ItemClienteCombo(c.getId(), c.getNombre() + " (Debe: $" + deuda + ")"));
        }

        JTextField txtMontoAbono = new JTextField();

        form.add(new JLabel("Seleccionar Cliente:"));
        form.add(cmbClientes);
        form.add(new JLabel("Monto a Abonar ($):"));
        form.add(txtMontoAbono);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnAplicar = new JButton("Aplicar Abono");
        btnAplicar.setBackground(new Color(23, 162, 184));
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
        
        btnAplicar.addActionListener(e -> {
            try {
                ItemClienteCombo seleccionado = (ItemClienteCombo) cmbClientes.getSelectedItem();
                if (seleccionado == null) {
                    JOptionPane.showMessageDialog(dialog, "Seleccione un cliente válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                BigDecimal monto = new BigDecimal(txtMontoAbono.getText().trim());
                clienteServicio.registrarAbono(seleccionado.getId(), monto);

                JOptionPane.showMessageDialog(dialog, "¡Abono registrado correctamente! Deuda actualizada.");
                dialog.dispose();
                cargarDatosTabla(); 
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Por favor, ingrese una cantidad numérica válida.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(btnAplicar);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public static class ItemClienteCombo {
        private final Long id;
        private final String texto;

        public ItemClienteCombo(Long id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        public Long getId() { return id; }

        @Override
        public String toString() { return texto; }
    }
}