package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class VentaCrearRequest {
    private Integer idCliente;
    private Boolean condicionVenta = false;
    private List<VentaCrearDetalleRequest> detalles;
}
