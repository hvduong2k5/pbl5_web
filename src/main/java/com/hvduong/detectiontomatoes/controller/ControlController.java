package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.dto.ControlCommandDTO;
import com.hvduong.detectiontomatoes.mqtt.MqttPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control")
public class ControlController {
    private final MqttPublisher mqttPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ControlController(MqttPublisher mqttPublisher) {
        this.mqttPublisher = mqttPublisher;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTROL_SYSTEM')")
    public ResponseEntity<Void> control(@RequestBody ControlCommandDTO dto) {
        try {
            String payload = objectMapper.writeValueAsString(dto);
            mqttPublisher.publish(payload);
        } catch (Exception e) {
            // Log error
        }
        return ResponseEntity.ok().build();
    }
}
