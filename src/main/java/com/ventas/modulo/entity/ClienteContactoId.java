package com.ventas.modulo.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClienteContactoId implements Serializable {
    private Integer idCliente;
    private Integer idContacto;
}
