package com.ventas.modulo.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventas.modulo.service.VentaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CajaResponseListener {

    private static final Logger log = LoggerFactory.getLogger(CajaResponseListener.class);

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "caja.respuesta", groupId = "modulo-ventas-group")
    public void listenCajaRespuesta(ConsumerRecord<String, String> record) {
        log.info("Received Kafka message from topic 'caja.respuesta' with key: {}", record.key());
        try {
            if ("COBRO".equalsIgnoreCase(record.key())) {
                String payload = record.value();
                log.info("Payload received: {}", payload);

                Map<?, ?> map = objectMapper.readValue(payload, Map.class);
                String tipo = (String) map.get("tipo");
                String idVentaStr = (String) map.get("id_venta");

                if ("COBRO_CONFIRMADO".equalsIgnoreCase(tipo) && idVentaStr != null) {
                    log.info("Processing confirmation for Venta: {}", idVentaStr);
                    Integer idVenta = null;
                    if (idVentaStr.startsWith("VTA-")) {
                        try {
                            idVenta = Integer.parseInt(idVentaStr.substring(4));
                        } catch (NumberFormatException e) {
                            log.warn("id_venta format is not VTA-number: {}", idVentaStr);
                        }
                    }

                    if (idVenta != null) {
                        ventaService.confirmarVenta(idVenta);
                        log.info("Successfully confirmed Venta ID: {}", idVenta);
                    } else {
                        log.warn("Could not extract Venta ID from: {}", idVentaStr);
                    }
                }
            } else {
                log.debug("Skipping message with non-COBRO key: {}", record.key());
            }
        } catch (Exception e) {
            log.error("Error processing Caja response Kafka message", e);
        }
    }
}
