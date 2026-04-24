package com.mycompany.chancuellarpuntodeventa.services.dtos;

import jakarta.persistence.Column; 
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class ProductoDTO {

    public ProductoDTO() {
    }

    public ProductoDTO(String sku, String name, BigDecimal price, String imagePath, boolean granel) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.granel = granel;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;
    private String name;
    private BigDecimal price;

    @Column(name = "image_path") // Coincide con el nombre de la columna en MySQL
    private String imagePath;

    @Column(name = "is_granel") // O como se llame en tu tabla
    private boolean granel;

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

    public java.math.BigDecimal getPrice() {
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
