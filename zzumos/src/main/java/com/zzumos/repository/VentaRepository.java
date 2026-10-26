package com.zzumos.repository;

import com.zzumos.model.Pedido;
import com.zzumos.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    boolean existsByPedido(Pedido pedido);
}
