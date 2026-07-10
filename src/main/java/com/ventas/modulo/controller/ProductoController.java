package com.ventas.modulo.controller;

import com.ventas.modulo.dto.ProductoDetalleResponse;
import com.ventas.modulo.dto.ProductoPage;
import com.ventas.modulo.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ProductoPage listarProductos(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return productoService.listarProductos(nombre, page, size);
    }

    @GetMapping("/{id_producto}")
    public ProductoDetalleResponse obtenerProductoPorId(@PathVariable("id_producto") Integer idProducto) {
        return productoService.obtenerProductoPorId(idProducto);
    }
}
