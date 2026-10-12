package com.mycompany.chancuellarpuntodeventa.services.repository;

import com.mycompany.chancuellarpuntodeventa.services.querys.RegistroVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<RegistroVenta, Long> {
}