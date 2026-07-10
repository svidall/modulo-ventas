package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ClienteCrearRequest {
    private String ruc;
    private String ci;
    private String nombre;
    private String apellido;
    private List<DireccionRequest> direcciones;
    private List<ContactoRequest> contactos;
}
