package com.ventas.modulo.service;

import com.ventas.modulo.client.StockClient;
import com.ventas.modulo.dto.ProductoDetalleResponse;
import com.ventas.modulo.dto.ProductoPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private StockClient stockClient;

    public ProductoPage listarProductos(String nombre, int page, int size) {
        List<StockClient.StockProductoOut> allProducts = stockClient.getProductos(null, true);
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            String searchLower = nombre.toLowerCase();
            allProducts = allProducts.stream()
                    .filter(p -> p.nombre != null && p.nombre.toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        int totalItems = allProducts.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalItems);

        List<StockClient.StockProductoOut> paginatedList;
        if (fromIndex >= totalItems || fromIndex < 0) {
            paginatedList = List.of();
        } else {
            paginatedList = allProducts.subList(fromIndex, toIndex);
        }

        List<ProductoDetalleResponse> items = paginatedList.stream().map(p -> {
            StockClient.StockProductoConsultaOut detail = stockClient.getProductoDetalle(p.productoId, "producto,precios");
            java.math.BigDecimal price = java.math.BigDecimal.ZERO;
            if (detail != null && detail.precios != null && detail.precios.precioVenta != null) {
                price = detail.precios.precioVenta;
            }
            ProductoDetalleResponse res = new ProductoDetalleResponse();
            res.setIdProducto(p.productoId);
            res.setCodigoInterno(p.codigo);
            res.setNombre(p.nombre);
            res.setDescripcion(p.descripcion);
            res.setPrecioUnitario(price);
            res.setTipoImpuesto(mapImpuesto(p.impuesto));
            res.setActivo(p.activo);
            return res;
        }).collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (totalPages == 0) {
            totalPages = 1;
        }

        ProductoPage resPage = new ProductoPage();
        resPage.setItems(items);
        resPage.setPage(page);
        resPage.setSize(size);
        resPage.setTotalItems((long) totalItems);
        resPage.setTotalPages(totalPages);
        resPage.setHasNext(page < totalPages - 1);
        resPage.setHasPrevious(page > 0);
        return resPage;
    }

    public ProductoDetalleResponse obtenerProductoPorId(Integer idProducto) {
        StockClient.StockProductoConsultaOut detail = stockClient.getProductoDetalle(idProducto, "producto,precios");
        if (detail == null || detail.producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + idProducto);
        }
        StockClient.StockProductoOut p = detail.producto;
        java.math.BigDecimal price = java.math.BigDecimal.ZERO;
        if (detail.precios != null && detail.precios.precioVenta != null) {
            price = detail.precios.precioVenta;
        }
        ProductoDetalleResponse res = new ProductoDetalleResponse();
        res.setIdProducto(p.productoId);
        res.setCodigoInterno(p.codigo);
        res.setNombre(p.nombre);
        res.setDescripcion(p.descripcion);
        res.setPrecioUnitario(price);
        res.setTipoImpuesto(mapImpuesto(p.impuesto));
        res.setActivo(p.activo);
        return res;
    }

    private String mapImpuesto(java.math.BigDecimal impuesto) {
        if (impuesto == null) {
            return "EXENTA";
        }
        int val = impuesto.intValue();
        if (val == 10) {
            return "IVA_10";
        } else if (val == 5) {
            return "IVA_5";
        } else {
            return "EXENTA";
        }
    }
}

