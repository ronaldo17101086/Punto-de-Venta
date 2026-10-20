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
import java.util.Map;

@Service
public class ClienteServicio {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CajaService cajaService;

    @Autowired
    public void inicializarTablaDevoluciones() {
        // Aseguramos que la tabla incluya producto_id para que no falle el registro
        String sql = "CREATE TABLE IF NOT EXISTS envases_devoluciones ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "cliente_id BIGINT, "
                + "producto_id BIGINT, "
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
        String sqlComprados = "SELECT COALESCE(SUM(vp.cantidad), 0) "
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
        // Consulta general de productos de ventas a crédito del cliente sin restricciones estrictas de nombres
        String sqlComprados = "SELECT p.id AS producto_id, p.name AS producto, SUM(vp.cantidad) AS cantidad "
                + "FROM ventas v "
                + "JOIN venta_productos vp ON v.id = vp.registro_venta_id "
                + "JOIN productos p ON vp.producto_id = p.id "
                + "WHERE v.cliente_id = ? "
                + "GROUP BY p.id, p.name";

        String sqlDevueltos = "SELECT producto_id, SUM(cantidad) AS devueltos FROM envases_devoluciones WHERE cliente_id = ? GROUP BY producto_id";

        java.util.Map<Long, Integer> mapaDevueltos = new java.util.HashMap<>();
        try (Connection conn = dataSource.getConnection()) {

            // 1. Cargar devoluciones
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDevueltos)) {
                pstmt.setLong(1, cliente.getId());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    mapaDevueltos.put(rs.getLong("producto_id"), rs.getInt("devueltos"));
                }
            }

            // 2. Calcular compras menos devoluciones
            try (PreparedStatement pstmt = conn.prepareStatement(sqlComprados)) {
                pstmt.setLong(1, cliente.getId());
                ResultSet rs = pstmt.executeQuery();

                int totalPendientesGlobal = 0;
                StringBuilder desglose = new StringBuilder();

                while (rs.next()) {
                    Long prodId = rs.getLong("producto_id");
                    String nombreProd = rs.getString("producto");
                    int cantPrestada = rs.getInt("cantidad");
                    int cantDevuelta = mapaDevueltos.getOrDefault(prodId, 0);

                    int cantRealPendiente = cantPrestada - cantDevuelta;
                    if (cantRealPendiente < 0) {
                        cantRealPendiente = 0;
                    }

                    totalPendientesGlobal += cantRealPendiente;

                    if (cantRealPendiente > 0) {
                        if (desglose.length() > 0) {
                            desglose.append(", ");
                        }
                        desglose.append(cantRealPendiente).append(" ").append(nombreProd);
                    }
                }

                cliente.setEnvasesPendientes(totalPendientesGlobal);
                cliente.setDetalleProductos(desglose.length() > 0 ? desglose.toString() : "Sin productos prestados");
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

    public BigDecimal obtenerPrecioProducto(Long productoId) {
        String sqlPrecio = "SELECT p.price FROM productos p WHERE p.id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlPrecio)) {
            pstmt.setLong(1, productoId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal precio = rs.getBigDecimal(1);
                return precio != null ? precio : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener precio del producto: " + e.getMessage());
        }
        return BigDecimal.ZERO;
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

    public Map<Long, String> obtenerProductosPrestadosCliente(Long clienteId) {
        Map<Long, String> productosMap = new java.util.LinkedHashMap<>();

        // Consulta directa y segura de los productos en ventas del cliente restando sus devoluciones
        String sql = "SELECT p.id, p.name FROM productos p "
                + "JOIN ("
                + "    SELECT vp.producto_id, SUM(vp.cantidad) as total_prestado "
                + "    FROM ventas v "
                + "    JOIN venta_productos vp ON v.id = vp.registro_venta_id "
                + "    WHERE v.cliente_id = ? "
                + "    GROUP BY vp.producto_id"
                + ") prestados ON p.id = prestados.producto_id "
                + "LEFT JOIN ("
                + "    SELECT ed.producto_id, SUM(ed.cantidad) as total_devuelto "
                + "    FROM envases_devoluciones ed "
                + "    WHERE ed.cliente_id = ? "
                + "    GROUP BY ed.producto_id"
                + ") devueltos ON p.id = devueltos.producto_id "
                + "WHERE prestados.total_prestado > COALESCE(devueltos.total_devuelto, 0)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, clienteId);
            pstmt.setLong(2, clienteId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                productosMap.put(rs.getLong("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos prestados del cliente: " + e.getMessage());
        }
        return productosMap;
    }

public void registrarAbonoYDevolucion(Long clienteId, BigDecimal montoAbono, Long productoId, int cantidadDevuelta) throws Exception {
        ClienteDTO cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new Exception("El cliente no existe en la base de datos."));

        BigDecimal deudaActual = cliente.getDeudaActual() != null ? cliente.getDeudaActual() : BigDecimal.ZERO;
        boolean operacionRealizada = false;

        try (Connection conn = dataSource.getConnection()) {
            
            // 1. Si se indicó una devolución manual de un producto específico en el modal
            if (cantidadDevuelta > 0 && productoId != null) {
                int prestadasProducto = 0;
                String sqlPrestadas = "SELECT COALESCE(SUM(vp.cantidad), 0) FROM ventas v "
                        + "JOIN venta_productos vp ON v.id = vp.registro_venta_id "
                        + "WHERE v.cliente_id = ? AND vp.producto_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlPrestadas)) {
                    ps.setLong(1, clienteId);
                    ps.setLong(2, productoId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        prestadasProducto = rs.getInt(1);
                    }
                }

                int devueltasProducto = 0;
                String sqlDevueltasProd = "SELECT COALESCE(SUM(cantidad), 0) FROM envases_devoluciones WHERE cliente_id = ? AND producto_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlDevueltasProd)) {
                    ps.setLong(1, clienteId);
                    ps.setLong(2, productoId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        devueltasProducto = rs.getInt(1);
                    }
                }

                int pendientesProducto = prestadasProducto - devueltasProducto;
                if (pendientesProducto < 0) {
                    pendientesProducto = 0;
                }

                if (cantidadDevuelta > pendientesProducto) {
                    throw new Exception("No puedes devolver " + cantidadDevuelta + " piezas. El cliente solo tiene " + pendientesProducto + " pendientes de este artículo.");
                }

                String sqlPrecio = "SELECT p.price FROM productos p WHERE p.id = ?";
                BigDecimal precioUnitarioReal = BigDecimal.ZERO;
                try (PreparedStatement pstmt = conn.prepareStatement(sqlPrecio)) {
                    pstmt.setLong(1, productoId);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        precioUnitarioReal = rs.getBigDecimal(1);
                    }
                }
                if (precioUnitarioReal == null) {
                    precioUnitarioReal = BigDecimal.ZERO;
                }

                BigDecimal valorEnvasesDevueltos = precioUnitarioReal.multiply(new BigDecimal(cantidadDevuelta));

                String sqlIns = "INSERT INTO envases_devoluciones (cliente_id, producto_id, cantidad, monto_descontado) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlIns)) {
                    pstmt.setLong(1, clienteId);
                    pstmt.setLong(2, productoId);
                    pstmt.setInt(3, cantidadDevuelta);
                    pstmt.setBigDecimal(4, valorEnvasesDevueltos);
                    pstmt.executeUpdate();
                }
                operacionRealizada = true;
            }

            // 2. Si hay abono en dinero
            if (montoAbono != null && montoAbono.compareTo(BigDecimal.ZERO) > 0) {
                if (montoAbono.compareTo(deudaActual) > 0) {
                    throw new Exception("El abono de $" + montoAbono + " no puede ser mayor a la deuda actual del cliente ($" + deudaActual + ").");
                }

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

                BigDecimal nuevaDeuda = deudaActual.subtract(montoAbono);
                if (nuevaDeuda.compareTo(BigDecimal.ZERO) < 0) {
                    nuevaDeuda = BigDecimal.ZERO;
                }
                clienteRepository.actualizarDeudaPorId(clienteId, nuevaDeuda);

                // --- CONDICIÓN NUEVA: Si el cliente liquida TODA la deuda monetaria (queda en $0.00), 
                // liberamos automáticamente los envases pendientes que tenga ---
                if (nuevaDeuda.compareTo(BigDecimal.ZERO) == 0) {
                    String sqlPendientes = "SELECT p.id AS producto_id, p.price, "
                            + "(COALESCE(prestados.total_prestado, 0) - COALESCE(devueltos.total_devuelto, 0)) AS pendiente "
                            + "FROM productos p "
                            + "JOIN ("
                            + "    SELECT vp.producto_id, SUM(vp.cantidad) as total_prestado "
                            + "    FROM ventas v "
                            + "    JOIN venta_productos vp ON v.id = vp.registro_venta_id "
                            + "    WHERE v.cliente_id = ? GROUP BY vp.producto_id"
                            + ") prestados ON p.id = prestados.producto_id "
                            + "LEFT JOIN ("
                            + "    SELECT ed.producto_id, SUM(ed.cantidad) as total_devuelto "
                            + "    FROM envases_devoluciones ed "
                            + "    WHERE ed.cliente_id = ? GROUP BY ed.producto_id"
                            + ") devueltos ON p.id = devueltos.producto_id "
                            + "HAVING pendiente > 0";

                    try (PreparedStatement psPend = conn.prepareStatement(sqlPendientes)) {
                        psPend.setLong(1, clienteId);
                        psPend.setLong(2, clienteId);
                        ResultSet rsPend = psPend.executeQuery();

                        String sqlInsAuto = "INSERT INTO envases_devoluciones (cliente_id, producto_id, cantidad, monto_descontado) VALUES (?, ?, ?, ?)";
                        while (rsPend.next()) {
                            Long pId = rsPend.getLong("producto_id");
                            int cantPendiente = rsPend.getInt("pendiente");
                            BigDecimal precioP = rsPend.getBigDecimal("price");
                            if (precioP == null) {
                                precioP = BigDecimal.ZERO;
                            }
                            BigDecimal subtotalP = precioP.multiply(new BigDecimal(cantPendiente));

                            try (PreparedStatement psIns = conn.prepareStatement(sqlInsAuto)) {
                                psIns.setLong(1, clienteId);
                                psIns.setLong(2, pId);
                                psIns.setInt(3, cantPendiente);
                                psIns.setBigDecimal(4, subtotalP);
                                psIns.executeUpdate();
                            }
                        }
                    }
                }

                operacionRealizada = true;
            }

            if (!operacionRealizada) {
                throw new Exception("Debe ingresar un monto de abono válido o indicar al menos 1 envase a devolver.");
            }
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
