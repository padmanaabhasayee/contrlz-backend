package com.contrlz.contrlz_backend.controller;

import com.contrlz.contrlz_backend.model.Device;
import com.contrlz.contrlz_backend.model.DeviceLog;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendDeviceUpdate(Device device) {
        messagingTemplate.convertAndSend("/topic/device-updates", device);
    }

    public void sendRecentActivity(DeviceLog log) {
        messagingTemplate.convertAndSend("/topic/recent-activity", log);
    }
}
