package com.hvduong.detectiontomatoes.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    @Value("${mqtt.broker}")
    private String brokerUrl;
    @Value("${mqtt.username:}")
    private String username;
    @Value("${mqtt.password:}")
    private String password;

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl}); // Sử dụng biến từ config
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
        }
        if (password != null && !password.isEmpty()) {
            options.setPassword(password.toCharArray());
        }
        return options;
    }
}