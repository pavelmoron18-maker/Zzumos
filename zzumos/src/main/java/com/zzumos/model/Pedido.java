package com.zzumos.model;

import com.zzumos.model.enums.EstadoPedido;
import com.zzumos.model.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "pedido")
@Getter @Setter @NoArgsConstructor
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPedido;

    @Column(nullable = false, length = 12, unique = true)
    private String codigoPedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_atencion")
    private Usuario usuarioAtencion;   // null hasta que el personal lo atiende

    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    @Column(length = 200) private String direccionEntrega;
    @Column(length = 120) private String referencia;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist void antesDeGuardar() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
    }
}
