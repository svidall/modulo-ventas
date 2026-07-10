package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class VentaCrearDetalleRequest {
    private Integer idProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
