
package com.mycompany.chancuellarpuntodeventa.services.dtos;

import java.math.BigDecimal;

public class CambioDTO {

    private BigDecimal efectivoRecibido;
    private BigDecimal totalVenta;
    private BigDecimal cambioDevuelto;
    private boolean requiereCambio;

    public CambioDTO() {
        this.efectivoRecibido = BigDecimal.ZERO;
        this.totalVenta = BigDecimal.ZERO;
        this.cambioDevuelto = BigDecimal.ZERO;
        this.requiereCambio = false;
    }

    public CambioDTO(BigDecimal efectivoRecibido, BigDecimal totalVenta) {
        this.efectivoRecibido = efectivoRecibido != null ? efectivoRecibido : BigDecimal.ZERO;
        this.totalVenta = totalVenta != null ? totalVenta : BigDecimal.ZERO;
        
        // Cálculo exacto del cambio (Efectivo - Total)
        if (this.efectivoRecibido.compareTo(this.totalVenta) > 0) {
            this.cambioDevuelto = this.efectivoRecibido.subtract(this.totalVenta);
            this.requiereCambio = true;
        } else {
            this.cambioDevuelto = BigDecimal.ZERO;
            this.requiereCambio = false;
        }
    }

    // --- GETTERS Y SETTERS ---

    public BigDecimal getEfectivoRecibido() {
        return efectivoRecibido;
    }

    public void setEfectivoRecibido(BigDecimal efectivoRecibido) {
        this.efectivoRecibido = efectivoRecibido;
    }

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }

    public BigDecimal getCambioDevuelto() {
        return cambioDevuelto;
    }

    public void setCambioDevuelto(BigDecimal cambioDevuelto) {
        this.cambioDevuelto = cambioDevuelto;
    }

    public boolean isRequiereCambio() {
        return requiereCambio;
    }

    public void setRequiereCambio(boolean requiereCambio) {
        this.requiereCambio = requiereCambio;
    }
}