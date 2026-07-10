package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class VentaListaResponse {
    private Integer idVenta;
    private String numeroFactura;
    private LocalDateTime fechaEmision;
    private String cliente;
    private Boolean condicionVenta;
    private BigDecimal montoTotal;
    private String estado;
}
