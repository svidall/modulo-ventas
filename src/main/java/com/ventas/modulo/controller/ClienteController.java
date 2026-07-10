package com.ventas.modulo.controller;

import com.ventas.modulo.dto.ClienteCrearRequest;
import com.ventas.modulo.dto.ClienteDetalleCompletoResponse;
import com.ventas.modulo.dto.ClientePage;
import com.ventas.modulo.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public ClientePage listarClientes(
            @RequestParam(value = "ruc", required = false) String ruc,
            @RequestParam(value = "ci", required = false) String ci,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "apellido", required = false) String apellido,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return clienteService.listarClientes(ruc, ci, nombre, apellido, page, size);
    }

    @PostMapping
    public ResponseEntity<ClienteDetalleCompletoResponse> crearCliente(@RequestBody ClienteCrearRequest request) {
        ClienteDetalleCompletoResponse response = clienteService.crearCliente(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getIdCliente())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id_cliente}")
    public ClienteDetalleCompletoResponse obtenerClientePorId(@PathVariable("id_cliente") Integer idCliente) {
        return clienteService.obtenerClientePorId(idCliente);
    }
}
