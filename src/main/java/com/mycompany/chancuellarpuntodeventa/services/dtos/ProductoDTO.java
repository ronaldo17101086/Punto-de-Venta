package com.mycompany.chancuellarpuntodeventa.services.dtos;

import jakarta.persistence.Column; // Importante agregar este
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class ProductoDTO {

    public ProductoDTO() {
    }

    public ProductoDTO(String sku, String name, Double price, String imagePath) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;
    private String name;
    private Double price;

    @Column(name = "image_path") // Coincide con el nombre de la columna en MySQL
    private String imagePath;

    // --- GETTERS Y SETTERS ---

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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    // Nuevo Getter para la imagen
    public String getImagePath() {
        return imagePath;
    }

    // Nuevo Setter para la imagen
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
