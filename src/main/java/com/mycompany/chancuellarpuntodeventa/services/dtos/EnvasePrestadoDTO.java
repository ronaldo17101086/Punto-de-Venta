package com.mycompany.chancuellarpuntodeventa.services.dtos;

public class EnvasePrestadoDTO {

    private Long clienteId;
    private String nombre;
    private String telefono;
    private int piezasPendientes;
    private String detalleEnvases;

    public EnvasePrestadoDTO(Long clienteId, String nombre, String telefono, int piezasPendientes, String detalleEnvases) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.telefono = telefono;
        this.piezasPendientes = piezasPendientes;
        this.detalleEnvases = detalleEnvases;
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

    public int getPiezasPendientes() {
        return piezasPendientes;
    }

    public String getDetalleEnvases() {
        return detalleEnvases;
    }
}
