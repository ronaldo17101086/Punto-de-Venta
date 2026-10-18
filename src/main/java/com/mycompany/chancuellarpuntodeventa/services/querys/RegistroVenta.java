package com.mycompany.chancuellarpuntodeventa.services.querys;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ventas")
public class RegistroVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String uuidVenta;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false, length = 30)
    private String tipoPago;

    @Column(nullable = false)
    private boolean esCredito;

    @Column(columnDefinition = "TEXT")
    private String comentarios;

    @Column(nullable = false)
    private LocalDate fechaVenta;

    @Column(nullable = false)
    private LocalTime horaVenta;

    @Column(name = "efectivo_recibido")
    private BigDecimal efectivoRecibido;

    @Column(name = "cambio_devuelto")
    private BigDecimal cambioDevuelto;

    @PrePersist
    public void prePersist() {
        if (this.fechaVenta == null) {
            this.fechaVenta = LocalDate.now();
        }
        if (this.horaVenta == null) {
            this.horaVenta = LocalTime.now();
        }
        if (this.uuidVenta == null || this.uuidVenta.isEmpty()) {
            this.uuidVenta = "NV-" + System.currentTimeMillis();
        }
    }

    public RegistroVenta() {
    }

    // --- GETTERS Y SETTERS ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
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

    public BigDecimal getEfectivoRecibido() {
        return efectivoRecibido;
    }

    public void setEfectivoRecibido(BigDecimal efectivoRecibido) {
        this.efectivoRecibido = efectivoRecibido;
    }

    public BigDecimal getCambioDevuelto() {
        return cambioDevuelto;
    }

    public void setCambioDevuelto(BigDecimal cambioDevuelto) {
        this.cambioDevuelto = cambioDevuelto;
    }
}
