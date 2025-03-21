package com.contrlz.contrlz_backend.service;

import com.contrlz.contrlz_backend.model.Device;
import com.contrlz.contrlz_backend.model.DeviceLog;
import com.contrlz.contrlz_backend.repository.DeviceRepository;
import com.contrlz.contrlz_backend.repository.DeviceLogRepository;
import com.contrlz.contrlz_backend.controller.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceLogRepository deviceLogRepository;

    @Autowired
    private WebSocketController webSocketController; // Inject WebSocket Controller

    // Toggle device ON/OFF and log activity
    public void toggleDevice(String deviceId, boolean status, String updatedBy) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setStatus(status);
        deviceRepository.save(device);

        // Notify frontend about device status update
        webSocketController.sendDeviceUpdate(device);

        DeviceLog log;
        if (status) {
            log = new DeviceLog();
            log.setDeviceId(deviceId);
            log.setUpdatedBy(updatedBy);
            log.setStartTime(LocalDateTime.now());
        } else {
            log = deviceLogRepository.findTopByDeviceIdOrderByStartTimeDesc(deviceId)
                    .orElseThrow(() -> new RuntimeException("No log entry found for this device"));
            log.setEndTime(LocalDateTime.now());
        }
        deviceLogRepository.save(log);

        // Notify frontend about recent activity update
        webSocketController.sendRecentActivity(log);
    }

    public List<DeviceLog> getRecentActivityLogs(int limit) {
        return deviceLogRepository.findTopNLogsSortedByTime(org.springframework.data.domain.PageRequest.of(0, limit));
    }
}
