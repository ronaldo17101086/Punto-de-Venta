package com.mycompany.chancuellarpuntodeventa.services.querys;

import caja.CajaService;
import caja.MovimientoCajaDto;
import caja.TipoMovimiento;
import com.mycompany.chancuellarpuntodeventa.services.dtos.ClienteDTO;
import com.mycompany.chancuellarpuntodeventa.services.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Service
public class ClienteServicio {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DataSource dataSource;
    
    // --- AQUÍ FALTABA INYECTAR EL SERVICIO DE CAJA ---
    @Autowired
    private CajaService cajaService;

    @Autowired
    public void inicializarTablaDevoluciones() {
        String sql = "CREATE TABLE IF NOT EXISTS envases_devoluciones ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "cliente_id BIGINT, "
                + "cantidad INT, "
                + "monto_descontado DECIMAL(10,2) DEFAULT 0.00, "
                + "fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear tabla envases_devoluciones: " + e.getMessage());
        }
    }

    public List<ClienteDTO> obtenerTodosLosClientes() {
        List<ClienteDTO> clientes = clienteRepository.findAll();
        enriquecerEnvases(clientes);
        return clientes;
    }

    public ClienteDTO obtenerClientePorId(Long id) {
        ClienteDTO cliente = clienteRepository.findById(id).orElse(null);
        if (cliente != null) {
            obtenerDetalleEnvasesCliente(cliente);
        }
        return cliente;
    }

    public List<ClienteDTO> buscarClientes(String filtro) {
        List<ClienteDTO> clientes;
        if (filtro == null || filtro.trim().isEmpty()) {
            clientes = clienteRepository.findAll();
        } else {
            clientes = clienteRepository.buscarPorNombreOTelefono(filtro.trim());
        }
        enriquecerEnvases(clientes);
        return clientes;
    }

    private void enriquecerEnvases(List<ClienteDTO> clientes) {
        for (ClienteDTO c : clientes) {
            obtenerDetalleEnvasesCliente(c);
        }
    }

    private int calcularEnvasesCliente(Long clienteId) {
        // Conteo de comprados: Exclusivo ventas a crédito (v.es_credito = 1) y palabra 'prestado'
        String sqlComprados = "SELECT COUNT(vp.producto_id) "
                + "FROM ventas v "
                + "JOIN venta_productos vp ON v.id = vp.registro_venta_id "
                + "JOIN productos p ON vp.producto_id = p.id "
                + "WHERE v.cliente_id = ? AND (v.es_credito = 1 OR v.es_credito IS TRUE) AND "
                + "LOWER(p.name) LIKE '%prestado%'";

        String sqlDevueltos = "SELECT COALESCE(SUM(cantidad), 0) FROM envases_devoluciones WHERE cliente_id = ?";

        int comprados = 0;
        int devueltos = 0;

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sqlComprados)) {
                pstmt.setLong(1, clienteId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    comprados = rs.getInt(1);
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDevueltos)) {
                pstmt.setLong(1, clienteId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    devueltos = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular envases pendientes: " + e.getMessage());
        }

        int pendientes = comprados - devueltos;
        return pendientes < 0 ? 0 : pendientes;
    }

    private void obtenerDetalleEnvasesCliente(ClienteDTO cliente) {
        // Detalle de comprados: Exclusivo ventas a crédito (v.es_credito = 1) y palabra 'prestado'
        String sqlComprados = "SELECT p.name AS producto, COUNT(vp.producto_id) AS cantidad "
                + "FROM ventas v "
                + "JOIN venta_productos vp ON v.id = vp.registro_venta_id "
                + "JOIN productos p ON vp.producto_id = p.id "
                + "WHERE v.cliente_id = ? AND (v.es_credito = 1 OR v.es_credito IS TRUE) AND "
                + "LOWER(p.name) LIKE '%prestado%' "
                + "GROUP BY p.name";

        String sqlDevueltos = "SELECT COALESCE(SUM(cantidad), 0) FROM envases_devoluciones WHERE cliente_id = ?";

        int totalDevueltos = 0;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDevueltos)) {
                pstmt.setLong(1, cliente.getId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    totalDevueltos = rs.getInt(1);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlComprados)) {
                pstmt.setLong(1, cliente.getId());
                ResultSet rs = pstmt.executeQuery();

                int totalComprados = 0;
                java.util.Map<String, Integer> productosCompradosMap = new java.util.LinkedHashMap<>();

                while (rs.next()) {
                    String nombreProd = rs.getString("producto");
                    int cant = rs.getInt("cantidad");
                    totalComprados += cant;
                    productosCompradosMap.put(nombreProd, cant);
                }

                int pendientes = totalComprados - totalDevueltos;
                if (pendientes < 0) {
                    pendientes = 0;
                }

                cliente.setEnvasesPendientes(pendientes);

                if (pendientes > 0 && !productosCompradosMap.isEmpty()) {
                    StringBuilder desglose = new StringBuilder();
                    int restantePorAsignar = pendientes;

                    for (java.util.Map.Entry<String, Integer> entry : productosCompradosMap.entrySet()) {
                        int cantAsignar = Math.min(entry.getValue(), restantePorAsignar);
                        if (cantAsignar > 0) {
                            if (desglose.length() > 0) {
                                desglose.append(", ");
                            }
                            desglose.append(cantAsignar).append(" ").append(entry.getKey());
                            restantePorAsignar -= cantAsignar;
                        }
                    }
                    cliente.setDetalleProductos(desglose.toString());
                } else {
                    cliente.setDetalleProductos("Sin productos prestados");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular desglose de envases: " + e.getMessage());
            cliente.setEnvasesPendientes(0);
            cliente.setDetalleProductos("Sin productos prestados");
        }
    }

    private BigDecimal obtenerPrecioUnitarioEnvase() {
        String sql = "SELECT price FROM productos WHERE name LIKE '%prestado%' LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("price");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener precio de envase: " + e.getMessage());
        }
        return new BigDecimal("10.00");
    }

    public void guardarCliente(ClienteDTO cliente) {
        clienteRepository.save(cliente);
    }

    public void actualizarCliente(Long id, String nombre, String telefono, String direccion, BigDecimal limiteCredito) throws Exception {
        ClienteDTO cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("El cliente no existe en la base de datos."));

        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        cliente.setLimiteCredito(limiteCredito);

        clienteRepository.save(cliente);
    }

    public void eliminarCliente(Long id) throws Exception {
        ClienteDTO cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("El cliente no existe."));

        BigDecimal deuda = cliente.getDeudaActual() != null ? cliente.getDeudaActual() : BigDecimal.ZERO;
        if (deuda.compareTo(BigDecimal.ZERO) > 0) {
            throw new Exception("No se puede eliminar al cliente porque tiene una deuda pendiente de $" + deuda + ".");
        }

        clienteRepository.deleteById(id);
    }

 public void registrarAbonoYDevolucion(Long clienteId, BigDecimal montoAbono, int envasesDevueltos) throws Exception {
        ClienteDTO cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new Exception("El cliente no existe en la base de datos."));

        BigDecimal deudaActual = cliente.getDeudaActual() != null ? cliente.getDeudaActual() : BigDecimal.ZERO;
        BigDecimal valorTotalDescuento = BigDecimal.ZERO;

        int envasesPendientesActuales = calcularEnvasesCliente(clienteId);

        if (envasesDevueltos > 0) {
            if (envasesDevueltos > envasesPendientesActuales) {
                throw new Exception("No puedes registrar la devolución de " + envasesDevueltos + " envases porque el cliente solo debe " + envasesPendientesActuales + " pzas.");
            }

            BigDecimal precioEnvase = obtenerPrecioUnitarioEnvase();
            BigDecimal valorEnvasesDevueltos = precioEnvase.multiply(new BigDecimal(envasesDevueltos));
            valorTotalDescuento = valorTotalDescuento.add(valorEnvasesDevueltos);

            String sqlIns = "INSERT INTO envases_devoluciones (cliente_id, cantidad, monto_descontado) VALUES (?, ?, ?)";
            try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlIns)) {
                pstmt.setLong(1, clienteId);
                pstmt.setInt(2, envasesDevueltos);
                pstmt.setBigDecimal(3, valorEnvasesDevueltos);
                pstmt.executeUpdate();
            }
        }

        if (montoAbono != null && montoAbono.compareTo(BigDecimal.ZERO) > 0) {
            if (montoAbono.compareTo(deudaActual) > 0) {
                throw new Exception("El abono de $" + montoAbono + " no puede ser mayor a la deuda actual del cliente ($" + deudaActual + ").");
            }
            valorTotalDescuento = valorTotalDescuento.add(montoAbono);

            // ===================================================================
            // REGISTRAR EL ABONO EN LA CAJA INMEDIATAMENTE (Aunque sea parcial)
            // ===================================================================
            try {
                MovimientoCajaDto mov = new MovimientoCajaDto();
                mov.setTipo(TipoMovimiento.ENTRADA);
                mov.setMonto(montoAbono.doubleValue());
                mov.setConcepto("Abono de Crédito - Cliente ID: " + clienteId);
                mov.setUsuario("ADMIN");

                cajaService.registrarMovimiento(1, mov, montoAbono.doubleValue(), false);
            } catch (Exception e) {
                System.err.println("[ADVERTENCIA] No se pudo registrar el abono en la caja: " + e.getMessage());
            }
        }

        if (valorTotalDescuento.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Debe ingresar un monto de abono válido o indicar al menos 1 envase a devolver.");
        }

        // --- SI SE PAGA TODA LA CUENTA EN DINERO, SALDAMOS LOS ENVASES RESTANTES AUTOMÁTICAMENTE ---
        boolean pagaTodaLaCuenta = (montoAbono != null && montoAbono.compareTo(deudaActual) == 0);

        if (pagaTodaLaCuenta && envasesPendientesActuales > envasesDevueltos) {
            int envasesExtrasADevolver = envasesPendientesActuales - envasesDevueltos;
            BigDecimal precioEnvase = obtenerPrecioUnitarioEnvase();
            BigDecimal valorEnvasesExtras = precioEnvase.multiply(new BigDecimal(envasesExtrasADevolver));

            String sqlIns = "INSERT INTO envases_devoluciones (cliente_id, cantidad, monto_descontado) VALUES (?, ?, ?)";
            try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlIns)) {
                pstmt.setLong(1, clienteId);
                pstmt.setInt(2, envasesExtrasADevolver);
                pstmt.setBigDecimal(3, valorEnvasesExtras);
                pstmt.executeUpdate();
            }
        }
        // ------------------------------------------------------------------------------------------

        if (valorTotalDescuento.compareTo(deudaActual) > 0) {
            BigDecimal nuevaDeuda = BigDecimal.ZERO;
            clienteRepository.actualizarDeudaPorId(clienteId, nuevaDeuda);
        } else {
            BigDecimal nuevaDeuda = deudaActual.subtract(valorTotalDescuento);
            clienteRepository.actualizarDeudaPorId(clienteId, nuevaDeuda);
        }
    }
    public BigDecimal calcularDeudaTotal() {
        List<ClienteDTO> clientes = clienteRepository.findAll();
        BigDecimal total = BigDecimal.ZERO;
        for (ClienteDTO c : clientes) {
            if (c.getDeudaActual() != null) {
                total = total.add(c.getDeudaActual());
            }
        }
        return total;
    }
}
