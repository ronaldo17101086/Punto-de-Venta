    package com.mycompany.chancuellarpuntodeventa.services.repository;

import com.mycompany.chancuellarpuntodeventa.services.dtos.ClienteDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteDTO, Long> {

    @Query("SELECT c FROM ClienteDTO c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR c.telefono LIKE CONCAT('%', :filtro, '%')")
    List<ClienteDTO> buscarPorNombreOTelefono(@Param("filtro") String filtro);

    @Modifying
    @Transactional
    @Query("UPDATE ClienteDTO c SET c.deudaActual = :nuevaDeuda WHERE c.id = :id")
    void actualizarDeudaPorId(@Param("id") Long id, @Param("nuevaDeuda") BigDecimal nuevaDeuda);
}