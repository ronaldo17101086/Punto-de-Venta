package caja;

import java.time.LocalDateTime;
import java.util.List;

public class CajaDto {

    private String idCaja;
    private String uuid; 
    private String usuarioApertura;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private Double montoInicial = 0.0;   
    private Double montoFinal = 0.0;     
    private Double totalVentas = 0.0;
    private Double totalIngresosExtra = 0.0;
    private Double totalRetiros = 0.0;         
    private Double totalPagosProveedores = 0.0; 
    private Double efectivoEsperado = 0.0;
    private Double efectivoReal = 0.0;
    private Double diferencia = 0.0;
    private String estado; 

    private List<MovimientoCajaDto> movimientos;

    public CajaDto() {
    }

    // --- Getters y Setters ---
    public String getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(String idCaja) {
        this.idCaja = idCaja;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsuarioApertura() {
        return usuarioApertura;
    }

    public void setUsuarioApertura(String usuarioApertura) {
        this.usuarioApertura = usuarioApertura;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Double getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(Double montoInicial) {
        this.montoInicial = montoInicial;
    }

    public Double getMontoFinal() {
        return montoFinal;
    }

    public void setMontoFinal(Double montoFinal) {
        this.montoFinal = montoFinal;
    }

    public Double getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(Double totalVentas) {
        this.totalVentas = totalVentas;
    }

    public Double getTotalIngresosExtra() {
        return totalIngresosExtra;
    }

    public void setTotalIngresosExtra(Double totalIngresosExtra) {
        this.totalIngresosExtra = totalIngresosExtra;
    }

    public Double getTotalRetiros() {
        return totalRetiros;
    }

    public void setTotalRetiros(Double totalRetiros) {
        this.totalRetiros = totalRetiros;
    }

    public Double getTotalPagosProveedores() {
        return totalPagosProveedores;
    }

    public void setTotalPagosProveedores(Double totalPagosProveedores) {
        this.totalPagosProveedores = totalPagosProveedores;
    }

    public Double getEfectivoEsperado() {
        return efectivoEsperado;
    }

    public void setEfectivoEsperado(Double efectivoEsperado) {
        this.efectivoEsperado = efectivoEsperado;
    }

    public Double getEfectivoReal() {
        return efectivoReal;
    }

    public void setEfectivoReal(Double efectivoReal) {
        this.efectivoReal = efectivoReal;
    }

    public Double getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(Double diferencia) {
        this.diferencia = diferencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<MovimientoCajaDto> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<MovimientoCajaDto> movimientos) {
        this.movimientos = movimientos;
    }
}
