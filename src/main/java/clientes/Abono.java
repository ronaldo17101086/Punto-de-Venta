package clientes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Abono {

    private Long id;
    private Long clienteId;
    private Long ventaId;
    private BigDecimal monto;
    private LocalDateTime fechaRegistro;
    private String status;

    public Abono() {
        this.status = "ACTIVO";
        this.fechaRegistro = LocalDateTime.now();
    }

    public Abono(Long clienteId, Long ventaId, BigDecimal monto) {
        this.clienteId = clienteId;
        this.ventaId = ventaId;
        this.monto = monto;
        this.status = "ACTIVO";
        this.fechaRegistro = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
