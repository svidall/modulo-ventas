package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactoRequest {
    private Integer idContacto; // Read-only in responses
    private String nombre;
    private String cargo;
    private String telefono;
    private String email;
    private Boolean esPrincipal = false;
}
