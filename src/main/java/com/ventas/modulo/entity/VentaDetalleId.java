package com.ventas.modulo.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VentaDetalleId implements Serializable {
    private Integer idVenta;
    private Integer idDetalle;
}
