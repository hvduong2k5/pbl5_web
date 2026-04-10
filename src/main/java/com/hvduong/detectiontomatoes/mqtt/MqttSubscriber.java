package com.hvduong.detectiontomatoes.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hvduong.detectiontomatoes.model.dto.FruitEventDTO;
import com.hvduong.detectiontomatoes.service.FruitService;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class MqttSubscriber implements MqttCallback {
    private MqttClient client;
    private final ObjectMapper objectMapper;
    private final FruitService fruitService;
    private final MqttConnectOptions options;
    @Value("${mqtt.topic.subscribe}")
    private String subscribeTopic;
    @Value("${mqtt.broker}")
    private String brokerUrl;

    public MqttSubscriber(MqttConnectOptions options, FruitService fruitService) {
        this.objectMapper = new ObjectMapper();
        this.fruitService = fruitService;
        this.options = options;
    }

    @PostConstruct
    public void subscribe() {
        try {
            if (client == null) {
                client = new MqttClient(brokerUrl, MqttClient.generateClientId());
                client.setCallback(this);
                client.connect(options);
            }
            client.subscribe(subscribeTopic,1);
        } catch (MqttException e) {
            System.err.println("MQTT connect/subscribe failed: " + e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        // Log connection lost
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            FruitEventDTO event = objectMapper.readValue(message.getPayload(), FruitEventDTO.class);
            System.out.println("event: " + event.getEvent());
            switch (event.getEvent()) {
                case "detected" -> fruitService.handleDetected(event);
                case "classified" -> fruitService.handleClassified(event);
                case "sorted" -> fruitService.handleSorted(event);
                case "transfer" -> fruitService.handleTransfer(event);
            }
        } catch (Exception e) {
            // Log error
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used for subscriber
    }
}
