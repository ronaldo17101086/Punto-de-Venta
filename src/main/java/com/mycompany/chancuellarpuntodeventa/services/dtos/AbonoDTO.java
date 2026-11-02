package com.mycompany.chancuellarpuntodeventa.services.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AbonoDTO {

    private Long id;
    private Long clienteId;
    private String nombreCliente;
    private Long ventaId;
    private String folioVenta; // <-- Campo agregado para el folio de la venta (ej. NV-179...)
    private BigDecimal monto;
    private LocalDateTime fechaRegistro;
    private String status;

    public AbonoDTO() {
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

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public String getFolioVenta() {
        return folioVenta;
    }

    public void setFolioVenta(String folioVenta) {
        this.folioVenta = folioVenta;
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