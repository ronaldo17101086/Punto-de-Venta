package com.mycompany.chancuellarpuntodeventa.services.repository;

import com.mycompany.chancuellarpuntodeventa.services.dtos.Producto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
// Cambia la línea por esta en el Repositorio:

    Producto findBySku(String sku);
}
