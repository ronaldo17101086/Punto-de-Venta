package com.mycompany.chancuellarpuntodeventa.services.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class ProductoDTO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "is_granel")
    private boolean granel;

    // --- LA VARIABLE DE CANTIDAD QUE FALTABA DECLARAR BIEN EN EL CÓDIGO ---
    @Column(name = "cantidad")
    private BigDecimal cantidad = BigDecimal.ONE;

    public ProductoDTO() {
    }

    public ProductoDTO(Long id, String sku, String name, BigDecimal price, String imagePath, boolean granel) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.granel = granel;
    }

    // --- GETTERS Y SETTERS ---
    
    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isGranel() {
        return granel;
    }

    public void setGranel(boolean granel) {
        this.granel = granel;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}