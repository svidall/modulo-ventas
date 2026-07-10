package com.ventas.modulo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas", schema = "public")
@Getter
@Setter
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id_cliente", insertable = false, updatable = false)
    private Cliente cliente;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "nro_timbrado", length = 20)
    private String nroTimbrado;

    @Column(name = "numero_factura", length = 15)
    private String numeroFactura;

    @Column(name = "condicion_venta")
    private Boolean condicionVenta = false; // false = Contado, true = Crédito

    @Column(name = "subtotal_iva5", precision = 12, scale = 2)
    private BigDecimal subtotalIva5 = BigDecimal.ZERO;

    @Column(name = "subtotal_iva10", precision = 12, scale = 2)
    private BigDecimal subtotalIva10 = BigDecimal.ZERO;

    @Column(name = "subtotal_exenta", precision = 12, scale = 2)
    private BigDecimal subtotalExenta = BigDecimal.ZERO;

    @Column(name = "monto_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "estado", length = 20)
    private String estado = "PENDIENTE"; // PENDIENTE, COMPLETADA, CANCELADA, FALLIDA

    @Column(name = "id_reserva")
    private Integer idReserva;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", referencedColumnName = "id_venta")
    private List<VentaDetalle> detalles = new ArrayList<>();
}
