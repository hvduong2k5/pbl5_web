package com.hvduong.detectiontomatoes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hvduong.detectiontomatoes.model.dto.ControlCommandDTO;
import com.hvduong.detectiontomatoes.mqtt.MqttPublisher;
import org.springframework.stereotype.Service;

@Service
public class ControlService {
    private final MqttPublisher mqttPublisher;
    private final ObjectMapper objectMapper;

    public ControlService(MqttPublisher mqttPublisher, ObjectMapper objectMapper) {
        this.mqttPublisher = mqttPublisher;
        this.objectMapper = objectMapper;
    }

    public void publishCommand(ControlCommandDTO dto) {
        try {
            String payload = objectMapper.writeValueAsString(dto);
            mqttPublisher.publish(payload);
        } catch (Exception e) {
            System.err.println("Failed to publish command: " + e.getMessage());
        }
    }
}
