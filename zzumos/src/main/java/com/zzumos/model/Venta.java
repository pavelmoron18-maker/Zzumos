package com.zzumos.model;

import com.zzumos.model.enums.EstadoVenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "venta")
@Getter @Setter @NoArgsConstructor
public class Venta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVenta;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    private LocalDateTime fechaVenta;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EstadoVenta estado = EstadoVenta.REGISTRADA;

    @PrePersist void antesDeGuardar() {
        if (fechaVenta == null) fechaVenta = LocalDateTime.now();
    }
}
