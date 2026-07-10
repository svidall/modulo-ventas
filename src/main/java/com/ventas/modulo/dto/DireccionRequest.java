package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class DireccionRequest {
    private Integer idDireccion; // Read-only in responses
    private String tipo;
    private String callePrincipal;
    private String calleSecundaria;
    private String numeroCasa;
    private String barrio;
    private String ciudad;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String referencia;
}
