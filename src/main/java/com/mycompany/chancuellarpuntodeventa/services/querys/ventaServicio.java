package com.mycompany.chancuellarpuntodeventa.services.querys;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ClienteDTO;
import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import com.mycompany.chancuellarpuntodeventa.services.dtos.VentaDTO;
import com.mycompany.chancuellarpuntodeventa.services.repository.ClienteRepository;
import com.mycompany.chancuellarpuntodeventa.services.repository.VentaRepository;

// Importaciones necesarias del paquete caja
import caja.CajaService;
import caja.MovimientoCajaDto;
import caja.TipoMovimiento;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class ventaServicio {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private CajaService cajaService;

    // Inyectamos el DataSource para poder ejecutar consultas SQL directas
    @Autowired
    private DataSource dataSource;

    // --- DTO INTERNO PARA LOS CLIENTES CON IMPORTE ---
    public static class ImporteClienteDTO {

        private Long clienteId;
        private String nombre;
        private String telefono;
        private int piezas;
        private String detalle;
        private BigDecimal montoTotal;

        public ImporteClienteDTO(Long clienteId, String nombre, String telefono, int piezas, String detalle, BigDecimal montoTotal) {
            this.clienteId = clienteId;
            this.nombre = nombre;
            this.telefono = telefono;
            this.piezas = piezas;
            this.detalle = detalle;
            this.montoTotal = montoTotal;
        }

        public Long getClienteId() {
            return clienteId;
        }

        public String getNombre() {
            return nombre;
        }

        public String getTelefono() {
            return telefono;
        }

        public int getPiezas() {
            return piezas;
        }

        public String getDetalle() {
            return detalle;
        }

        public BigDecimal getMontoTotal() {
            return montoTotal;
        }
    }

   @Transactional
    public String registrarVentaCompleta(VentaDTO dto) throws Exception {

        // 1. Validar que el carrito no esté vacío
        List<ProductoDTO> productos = dto.getProductos();
        if (productos == null || productos.isEmpty()) {
            throw new Exception("No hay productos en el carrito para registrar la venta.");
        }

        ClienteDTO cliente = dto.getCliente();
        Map<String, BigDecimal> formasPagoMap = dto.getFormasPagoMap();

        // 2. Verificar o generar Folio único (UUID)
        String folio = dto.getUuidVenta();
        if (folio == null || folio.isEmpty()) {
            folio = "NV-" + System.currentTimeMillis();
            dto.setUuidVenta(folio);
        }

        // 3. Si hay pago a crédito, actualizar la deuda del cliente
        BigDecimal montoCredito = (formasPagoMap != null) ? formasPagoMap.getOrDefault("CREDITO", BigDecimal.ZERO) : BigDecimal.ZERO;
        if (montoCredito.compareTo(BigDecimal.ZERO) > 0) {
            if (cliente == null || cliente.getId() == null) {
                throw new Exception("Debe seleccionar un cliente registrado para realizar una venta a crédito.");
            }

            BigDecimal deudaActual = cliente.getDeudaActual() != null ? cliente.getDeudaActual() : BigDecimal.ZERO;
            BigDecimal nuevaDeuda = deudaActual.add(montoCredito);
            clienteRepository.actualizarDeudaPorId(cliente.getId(), nuevaDeuda);
        }

        // 4. Determinar tipo de pago principal
        if (dto.getTipoPago() == null || dto.getTipoPago().isEmpty()) {
            String tipoPagoPrincipal = "EFECTIVO";
            if (formasPagoMap != null && !formasPagoMap.isEmpty()) {
                tipoPagoPrincipal = formasPagoMap.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("EFECTIVO");
            }
            dto.setTipoPago(tipoPagoPrincipal);
        }

        // 5. Construir y guardar la venta principal (Cabecera)
        RegistroVenta venta = new RegistroVenta();
        venta.setUuidVenta(dto.getUuidVenta());
        venta.setClienteId(dto.getClienteId());
        venta.setTotal(dto.getTotalVenta());
        venta.setTipoPago(dto.getTipoPago());
        venta.setEsCredito(dto.isEsCredito());
        venta.setComentarios(dto.getComentarios());
        venta.setFechaVenta(dto.getFechaVenta() != null ? dto.getFechaVenta() : LocalDate.now());
        venta.setHoraVenta(dto.getHoraVenta() != null ? dto.getHoraVenta() : LocalTime.now());

        BigDecimal efectivoRecibido = BigDecimal.ZERO;
        if (dto.getCambio() != null) {
            efectivoRecibido = dto.getCambio().getEfectivoRecibido() != null ? dto.getCambio().getEfectivoRecibido() : BigDecimal.ZERO;
            venta.setEfectivoRecibido(efectivoRecibido);
            venta.setCambioDevuelto(dto.getCambio().getCambioDevuelto());
        } else {
            venta.setEfectivoRecibido(BigDecimal.ZERO);
            venta.setCambioDevuelto(BigDecimal.ZERO);
        }

        // Guardamos la venta y obtenemos el ID generado por la BD
        RegistroVenta ventaGuardada = ventaRepository.save(venta);
        Long ventaId = ventaGuardada.getId();

        // ===================================================================
        // 5.1 INSERTAR LOS PRODUCTOS Y SUS CANTIDADES REALES MANUALMENTE
        // ===================================================================
        String sqlInsertDetalle = "INSERT INTO venta_productos (registro_venta_id, producto_id, cantidad) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlInsertDetalle)) {

            for (ProductoDTO prod : productos) {
                pstmt.setLong(1, ventaId);
                pstmt.setLong(2, prod.getId());
                
                // Tomamos la cantidad que trae el DTO; si viene nula o en ceros, aseguramos 1 por seguridad
                BigDecimal cantidadReal = (prod.getCantidad() != null && prod.getCantidad().compareTo(BigDecimal.ZERO) > 0) 
                                        ? prod.getCantidad() 
                                        : BigDecimal.ONE;
                
                pstmt.setBigDecimal(3, cantidadReal);
                pstmt.addBatch(); // Agrupamos para eficiencia
            }
            pstmt.executeBatch(); // Ejecutamos todas las inserciones de golpe

        } catch (SQLException e) {
            throw new Exception("Error al guardar el detalle de productos con sus cantidades: " + e.getMessage());
        }

        // 6. Impactar la caja automáticamente
        try {
            MovimientoCajaDto mov = new MovimientoCajaDto();
            mov.setTipo(TipoMovimiento.VENTA);
            mov.setMonto(dto.getTotalVenta() != null ? dto.getTotalVenta().doubleValue() : 0.0);
            mov.setConcepto("Venta de Mostrador - Folio: " + folio);
            mov.setUsuario("ADMIN");

            boolean esVentaCredito = dto.isEsCredito() || (montoCredito.compareTo(dto.getTotalVenta()) >= 0);
            double efectivoDouble = efectivoRecibido.doubleValue();

            cajaService.registrarMovimiento(1, mov, efectivoDouble, esVentaCredito);

        } catch (Exception e) {
            System.err.println("[ADVERTENCIA] No se pudo registrar el movimiento en la caja: " + e.getMessage());
        }

        return folio;
    }
    // ===================================================================
    // MÉTODOS DE CONSULTA Y GESTIÓN DE IMPORTES
    // ===================================================================
   public List<ImporteClienteDTO> obtenerClientesConImportes(String filtro) {
        List<ImporteClienteDTO> lista = new ArrayList<>();

        // Agregamos COALESCE para evitar que devuelva ceros o nulos si algún campo viene vacío
        String sql = "SELECT c.id AS cliente_id, c.nombre, c.telefono, "
                + "COALESCE(SUM(vp.cantidad), 0) AS pzas, "
                + "GROUP_CONCAT(CONCAT(COALESCE(vp.cantidad, 1), ' ', p.name) SEPARATOR ', ') AS detalle, "
                + "COALESCE(SUM(p.price * vp.cantidad), 0) AS total_monto "
                + "FROM ventas v "
                + "JOIN clientes c ON v.cliente_id = c.id "
                + "JOIN venta_productos vp ON v.id = vp.registro_venta_id "
                + "JOIN productos p ON vp.producto_id = p.id "
                + "WHERE LOWER(p.name) LIKE '%importe%' ";

        if (filtro != null && !filtro.trim().isEmpty()) {
            sql += "AND (LOWER(c.nombre) LIKE ? OR c.telefono LIKE ?) ";
        }
        sql += "GROUP BY c.id, c.nombre, c.telefono";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (filtro != null && !filtro.trim().isEmpty()) {
                pstmt.setString(1, "%" + filtro.trim().toLowerCase() + "%");
                pstmt.setString(2, "%" + filtro.trim().toLowerCase() + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(new ImporteClienteDTO(
                        rs.getLong("cliente_id"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getInt("pzas"),
                        rs.getString("detalle"),
                        rs.getBigDecimal("total_monto")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener clientes con importes: " + e.getMessage());
        }
        return lista;
    }
    @Transactional
    public void abonarOLiquidarImporte(Long clienteId, BigDecimal montoAbono) throws Exception {
        String sqlEliminarImporte = "DELETE vp FROM venta_productos vp "
                + "JOIN ventas v ON vp.registro_venta_id = v.id "
                + "JOIN productos p ON vp.producto_id = p.id "
                + "WHERE v.cliente_id = ? AND LOWER(p.name) LIKE '%importe%'";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlEliminarImporte)) {
            pstmt.setLong(1, clienteId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error al actualizar la base de datos al abonar el importe: " + e.getMessage());
        }

        try {
            MovimientoCajaDto mov = new MovimientoCajaDto();
            mov.setTipo(TipoMovimiento.DEVOLUCION);
            mov.setMonto(montoAbono != null ? montoAbono.doubleValue() : 0.0);
            mov.setConcepto("Devolución de Importe - Cliente ID: " + clienteId);
            mov.setUsuario("ADMIN");

            double montoNegativo = montoAbono != null ? -montoAbono.doubleValue() : 0.0;
            cajaService.registrarMovimiento(1, mov, montoNegativo, false);
        } catch (Exception e) {
            System.err.println("[ADVERTENCIA] No se pudo registrar la devolución en la caja: " + e.getMessage());
        }
    }
}
