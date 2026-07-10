package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ClienteListaResponse {
    private Integer idCliente;
    private String ruc;
    private String ci;
    private String nombre;
    private String apellido;
    private LocalDateTime fechaRegistro;
}
