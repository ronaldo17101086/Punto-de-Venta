package caja;

import java.time.LocalDateTime;

public class MovimientoCajaDto {

    private String id;
    private String cajaId;
    private TipoMovimiento tipo;
    private Double monto;
    private String concepto;
    private String referencia;
    private String proveedorId;
    private String nombreProveedor;
    private String usuario;
    private LocalDateTime fecha;
    private TipoMovimiento metodoPago;
    private String observaciones;

    public MovimientoCajaDto() {
    }

    public MovimientoCajaDto(TipoMovimiento tipo, Double monto, String concepto, String usuario, TipoMovimiento metodoPago) {
        this.tipo = tipo;
        this.monto = monto;
        this.concepto = concepto;
        this.usuario = usuario;
        this.metodoPago = metodoPago;
        this.fecha = LocalDateTime.now();
    }

    public boolean esEntrada() {
        return tipo == TipoMovimiento.VENTA
                || tipo == TipoMovimiento.APERTURA
                || tipo == TipoMovimiento.INGRESO_EXTRA;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCajaId() {
        return cajaId;
    }

    public void setCajaId(String cajaId) {
        this.cajaId = cajaId;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoMovimiento getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(TipoMovimiento metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "Movimiento[" + tipo + ": " + monto + " - " + concepto + "]";
    }
}
