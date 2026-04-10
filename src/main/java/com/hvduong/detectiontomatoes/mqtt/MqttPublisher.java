package com.hvduong.detectiontomatoes.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttPublisher {
    private MqttClient client;
    private final MqttConnectOptions options;
    @Value("${mqtt.topic.publish}")
    private String publishTopic;
    @Value("${mqtt.broker}")
    private String brokerUrl;

    public MqttPublisher(MqttConnectOptions options) {
        this.options = options;
    }

    private synchronized void ensureConnected() throws MqttException {
        if (client == null) {
            client = new MqttClient(brokerUrl, MqttClient.generateClientId());
            client.connect(options);
        } else if (!client.isConnected()) {
            client.connect(options);
        }
    }

    public void publish(String payload) {
        try {
            ensureConnected();
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            client.publish(publishTopic, message);
        } catch (Exception e) {
            // Log error
        }
    }
}
