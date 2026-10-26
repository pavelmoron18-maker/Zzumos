package com.zzumos.repository;

import com.zzumos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    @Query("select distinct p from Producto p left join fetch p.opciones where p.disponible = true order by p.nombre")
    List<Producto> findCatalogo();
}
