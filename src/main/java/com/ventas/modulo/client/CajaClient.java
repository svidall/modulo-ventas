package com.ventas.modulo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class CajaClient {

    private static final Logger log = LoggerFactory.getLogger(CajaClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;

    @Value("${app.integration.caja.mode:QUEUE}")
    private String mode; // QUEUE or REST

    @Value("${app.integration.caja.url:http://localhost:8081}")
    private String cajaUrl;

    @Value("${app.integration.caja.queue-name:caja.solicitudes}")
    private String queueName;

    public static class SolicitudCobroMsg {
        public String tipo = "SOLICITUD_COBRO";
        public String id_venta;
        public Integer id_reserva;
        public String cliente;
        public String concepto;
        public Double monto;

        public SolicitudCobroMsg(String idVenta, Integer idReserva, String cliente, String concepto, Double monto) {
            this.id_venta = idVenta;
            this.id_reserva = idReserva;
            this.cliente = cliente;
            this.concepto = concepto;
            this.monto = monto;
        }
    }

    public void enviarSolicitudCobro(Integer idVenta, Integer idReserva, String clienteName, String concepto, BigDecimal monto) {
        String idVentaStr = "VTA-" + idVenta;
        SolicitudCobroMsg msg = new SolicitudCobroMsg(
                idVentaStr,
                idReserva,
                clienteName,
                concepto,
                monto != null ? monto.doubleValue() : 0.0
        );

        if ("QUEUE".equalsIgnoreCase(mode) && jmsTemplate != null) {
            try {
                log.info("Sending SolicitudCobro message to ActiveMQ queue '{}' for Venta: {}", queueName, idVentaStr);
                // Convert object to JSON string or send as MapMessage
                Map<String, Object> mapMsg = new HashMap<>();
                mapMsg.put("tipo", msg.tipo);
                mapMsg.put("id_venta", msg.id_venta);
                mapMsg.put("id_reserva", msg.id_reserva);
                mapMsg.put("cliente", msg.cliente);
                mapMsg.put("concepto", msg.concepto);
                mapMsg.put("monto", msg.monto);

                jmsTemplate.convertAndSend(queueName, mapMsg);
                log.info("Successfully published message to queue: {}", mapMsg);
            } catch (Exception e) {
                log.error("Failed to send message to queue: {}. Falling back to log print.", e.getMessage());
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
