package com.ventas.modulo.service;

import com.ventas.modulo.dto.*;
import com.ventas.modulo.entity.Cliente;
import com.ventas.modulo.entity.ClienteContacto;
import com.ventas.modulo.entity.ClienteDireccion;
import com.ventas.modulo.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional
    public ClienteDetalleCompletoResponse crearCliente(ClienteCrearRequest request) {
        Cliente cliente = new Cliente();
        cliente.setRuc(request.getRuc());
        cliente.setCi(request.getCi());
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setFechaRegistro(LocalDateTime.now());

        // Save parent first to obtain the auto-generated id_cliente
        Cliente saved = clienteRepository.save(cliente);

        if (request.getDirecciones() != null) {
            for (DireccionRequest dirReq : request.getDirecciones()) {
                ClienteDireccion dir = new ClienteDireccion();
                dir.setIdCliente(saved.getIdCliente());
                dir.setTipo(dirReq.getTipo());
                dir.setCallePrincipal(dirReq.getCallePrincipal());
                dir.setCalleSecundaria(dirReq.getCalleSecundaria());
                dir.setNumeroCasa(dirReq.getNumeroCasa());
                dir.setBarrio(dirReq.getBarrio());
                dir.setCiudad(dirReq.getCiudad());
                dir.setLatitud(dirReq.getLatitud());
                dir.setLongitud(dirReq.getLongitud());
                dir.setReferencia(dirReq.getReferencia());
                saved.getDirecciones().add(dir);
            }
        }

        if (request.getContactos() != null) {
            for (ContactoRequest conReq : request.getContactos()) {
                ClienteContacto con = new ClienteContacto();
                con.setIdCliente(saved.getIdCliente());
                con.setNombre(conReq.getNombre());
                con.setCargo(conReq.getCargo());
                con.setTelefono(conReq.getTelefono());
                con.setEmail(conReq.getEmail());
                con.setEsPrincipal(conReq.getEsPrincipal() != null ? conReq.getEsPrincipal() : false);
                saved.getContactos().add(con);
            }
        }

        // Save again with child entities
        saved = clienteRepository.save(saved);
        return mapToDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClientePage listarClientes(String ruc, String ci, String nombre, String apellido, int page, int size) {
        Specification<Cliente> spec = (root, query, cb) -> cb.conjunction();
        if (ruc != null && !ruc.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("ruc"), "%" + ruc + "%"));
        }
        if (ci != null && !ci.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("ci"), "%" + ci + "%"));
        }
        if (nombre != null && !nombre.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
        }
        if (apellido != null && !apellido.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("apellido")), "%" + apellido.toLowerCase() + "%"));
        }

        Page<Cliente> dbPage = clienteRepository.findAll(spec, PageRequest.of(page, size));
        ClientePage resPage = new ClientePage();
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
    public ClienteDetalleCompletoResponse obtenerClientePorId(Integer idCliente) {
        Cliente client = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente not found with id: " + idCliente));
        return mapToDetailResponse(client);
    }

    private ClienteListaResponse mapToListResponse(Cliente entity) {
        ClienteListaResponse res = new ClienteListaResponse();
        res.setIdCliente(entity.getIdCliente());
        res.setRuc(entity.getRuc());
        res.setCi(entity.getCi());
        res.setNombre(entity.getNombre());
        res.setApellido(entity.getApellido());
        res.setFechaRegistro(entity.getFechaRegistro());
        return res;
    }

    private ClienteDetalleCompletoResponse mapToDetailResponse(Cliente entity) {
        ClienteDetalleCompletoResponse res = new ClienteDetalleCompletoResponse();
        res.setIdCliente(entity.getIdCliente());
        res.setRuc(entity.getRuc());
        res.setCi(entity.getCi());
        res.setNombre(entity.getNombre());
        res.setApellido(entity.getApellido());
        res.setFechaRegistro(entity.getFechaRegistro());

        res.setDirecciones(entity.getDirecciones().stream().map(dir -> {
            DireccionRequest d = new DireccionRequest();
            d.setIdDireccion(dir.getIdDireccion());
            d.setTipo(dir.getTipo());
            d.setCallePrincipal(dir.getCallePrincipal());
            d.setCalleSecundaria(dir.getCalleSecundaria());
            d.setNumeroCasa(dir.getNumeroCasa());
            d.setBarrio(dir.getBarrio());
            d.setCiudad(dir.getCiudad());
            d.setLatitud(dir.getLatitud());
            d.setLongitud(dir.getLongitud());
            d.setReferencia(dir.getReferencia());
            return d;
        }).collect(Collectors.toList()));

        res.setContactos(entity.getContactos().stream().map(con -> {
            ContactoRequest c = new ContactoRequest();
            c.setIdContacto(con.getIdContacto());
            c.setNombre(con.getNombre());
            c.setCargo(con.getCargo());
            c.setTelefono(con.getTelefono());
            c.setEmail(con.getEmail());
            c.setEsPrincipal(con.getEsPrincipal());
            return c;
        }).collect(Collectors.toList()));

        return res;
    }
}
