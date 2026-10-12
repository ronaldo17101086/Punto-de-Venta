package com.mycompany.chancuellarpuntodeventa.services.querys;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ClienteDTO;
import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import com.mycompany.chancuellarpuntodeventa.services.dtos.VentaDTO;
import com.mycompany.chancuellarpuntodeventa.services.repository.ClienteRepository;
import com.mycompany.chancuellarpuntodeventa.services.repository.VentaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Transactional
    public String registrarVentaCompleta(VentaDTO dto) throws Exception {

        // 1. Validar que el carrito no esté vacío
        List<ProductoDTO> productos = dto.getProductos(); // Usando directamente ProductoTable
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

        // 3. Si hay pago a crédito, actualizar la deuda del cliente en la BD
        BigDecimal montoCredito = (formasPagoMap != null) ? formasPagoMap.getOrDefault("CREDITO", BigDecimal.ZERO) : BigDecimal.ZERO;
        if (montoCredito.compareTo(BigDecimal.ZERO) > 0) {
            if (cliente == null || cliente.getId() == null) {
                throw new Exception("Debe seleccionar un cliente registrado para realizar una venta a crédito.");
            }

            BigDecimal deudaActual = cliente.getDeudaActual() != null ? cliente.getDeudaActual() : BigDecimal.ZERO;
            BigDecimal nuevaDeuda = deudaActual.add(montoCredito);

            clienteRepository.actualizarDeudaPorId(cliente.getId(), nuevaDeuda);
        }

        // 4. Determinar tipo de pago principal si no viene seteado
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

        // Asegurar fechas y horas por seguridad
        if (dto.getFechaVenta() == null) {
            dto.setFechaVenta(LocalDate.now());
        }
        if (dto.getHoraVenta() == null) {
            dto.setHoraVenta(LocalTime.now());
        }

        // ===================================================================
        // 5. REGISTRO FÍSICO EN LA TABLA DE VENTAS
        // ===================================================================
        RegistroVenta venta = new RegistroVenta();
        
        venta.setUuidVenta(dto.getUuidVenta());
        venta.setClienteId(dto.getCliente() != null ? dto.getCliente().getId() : null);
        venta.setTotal(dto.getTotalVenta());
        venta.setTipoPago(dto.getTipoPago());
        venta.setEsCredito(dto.isEsCredito());
        venta.setComentarios(dto.getComentarios());
        venta.setFechaVenta(dto.getFechaVenta());
        venta.setHoraVenta(dto.getHoraVenta());

        if (dto.getCambio() != null) {
            venta.setEfectivoRecibido(dto.getCambio().getEfectivoRecibido());
            venta.setCambioDevuelto(dto.getCambio().getCambioDevuelto());
        } else {
            venta.setEfectivoRecibido(BigDecimal.ZERO);
            venta.setCambioDevuelto(BigDecimal.ZERO);
        }

        // Asignamos la lista de productos directamente sin buscar IDs uno por uno
        venta.setProductos(productos);

        // Guardar la venta en la base de datos
        ventaRepository.save(venta); 

        return folio;
    }
}