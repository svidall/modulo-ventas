package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class VentaDetalleLinea {
    private Integer idDetalle;
    private Integer idProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal iva5;
    private BigDecimal iva10;
    private BigDecimal exenta;
    private BigDecimal subtotal;
}
