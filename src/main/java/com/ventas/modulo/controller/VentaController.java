package com.ventas.modulo.controller;

import com.ventas.modulo.dto.VentaCrearRequest;
import com.ventas.modulo.dto.VentaDetalleCompletoResponse;
import com.ventas.modulo.dto.VentaPage;
import com.ventas.modulo.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public VentaPage listarVentas(
            @RequestParam(value = "fecha_desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(value = "fecha_hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "id_cliente", required = false) Integer idCliente,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ventaService.listarVentas(fechaDesde, fechaHasta, estado, idCliente, page, size);
    }

    @PostMapping
    public ResponseEntity<VentaDetalleCompletoResponse> crearVenta(@RequestBody VentaCrearRequest request) {
        VentaDetalleCompletoResponse response = ventaService.crearVenta(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getIdVenta())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id_venta}")
    public VentaDetalleCompletoResponse obtenerVentaPorId(@PathVariable("id_venta") Integer idVenta) {
        return ventaService.obtenerVentaPorId(idVenta);
    }

    @PostMapping("/{id_venta}/confirmar")
    public VentaDetalleCompletoResponse confirmarVenta(@PathVariable("id_venta") Integer idVenta) {
        return ventaService.confirmarVenta(idVenta);
    }

    @PostMapping("/{id_venta}/cancelar")
    public VentaDetalleCompletoResponse cancelarVenta(@PathVariable("id_venta") Integer idVenta) {
        return ventaService.cancelarVenta(idVenta);
    }
}
