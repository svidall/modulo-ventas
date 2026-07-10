package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class VentaDetalleCompletoResponse {
    private Integer idVenta;
    private LocalDateTime fechaEmision;
    private String nroTimbrado;
    private String numeroFactura;
    private Boolean condicionVenta;
    private BigDecimal subtotalIva5;
    private BigDecimal subtotalIva10;
    private BigDecimal subtotalExenta;
    private BigDecimal montoTotal;
    private String estado;
    private ClienteListaResponse cliente;
    private List<VentaDetalleLinea> detalles;
}
