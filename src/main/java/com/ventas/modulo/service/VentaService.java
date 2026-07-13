package com.ventas.modulo.service;

import com.ventas.modulo.client.CajaClient;
import com.ventas.modulo.client.StockClient;
import com.ventas.modulo.dto.*;
import com.ventas.modulo.entity.*;
import com.ventas.modulo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Autowired
    private StockClient stockClient;

    @Autowired
    private CajaClient cajaClient;

    @Transactional
    public VentaDetalleCompletoResponse crearVenta(VentaCrearRequest request) {
        // 1. Fetch active configuration
        Configuracion config = configuracionRepository.findFirstByActivoTrue()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No active configuration found in the database. Unable to generate invoice."));

        // 2. Validate client
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente not found with id: " + request.getIdCliente()));

        // 3. Create Venta
        Venta venta = new Venta();
        venta.setIdCliente(request.getIdCliente());
        venta.setFechaEmision(LocalDateTime.now());
        venta.setCondicionVenta(request.getCondicionVenta() != null ? request.getCondicionVenta() : false);
        venta.setNroTimbrado(config.getTimbrado());

        // Generate invoice number satisfying the stock pattern ^[A-Z]{2,3}-\\d{3}-\\d{6}$ (e.g. FV-001-000001)
        String invoiceNum = "FV-" + config.getCodigoEstablecimiento() + "-" + String.format("%06d", config.getNumeroSecuencialActual());
        venta.setNumeroFactura(invoiceNum);

        // Increment configuration sequential number
        config.setNumeroSecuencialActual(config.getNumeroSecuencialActual() + 1);
        configuracionRepository.save(config);

        BigDecimal subtotalIva5 = BigDecimal.ZERO;
        BigDecimal subtotalIva10 = BigDecimal.ZERO;
        BigDecimal subtotalExenta = BigDecimal.ZERO;
        BigDecimal montoTotal = BigDecimal.ZERO;

        List<VentaDetalle> detalles = new ArrayList<>();
        List<StockClient.ReservaItem> stockItems = new ArrayList<>();

        for (VentaCrearDetalleRequest detReq : request.getDetalles()) {
            StockClient.StockProductoConsultaOut detail = stockClient.getProductoDetalle(detReq.getIdProducto(), "producto,precios");
            if (detail == null || detail.producto == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found in stock module with id: " + detReq.getIdProducto());
            }
            StockClient.StockProductoOut stockProducto = detail.producto;
            BigDecimal precioVenta = BigDecimal.ZERO;
            if (detail.precios != null && detail.precios.precioVenta != null) {
                precioVenta = detail.precios.precioVenta;
            }

            String tipoImpuesto = "EXENTA";
            if (stockProducto.impuesto != null) {
                int val = stockProducto.impuesto.intValue();
                if (val == 10) {
                    tipoImpuesto = "IVA_10";
                } else if (val == 5) {
                    tipoImpuesto = "IVA_5";
                }
            }

            VentaDetalle det = new VentaDetalle();
            det.setIdProducto(stockProducto.productoId);
            det.setCantidad(detReq.getCantidad());
            det.setPrecioUnitario(detReq.getPrecioUnitario() != null ? detReq.getPrecioUnitario() : precioVenta);

            BigDecimal subtotal = det.getPrecioUnitario().multiply(BigDecimal.valueOf(det.getCantidad()));
            det.setSubtotal(subtotal);

            // Calculate taxes (inclusive mode: VAT is included in the unit price)
            if ("IVA_10".equalsIgnoreCase(tipoImpuesto)) {
                BigDecimal iva10 = subtotal.divide(BigDecimal.valueOf(11), 2, RoundingMode.HALF_UP);
                det.setIva10(iva10);
                subtotalIva10 = subtotalIva10.add(iva10);
            } else if ("IVA_5".equalsIgnoreCase(tipoImpuesto)) {
                BigDecimal iva5 = subtotal.divide(BigDecimal.valueOf(21), 2, RoundingMode.HALF_UP);
                det.setIva5(iva5);
                subtotalIva5 = subtotalIva5.add(iva5);
            } else {
                det.setExenta(subtotal);
                subtotalExenta = subtotalExenta.add(subtotal);
            }

            montoTotal = montoTotal.add(subtotal);
            detalles.add(det);

            stockItems.add(new StockClient.ReservaItem(stockProducto.productoId, det.getCantidad().doubleValue()));
        }

        venta.setSubtotalIva5(subtotalIva5);
        venta.setSubtotalIva10(subtotalIva10);
        venta.setSubtotalExenta(subtotalExenta);
        venta.setMontoTotal(montoTotal);
        venta.setEstado("PENDIENTE");

        // 4. Call Stock to create reservation
        Integer idReserva = stockClient.crearReserva(invoiceNum, stockItems);
        venta.setIdReserva(idReserva);
        // 5. Save Venta parent to obtain ID first, then set ID on details and cascade-save
        Venta saved = ventaRepository.save(venta);
        for (VentaDetalle det : detalles) {
            det.setIdVenta(saved.getIdVenta());
            saved.getDetalles().add(det);
        }
        saved = ventaRepository.save(saved);
        String clienteFullName = cliente.getNombre() + " " + cliente.getApellido();
        String concepto = "Venta factura " + saved.getNumeroFactura();
        List<CajaClient.SolicitudCobroMsg.ItemMsg> itemsMsg = saved.getDetalles().stream()
                .map(det -> new CajaClient.SolicitudCobroMsg.ItemMsg(
                        det.getIdProducto(),
                        det.getCantidad(),
                        det.getPrecioUnitario() != null ? det.getPrecioUnitario().doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
        cajaClient.enviarSolicitudCobro(
                saved.getIdVenta(),
                saved.getIdReserva(),
                saved.getNumeroFactura(),
                itemsMsg,
                clienteFullName,
                concepto,
                saved.getMontoTotal()
        );

        return mapToDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public VentaPage listarVentas(LocalDate fechaDesde, LocalDate fechaHasta, String estado, Integer idCliente, int page, int size) {
        Specification<Venta> spec = (root, query, cb) -> cb.conjunction();
        if (fechaDesde != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaEmision"), fechaDesde.atStartOfDay()));
        }
        if (fechaHasta != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaEmision"), fechaHasta.atTime(23, 59, 59)));
        }
        if (estado != null && !estado.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (idCliente != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("idCliente"), idCliente));
        }

        Page<Venta> dbPage = ventaRepository.findAll(spec, PageRequest.of(page, size));

        VentaPage resPage = new VentaPage();
        resPage.setItems(dbPage.getContent().stream().map(this::mapToListResponse).collect(Collectors.toList()));
        resPage.setPage(dbPage.getNumber());
        resPage.setSize(dbPage.getSize());
        resPage.setTotalItems(dbPage.getTotalElements());
        resPage.setTotalPages(dbPage.getTotalPages());
        resPage.setHasNext(dbPage.hasNext());
        resPage.setHasPrevious(dbPage.hasPrevious());
        return resPage;
    }

    @Transactional(readOnly = true)
    public VentaDetalleCompletoResponse obtenerVentaPorId(Integer idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta not found with id: " + idVenta));
        return mapToDetailResponse(venta);
    }

    @Transactional
    public VentaDetalleCompletoResponse confirmarVenta(Integer idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta not found with id: " + idVenta));

        if (!"PENDIENTE".equalsIgnoreCase(venta.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDIENTE sales can be confirmed. Current status: " + venta.getEstado());
        }

        venta.setEstado("COMPLETADA");
        Venta saved = ventaRepository.save(venta);

        // Confirm reservation in stock
        if (saved.getIdReserva() != null) {
            //stockClient.confirmarReserva(saved.getIdReserva(), saved.getNumeroFactura());
        }

        return mapToDetailResponse(saved);
    }

    @Transactional
    public VentaDetalleCompletoResponse cancelarVenta(Integer idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta not found with id: " + idVenta));

        if ("CANCELADA".equalsIgnoreCase(venta.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sale is already CANCELADA");
        }

        venta.setEstado("CANCELADA");
        Venta saved = ventaRepository.save(venta);

        // Release reservation in stock
        if (saved.getIdReserva() != null) {
            //stockClient.liberarReserva(saved.getIdReserva());
        }

        return mapToDetailResponse(saved);
    }

    private VentaListaResponse mapToListResponse(Venta entity) {
        VentaListaResponse res = new VentaListaResponse();
        res.setIdVenta(entity.getIdVenta());
        res.setNumeroFactura(entity.getNumeroFactura());
        res.setFechaEmision(entity.getFechaEmision());
        if (entity.getCliente() != null) {
            res.setCliente(entity.getCliente().getNombre() + " " + entity.getCliente().getApellido());
        } else {
            res.setCliente("Cliente #" + entity.getIdCliente());
        }
        res.setCondicionVenta(entity.getCondicionVenta());
        res.setMontoTotal(entity.getMontoTotal());
        res.setEstado(entity.getEstado());
        return res;
    }

    private VentaDetalleCompletoResponse mapToDetailResponse(Venta entity) {
        VentaDetalleCompletoResponse res = new VentaDetalleCompletoResponse();
        res.setIdVenta(entity.getIdVenta());
        res.setFechaEmision(entity.getFechaEmision());
        res.setNroTimbrado(entity.getNroTimbrado());
        res.setNumeroFactura(entity.getNumeroFactura());
        res.setCondicionVenta(entity.getCondicionVenta());
        res.setSubtotalIva5(entity.getSubtotalIva5());
        res.setSubtotalIva10(entity.getSubtotalIva10());
        res.setSubtotalExenta(entity.getSubtotalExenta());
        res.setMontoTotal(entity.getMontoTotal());
        res.setEstado(entity.getEstado());

        // Map client
        ClienteListaResponse cl = new ClienteListaResponse();
        cl.setIdCliente(entity.getIdCliente());
        if (entity.getCliente() != null) {
            cl.setRuc(entity.getCliente().getRuc());
            cl.setCi(entity.getCliente().getCi());
            cl.setNombre(entity.getCliente().getNombre());
            cl.setApellido(entity.getCliente().getApellido());
            cl.setFechaRegistro(entity.getCliente().getFechaRegistro());
        }
        res.setCliente(cl);

        // Map details
        res.setDetalles(entity.getDetalles().stream().map(det -> {
            VentaDetalleLinea line = new VentaDetalleLinea();
            line.setIdDetalle(det.getIdDetalle());
            line.setIdProducto(det.getIdProducto());
            line.setCantidad(det.getCantidad());
            line.setPrecioUnitario(det.getPrecioUnitario());
            line.setIva5(det.getIva5());
            line.setIva10(det.getIva10());
            line.setExenta(det.getExenta());
            line.setSubtotal(det.getSubtotal());
            return line;
        }).collect(Collectors.toList()));

        return res;
    }
}
