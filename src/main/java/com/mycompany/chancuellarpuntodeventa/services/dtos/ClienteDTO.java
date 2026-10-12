
package com.mycompany.chancuellarpuntodeventa.services.dtos;


import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
public class ClienteDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String telefono;
    private String direccion;

    @Column(name = "limite_credito")
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "deuda_actual")
    private BigDecimal deudaActual = BigDecimal.ZERO;

    private String estado = "ACTIVO";

    public ClienteDTO() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal limiteCredito) { this.limiteCredito = limiteCredito; }

    public BigDecimal getDeudaActual() { return deudaActual; }
    public void setDeudaActual(BigDecimal deudaActual) { this.deudaActual = deudaActual; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
