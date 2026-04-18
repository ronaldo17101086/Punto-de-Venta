package com.mycompany.chancuellarpuntodeventa.services.controller;

import com.mycompany.chancuellarpuntodeventa.services.dtos.Producto;
import com.mycompany.chancuellarpuntodeventa.services.repository.ProductoRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class ProductoController {

    private final ProductoRepository repository;

    public ProductoController(ProductoRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public List<Producto> listarProductos() {
        return repository.findAll();
    }
}
