package com.ventas.modulo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class VentaCrearDetalleRequest {
    @JsonProperty("id_producto")
    @JsonAlias({"idProducto", "id_producto"})
    private Integer idProducto;

    @JsonProperty("cantidad")
    @JsonAlias({"cantidad"})
    private Integer cantidad;

    @JsonProperty("precio_unitario")
    @JsonAlias({"precioUnitario", "precio_unitario"})
    private BigDecimal precioUnitario;
}
