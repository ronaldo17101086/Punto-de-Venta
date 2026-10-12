    package com.mycompany.chancuellarpuntodeventa.services.dtos;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.util.List;
    import java.util.Map;

    public class VentaDTO {

        // Atributos base de la venta
        private String uuidVenta;
        private Long clienteId;
        private BigDecimal totalVenta;
        private boolean esCredito;
        private String comentarios;
        private String tipoPago;

        // --- NUEVOS ATRIBUTOS DE FECHA Y HORA ---
        private LocalDate fechaVenta;
        private LocalTime horaVenta;

        // --- NODOS INTEGRADOS ---
        private CambioDTO cambio;                    // Nodo para el cálculo y control del cambio
        private ClienteDTO cliente;                  // Nodo del cliente completo
        private List<ProductoDTO> productos;         // Nodo de la lista de productos
        private Map<String, BigDecimal> formasPagoMap; // Nodo de desglose de formas de pago

        // --- GETTERS Y SETTERS ---

        public String getUuidVenta() {
            return uuidVenta;
        }

        public void setUuidVenta(String uuidVenta) {
            this.uuidVenta = uuidVenta;
        }

        public Long getClienteId() {
            return clienteId;
        }

        public void setClienteId(Long clienteId) {
            this.clienteId = clienteId;
        }

        public BigDecimal getTotalVenta() {
            return totalVenta;
        }

        public void setTotalVenta(BigDecimal totalVenta) {
            this.totalVenta = totalVenta;
        }

        public boolean isEsCredito() {
            return esCredito;
        }

        public void setEsCredito(boolean esCredito) {
            this.esCredito = esCredito;
        }

        public String getComentarios() {
            return comentarios;
        }

        public void setComentarios(String comentarios) {
            this.comentarios = comentarios;
        }

        public String getTipoPago() {
            return tipoPago;
        }

        public void setTipoPago(String tipoPago) {
            this.tipoPago = tipoPago;
        }

        public LocalDate getFechaVenta() {
            return fechaVenta;
        }

        public void setFechaVenta(LocalDate fechaVenta) {
            this.fechaVenta = fechaVenta;
        }

        public LocalTime getHoraVenta() {
            return horaVenta;
        }

        public void setHoraVenta(LocalTime horaVenta) {
            this.horaVenta = horaVenta;
        }

        public CambioDTO getCambio() {
            return cambio;
        }

        public void setCambio(CambioDTO cambio) {
            this.cambio = cambio;
        }

        public ClienteDTO getCliente() {
            return cliente;
        }

        public void setCliente(ClienteDTO cliente) {
            this.cliente = cliente;
        }

        public List<ProductoDTO> getProductos() {
            return productos;
        }

        public void setProductos(List<ProductoDTO> productos) {
            this.productos = productos;
        }

        public Map<String, BigDecimal> getFormasPagoMap() {
            return formasPagoMap;
        }

        public void setFormasPagoMap(Map<String, BigDecimal> formasPagoMap) {
            this.formasPagoMap = formasPagoMap;
        }
    }