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
import java.util.Optional;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceLogRepository deviceLogRepository;
    @Autowired
    private MqttService mqttService;

    @Autowired
    private WebSocketController webSocketController; // Inject WebSocket Controller

    // Toggle device ON/OFF and log activity
    public void toggleDevice(String deviceId, boolean status, String updatedBy) throws Exception {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setStatus(status);
        deviceRepository.save(device);

        String topic = "/contrlz";
        String message = status ? "ON" : "OFF";
        mqttService.publishMessage(topic,message);

        webSocketController.sendDevicesUpdate();

        DeviceLog log;
        if (status) {
            log = new DeviceLog();
            log.setDevice(device);
            log.setTurnedOnBy(updatedBy);
            log.setStartTime(LocalDateTime.now());
        } else {
            log = deviceLogRepository.findTopByDevice_deviceIdOrderByStartTimeDesc(deviceId)
                    .orElseThrow(() -> new RuntimeException("No log entry found for this device"));
            log.setTurnedOffBy(updatedBy);
            log.setEndTime(LocalDateTime.now());
        }
        deviceLogRepository.save(log);

        // Notify frontend about recent activity update
        webSocketController.sendRecentActivity();
    }

    public void updateDevice(String deviceId, Device updatedDevice) {
        Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
        if (deviceOptional.isPresent()) {
            Device device = deviceOptional.get();
            device.setDeviceType(updatedDevice.getDeviceType()); // Update type
            device.setDeviceLocation(updatedDevice.getDeviceLocation()); // Update location
            device.setStatus(updatedDevice.isStatus()); // Update status
            deviceRepository.save(device);
            webSocketController.sendDevicesUpdate();
        } else {
            throw new RuntimeException("Device not found");
        }
    }

    public List<DeviceLog> getRecentActivityLogs(int limit) {
        return deviceLogRepository.findTopNLogsSortedByTime(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    public void deleteDevice(String deviceId) {
        Optional<Device> device = deviceRepository.findById(deviceId);
        if (device.isPresent()) {
            deviceRepository.deleteById(deviceId);
            deviceLogRepository.deleteByDevice_deviceId(deviceId);
            webSocketController.sendDevicesUpdate();
            webSocketController.sendRecentActivity();
        } else {
            throw new RuntimeException("Device not found: " + deviceId);
        }
    }
}
