package com.ventas.modulo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CajaClient {

    private static final Logger log = LoggerFactory.getLogger(CajaClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.integration.caja.mode:KAFKA}")
    private String mode; // QUEUE, REST, or KAFKA

    @Value("${app.integration.caja.url:http://localhost:8081}")
    private String cajaUrl;

    @Value("${app.integration.caja.queue-name:caja.solicitudes}")
    private String queueName;

    public static class SolicitudCobroMsg {
        public String tipo = "SOLICITUD_COBRO";
        public String id_venta;
        public Integer id_reserva;
        public String documento_ref;
        public List<ItemMsg> items;
        public String cliente;
        public String concepto;
        public Double monto;

        public static class ItemMsg {
            public Integer producto_id;
            public Integer cantidad;
            public Double precio_venta;

            public ItemMsg() {}

            public ItemMsg(Integer productoId, Integer cantidad, Double precioVenta) {
                this.producto_id = productoId;
                this.cantidad = cantidad;
                this.precio_venta = precioVenta;
            }
        }

        public SolicitudCobroMsg() {}

        public SolicitudCobroMsg(String idVenta, Integer idReserva, String documentoRef, List<ItemMsg> items, String cliente, String concepto, Double monto) {
            this.id_venta = idVenta;
            this.id_reserva = idReserva;
            this.documento_ref = documentoRef;
            this.items = items;
            this.cliente = cliente;
            this.concepto = concepto;
            this.monto = monto;
        }
    }

    public void enviarSolicitudCobro(Integer idVenta, Integer idReserva, String documentoRef, List<SolicitudCobroMsg.ItemMsg> items, String clienteName, String concepto, BigDecimal monto) {
        String idVentaStr = "VTA-" + idVenta;
        SolicitudCobroMsg msg = new SolicitudCobroMsg(
                idVentaStr,
                idReserva,
                documentoRef,
                items,
                clienteName,
                concepto,
                monto != null ? monto.doubleValue() : 0.0
        );

        if ("KAFKA".equalsIgnoreCase(mode) && kafkaTemplate != null) {
            try {
                log.info("Sending SolicitudCobro message to Kafka topic '{}' for Venta: {}", queueName, idVentaStr);
                kafkaTemplate.send(queueName, "VENTA", msg);
                log.info("Successfully published message to Kafka: {}", msg);
            } catch (Exception e) {
                log.error("Failed to send message to Kafka: {}. Falling back.", e.getMessage());
            }
        } else if ("QUEUE".equalsIgnoreCase(mode) && jmsTemplate != null) {
            try {
                log.info("Sending SolicitudCobro message to ActiveMQ queue '{}' for Venta: {}", queueName, idVentaStr);
                Map<String, Object> mapMsg = new HashMap<>();
                mapMsg.put("tipo", msg.tipo);
                mapMsg.put("id_venta", msg.id_venta);
                mapMsg.put("id_reserva", msg.id_reserva);
                mapMsg.put("documento_ref", msg.documento_ref);
                
                List<Map<String, Object>> itemsList = new ArrayList<>();
                if (msg.items != null) {
                    for (SolicitudCobroMsg.ItemMsg item : msg.items) {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("producto_id", item.producto_id);
                        itemMap.put("cantidad", item.cantidad);
                        itemMap.put("precio_venta", item.precio_venta);
                        itemsList.add(itemMap);
                    }
                }
                mapMsg.put("items", itemsList);
                mapMsg.put("cliente", msg.cliente);
                mapMsg.put("concepto", msg.concepto);
                mapMsg.put("monto", msg.monto);

                jmsTemplate.convertAndSend(queueName, mapMsg);
                log.info("Successfully published message to ActiveMQ: {}", mapMsg);
            } catch (Exception e) {
                log.error("Failed to send message to ActiveMQ: {}. Falling back.", e.getMessage());
            }
        } else {
            // REST mode fallback
            String endpoint = cajaUrl + "/cobros";
            try {
                log.info("Sending SolicitudCobro REST call to Caja API: {}", endpoint);
                restTemplate.postForObject(endpoint, msg, Object.class);
                log.info("Successfully sent REST SolicitudCobro to Caja.");
            } catch (Exception e) {
                log.warn("Failed to communicate with Caja API at {}. Printed payload: {}. Error: {}", endpoint, msg, e.getMessage());
            }
        }
    }
}

