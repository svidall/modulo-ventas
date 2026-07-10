package com.ventas.modulo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "cliente_direcciones", schema = "public")
@IdClass(ClienteDireccionId.class)
@Getter
@Setter
public class ClienteDireccion {

    @Id
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Integer idDireccion;

    @Column(name = "tipo", length = 20)
    private String tipo;

    @Column(name = "calle_principal", length = 100)
    private String callePrincipal;

    @Column(name = "calle_secundaria", length = 100)
    private String calleSecundaria;

    @Column(name = "numero_casa", length = 10)
    private String numeroCasa;

    @Column(name = "barrio", length = 50)
    private String barrio;

    @Column(name = "ciudad", length = 50)
    private String ciudad;

    @Column(name = "latitud", precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "referencia", columnDefinition = "text")
    private String referencia;
}
