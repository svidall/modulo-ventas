package com.ventas.modulo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cliente_contactos", schema = "public")
@Getter
@Setter
public class ClienteContacto {

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contacto")
    private Integer idContacto;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "cargo", length = 50)
    private String cargo;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "es_principal")
    private Boolean esPrincipal = false;
}
