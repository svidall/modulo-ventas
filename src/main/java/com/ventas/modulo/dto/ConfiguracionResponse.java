package com.ventas.modulo.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ConfiguracionResponse {
    private Integer idConfiguracion;
    private String rucEmpresa;
    private String razonSocialEmpresa;
    private String timbrado;
    private LocalDate fechaInicioTimbrado;
    private LocalDate fechaFinTimbrado;
    private String codigoEstablecimiento;
    private String codigoPuntoExpedicion;
    private Integer numeroSecuencialActual;
    private Boolean activo;
}
