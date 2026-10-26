package com.zzumos.repository;

import com.zzumos.model.Pedido;
import com.zzumos.model.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    Optional<Pedido> findByCodigoPedido(String codigoPedido);
    List<Pedido> findByEstadoOrderByFechaHoraDesc(EstadoPedido estado);
    List<Pedido> findAllByOrderByFechaHoraDesc();
    List<Pedido> findByCliente_Usuario_IdUsuarioOrderByFechaHoraDesc(Integer idUsuario);
}
