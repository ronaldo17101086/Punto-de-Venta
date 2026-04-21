package com.mycompany.chancuellarpuntodeventa.services.repository;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ProductoDTO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoDTO, Long> {

    ProductoDTO findBySku(String sku);
}
