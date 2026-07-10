package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductoDetalleResponse {
    private Integer idProducto;
    private String codigoInterno;
    private String nombre;
    private String descripcion;
    private BigDecimal precioUnitario;
    private String tipoImpuesto; // IVA_5, IVA_10, EXENTA
    private Boolean activo;
}
