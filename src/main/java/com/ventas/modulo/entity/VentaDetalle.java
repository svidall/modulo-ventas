package com.ventas.modulo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "ventas_detalle", schema = "public")
@Getter
@Setter
public class VentaDetalle {

    @Column(name = "id_venta")
    private Integer idVenta;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "iva_5", precision = 12, scale = 2)
    private BigDecimal iva5 = BigDecimal.ZERO;

    @Column(name = "iva_10", precision = 12, scale = 2)
    private BigDecimal iva10 = BigDecimal.ZERO;

    @Column(name = "exenta", precision = 12, scale = 2)
    private BigDecimal exenta = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;
}
