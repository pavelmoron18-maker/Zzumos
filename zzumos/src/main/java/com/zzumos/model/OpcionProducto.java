package com.zzumos.model;

import com.zzumos.model.enums.Grupo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name = "opcion_producto")
@Getter @Setter @NoArgsConstructor
public class OpcionProducto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOpcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Grupo grupo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precioAdicional = BigDecimal.ZERO;

    private boolean disponible = true;
}
