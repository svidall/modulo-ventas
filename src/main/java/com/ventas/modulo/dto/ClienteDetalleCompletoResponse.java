package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ClienteDetalleCompletoResponse extends ClienteListaResponse {
    private List<DireccionRequest> direcciones;
    private List<ContactoRequest> contactos;
}
