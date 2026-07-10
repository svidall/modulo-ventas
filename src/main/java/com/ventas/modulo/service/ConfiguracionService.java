package com.ventas.modulo.service;

import com.ventas.modulo.dto.ConfiguracionRequest;
import com.ventas.modulo.dto.ConfiguracionResponse;
import com.ventas.modulo.entity.Configuracion;
import com.ventas.modulo.repository.ConfiguracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConfiguracionService {

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Transactional(readOnly = true)
    public ConfiguracionResponse getActiveConfig() {
        Configuracion config = configuracionRepository.findFirstByActivoTrue()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active configuration found"));
        return mapToResponse(config);
    }

    @Transactional
    public ConfiguracionResponse updateConfig(ConfiguracionRequest request) {
        Configuracion config = configuracionRepository.findFirstByActivoTrue()
                .orElse(new Configuracion());

        config.setRucEmpresa(request.getRucEmpresa());
        config.setRazonSocialEmpresa(request.getRazonSocialEmpresa());
        config.setTimbrado(request.getTimbrado());
        config.setFechaInicioTimbrado(request.getFechaInicioTimbrado());
        config.setFechaFinTimbrado(request.getFechaFinTimbrado());
        config.setCodigoEstablecimiento(request.getCodigoEstablecimiento());
        config.setCodigoPuntoExpedicion(request.getCodigoPuntoExpedicion());
        config.setActivo(request.getActivo() != null ? request.getActivo() : true);

        if (config.getIdConfiguracion() == null) {
            config.setNumeroSecuencialActual(1);
        }

        Configuracion saved = configuracionRepository.save(config);
        return mapToResponse(saved);
    }

    private ConfiguracionResponse mapToResponse(Configuracion entity) {
        ConfiguracionResponse res = new ConfiguracionResponse();
        res.setIdConfiguracion(entity.getIdConfiguracion());
        res.setRucEmpresa(entity.getRucEmpresa());
        res.setRazonSocialEmpresa(entity.getRazonSocialEmpresa());
        res.setTimbrado(entity.getTimbrado());
        res.setFechaInicioTimbrado(entity.getFechaInicioTimbrado());
        res.setFechaFinTimbrado(entity.getFechaFinTimbrado());
        res.setCodigoEstablecimiento(entity.getCodigoEstablecimiento());
        res.setCodigoPuntoExpedicion(entity.getCodigoPuntoExpedicion());
        res.setNumeroSecuencialActual(entity.getNumeroSecuencialActual());
        res.setActivo(entity.getActivo());
        return res;
    }
}
