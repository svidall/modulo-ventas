package com.ventas.modulo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class VentaCrearRequest {
    @JsonProperty("id_cliente")
    @JsonAlias({"idCliente", "id_cliente"})
    private Integer idCliente;

    @JsonProperty("condicion_venta")
    @JsonAlias({"condicionVenta", "condicion_venta"})
    private Boolean condicionVenta = false;

    @JsonProperty("detalles")
    @JsonAlias({"detalles"})
    private List<VentaCrearDetalleRequest> detalles;
}
