package com.zzumos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name = "detalle_pedido_opcion")
@Getter @Setter @NoArgsConstructor
public class DetallePedidoOpcion {
    @EmbeddedId
    private DetallePedidoOpcionId id = new DetallePedidoOpcionId();

    @MapsId("idDetalle")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_detalle")
    private DetallePedido detalle;

    @MapsId("idOpcion")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion")
    private OpcionProducto opcion;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precioAdicional;  // precio vigente al pedir
}
