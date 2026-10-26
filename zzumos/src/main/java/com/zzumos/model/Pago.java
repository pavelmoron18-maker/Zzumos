package com.zzumos.model;

import com.zzumos.model.enums.EstadoPago;
import com.zzumos.model.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "pago")
@Getter @Setter @NoArgsConstructor
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPago;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venta")
    private Venta venta;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private MetodoPago metodo;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    private LocalDateTime fechaPago;
}
