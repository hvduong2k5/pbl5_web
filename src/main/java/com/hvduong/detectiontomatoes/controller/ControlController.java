package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.dto.ControlCommandDTO;
import com.hvduong.detectiontomatoes.service.ControlService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control")
public class ControlController {
    
    private final ControlService controlService;

    public ControlController(ControlService controlService) {
        this.controlService = controlService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTROL_SYSTEM')")
    public ResponseEntity<Void> control(@RequestBody ControlCommandDTO dto) {
        controlService.publishCommand(dto);
        return ResponseEntity.ok().build();
    }
}
