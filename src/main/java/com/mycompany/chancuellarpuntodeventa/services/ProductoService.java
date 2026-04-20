package com.mycompany.chancuellarpuntodeventa.services;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "productos") // Nombre de tu tabla en MySQL
public class ProductoService implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(name = "image_path")
    private String imagePath; // Aquí se guarda la ruta de la foto

    // --- CONSTRUCTORES ---
    public ProductoService() {
    }

    public ProductoService(String sku, String name, Double price) {
        this.sku = sku;
        this.name = name;
        this.price = price;
    }

    // --- GETTERS Y SETTERS (Esenciales para que repo.save funcione) ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
