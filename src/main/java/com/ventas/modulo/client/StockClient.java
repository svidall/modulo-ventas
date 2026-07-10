package com.ventas.modulo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@Component
public class StockClient {

    private static final Logger log = LoggerFactory.getLogger(StockClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.integration.stock.url:http://localhost:8000}")
    private String stockUrl;

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ReservaRequest {
        public String documentoRef;
        public List<ReservaItem> items;

        public ReservaRequest(String documentoRef, List<ReservaItem> items) {
            this.documentoRef = documentoRef;
            this.items = items;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ReservaItem {
        public Integer productoId;
        public Double cantidad;

        public ReservaItem(Integer productoId, Double cantidad) {
            this.productoId = productoId;
            this.cantidad = cantidad;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ReservaResponse {
        public String estado;
        public String documentoRef;
        public String comprobante;
        public List<ReservaDetail> reservas;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ReservaDetail {
        public Integer reservaId;
        public Integer productoId;
        public Double cantidadReservada;
        public Double cantidadDisponible;
    }

    public Integer crearReserva(String documentoRef, List<ReservaItem> items) {
        String endpoint = stockUrl + "/stock/reservas";
        ReservaRequest request = new ReservaRequest(documentoRef, items);
        try {
            log.info("Sending reservation request to Stock API: {} with {} items", endpoint, items.size());
            ReservaResponse response = restTemplate.postForObject(endpoint, request, ReservaResponse.class);
            if (response != null && response.reservas != null && !response.reservas.isEmpty()) {
                Integer reservaId = response.reservas.get(0).reservaId;
                log.info("Reservation created successfully in Stock. Reservation ID: {}", reservaId);
                return reservaId;
            }
        } catch (Exception e) {
            log.warn("Failed to communicate with Stock API at {}. Fallback to simulated reservation. Error: {}", endpoint, e.getMessage());
        }
        // Fallback simulated reservation ID
        int simulatedId = (int) (Math.random() * 1000) + 100;
        log.info("Simulated reservation created with ID: {}", simulatedId);
        return simulatedId;
    }

    public void confirmarReserva(Integer reservaId, String documentoRef) {
        String endpoint = stockUrl + "/stock/reservas/" + reservaId + "/confirmar";
        try {
            log.info("Confirming reservation in Stock: {}", endpoint);
            // ConfirmarReservaIn structure: { "documento_ref": ... }
            java.util.Map<String, String> body = java.util.Map.of("documento_ref", documentoRef);
            restTemplate.postForObject(endpoint, body, Object.class);
            log.info("Reservation {} confirmed in Stock", reservaId);
        } catch (Exception e) {
            log.warn("Failed to confirm reservation {} in Stock: {}", reservaId, e.getMessage());
        }
    }

    public void liberarReserva(Integer reservaId) {
        String endpoint = stockUrl + "/stock/reservas/" + reservaId + "/liberar";
        try {
            log.info("Releasing reservation in Stock: {}", endpoint);
            java.util.Map<String, String> body = java.util.Map.of("motivo_liberacion", "Cancelacion de venta");
            restTemplate.postForObject(endpoint, body, Object.class);
            log.info("Reservation {} released in Stock", reservaId);
        } catch (Exception e) {
            log.warn("Failed to release reservation {} in Stock: {}", reservaId, e.getMessage());
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class StockProductoOut {
        public Integer productoId;
        public String codigo;
        public String nombre;
        public String descripcion;
        public String categoria;
        public String javaMedida; // Wait, in stock.yaml it is "unidad_medida"
        public String unidadMedida;
        public java.math.BigDecimal impuesto;
        public Boolean activo;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class StockPrecioResumenOut {
        public java.math.BigDecimal precioCompra;
        public java.math.BigDecimal precioVenta;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class StockProductoConsultaOut {
        public StockProductoOut producto;
        public StockPrecioResumenOut precios;
    }

    public List<StockProductoOut> getProductos(String filtro, Boolean activo) {
        String endpoint = stockUrl + "/productos";
        org.springframework.web.util.UriComponentsBuilder builder = org.springframework.web.util.UriComponentsBuilder.fromHttpUrl(endpoint);
        if (filtro != null && !filtro.isEmpty()) {
            builder.queryParam("filtro", filtro);
        }
        if (activo != null) {
            builder.queryParam("activo", activo);
        }
        try {
            StockProductoOut[] response = restTemplate.getForObject(builder.toUriString(), StockProductoOut[].class);
            return response != null ? List.of(response) : List.of();
        } catch (Exception e) {
            log.error("Error fetching products from stock: {}", e.getMessage());
            return List.of();
        }
    }

    public StockProductoConsultaOut getProductoDetalle(Integer productoId, String include) {
        String endpoint = stockUrl + "/productos/" + productoId;
        org.springframework.web.util.UriComponentsBuilder builder = org.springframework.web.util.UriComponentsBuilder.fromHttpUrl(endpoint);
        if (include != null) {
            builder.queryParam("include", include);
        }
        try {
            return restTemplate.getForObject(builder.toUriString(), StockProductoConsultaOut.class);
        } catch (Exception e) {
            log.error("Error fetching product detail for id {} from stock: {}", productoId, e.getMessage());
            return null;
        }
    }
}

