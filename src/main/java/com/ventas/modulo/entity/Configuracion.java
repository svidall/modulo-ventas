package com.ventas.modulo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "configuracion", schema = "public")
@Getter
@Setter
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private Integer idConfiguracion;

    @Column(name = "ruc_empresa", length = 20, nullable = false)
    private String rucEmpresa;

    @Column(name = "razon_social_empresa", length = 100)
    private String razonSocialEmpresa;

    @Column(name = "timbrado", length = 20, nullable = false)
    private String timbrado;

    @Column(name = "fecha_inicio_timbrado", nullable = false)
    private LocalDate fechaInicioTimbrado;

    @Column(name = "fecha_fin_timbrado", nullable = false)
    private LocalDate fechaFinTimbrado;

    @Column(name = "codigo_establecimiento", length = 3, nullable = false)
    private String codigoEstablecimiento;

    @Column(name = "codigo_punto_expedicion", length = 3, nullable = false)
    private String codigoPuntoExpedicion;

    @Column(name = "numero_secuencial_actual", nullable = false)
    private Integer numeroSecuencialActual = 1;

    @Column(name = "activo")
    private Boolean activo = true;
}
