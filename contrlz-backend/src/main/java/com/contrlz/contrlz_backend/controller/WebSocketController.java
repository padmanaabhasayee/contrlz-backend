package com.contrlz.contrlz_backend.controller;

import com.contrlz.contrlz_backend.model.AppUser;
import com.contrlz.contrlz_backend.model.Device;
import com.contrlz.contrlz_backend.model.DeviceLog;
import com.contrlz.contrlz_backend.repository.DeviceLogRepository;
import com.contrlz.contrlz_backend.repository.DeviceRepository;
import com.contrlz.contrlz_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Autowired
    private DeviceLogRepository deviceLogRepository;
    public void sendRecentActivity() {
        List<DeviceLog> logs = deviceLogRepository.findTopNLogsSortedByTime(org.springframework.data.domain.PageRequest.of(0, 10));
        messagingTemplate.convertAndSend("/contrlz/recent-activity", logs);
    }

    @Autowired
    private DeviceRepository deviceRepository;

    public void sendDevicesUpdate() {
        List<Device> allDevices = deviceRepository.findAll();
        messagingTemplate.convertAndSend("/contrlz/devices", allDevices);
    }

    @Autowired
    private UserRepository userRepository;

    public void sendUsersUpdate(){
        List<AppUser> allUsers = userRepository.findAll();
        messagingTemplate.convertAndSend("/contrlz/users", allUsers);
    }
}
