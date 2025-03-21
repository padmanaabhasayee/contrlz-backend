package com.contrlz.contrlz_backend.controller;

import com.contrlz.contrlz_backend.service.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mqtt")
public class MqttController {

    private final MqttService mqttService;

    @Autowired
    public MqttController(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @PostMapping("/publish")
    public void publishMessage(@RequestParam String topic, @RequestParam String message) {
        try {
            mqttService.publishMessage(topic, message);
            ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            ResponseEntity.status(500).body("Error sending message: " + e.getMessage());
        }
    }
}
