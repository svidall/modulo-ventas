package com.ventas.modulo.controller;

import com.ventas.modulo.dto.ConfiguracionRequest;
import com.ventas.modulo.dto.ConfiguracionResponse;
import com.ventas.modulo.service.ConfiguracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    @Autowired
    private ConfiguracionService configuracionService;

    @GetMapping
    public ConfiguracionResponse getConfiguracionActiva() {
        return configuracionService.getActiveConfig();
    }

    @PutMapping
    public ConfiguracionResponse updateConfiguracion(@RequestBody ConfiguracionRequest request) {
        return configuracionService.updateConfig(request);
    }
}
