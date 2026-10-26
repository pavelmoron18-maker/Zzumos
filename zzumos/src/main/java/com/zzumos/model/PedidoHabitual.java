package com.zzumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "pedido_habitual")
@Getter @Setter @NoArgsConstructor
public class PedidoHabitual {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPedidoHabitual;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido_base")
    private Pedido pedidoBase;

    private LocalDateTime fechaActualizacion;

    @PrePersist @PreUpdate void marcarFecha() { fechaActualizacion = LocalDateTime.now(); }
}
