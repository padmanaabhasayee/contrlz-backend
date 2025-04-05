package com.contrlz.contrlz_backend.service;

import com.contrlz.contrlz_backend.model.Device;
import com.contrlz.contrlz_backend.model.DeviceLog;
import com.contrlz.contrlz_backend.repository.DeviceRepository;
import com.contrlz.contrlz_backend.repository.DeviceLogRepository;
import com.contrlz.contrlz_backend.controller.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

        String topic = "contrlz/devices" + device.getDeviceMac();
        String message = status ? "ON" : "OFF";
        mqttService.publishMessage(topic, message);

        webSocketController.sendDevicesUpdate();

        // Create a new log entry for the event
        DeviceLog log = new DeviceLog();
        log.setDevice(device);
        log.setEvent(status ? "ON" : "OFF");
        log.setEventBy(updatedBy);
        log.setEventTime(LocalDateTime.now());

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

    public List<DeviceLog> getRecentActivityLogs(int page,int limit) {
        return deviceLogRepository.findTopNLogsSortedByTime(org.springframework.data.domain.PageRequest.of(page, limit));
    }

    public void deleteDevice(String deviceId) {
        Optional<Device> device = deviceRepository.findById(deviceId);
        if (device.isPresent()) {
            deviceLogRepository.deleteByDevice_deviceId(deviceId);
            deviceRepository.deleteById(deviceId);
            webSocketController.sendDevicesUpdate();
            webSocketController.sendRecentActivity();
        } else {
            throw new RuntimeException("Device not found: " + deviceId);
        }
    }


    public ResponseEntity<Void> bulkCreateDevices(List<Device> devices) {
        for (Device device : devices) {
            if (device.getDeviceType() == null || device.getDeviceLocation() == null || device.getDeviceMac() == null) {
                throw new IllegalArgumentException("Each device must have deviceType, deviceLocation, and deviceMac");
            }
            device.setLastUpdated(LocalDateTime.now());
            device.setDeviceMac(device.getDeviceMac().replace(":",""));
        }
        deviceRepository.saveAll(devices);
        webSocketController.sendDevicesUpdate();
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> bulkDeleteDevices(List<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            throw new IllegalArgumentException("Device IDs list cannot be empty");
        }

        // Fetch devices first
        List<Device> devices = deviceRepository.findAllById(deviceIds);
        if (devices.isEmpty()) {
            throw new RuntimeException("No devices found for the given IDs");
        }


        // Delete logs first
        deviceLogRepository.deleteByDeviceIn(devices);

        // Delete devices
        deviceRepository.deleteAllById(deviceIds);

        // Send WebSocket updates
        webSocketController.sendDevicesUpdate();
        webSocketController.sendRecentActivity();

        return ResponseEntity.ok().build();
    }

    public List<DeviceLog> getRecentActivityLogsOf(String deviceId) {
        return deviceLogRepository.findTopNLogsByDevice_deviceIdOrderByEventTimeDesc(org.springframework.data.domain.PageRequest.of(0, 10),deviceId);
    }

    public List<DeviceLog> getLogs(LocalDateTime startDate, LocalDateTime endDate, List<String> deviceIds) {
        List<DeviceLog> logs;
        if (deviceIds == null || deviceIds.isEmpty()) {
            logs = deviceLogRepository.findByEventTimeBetween(startDate, endDate);
        } else {
            List<Device> devices = deviceRepository.findAllById(deviceIds);
            logs = deviceLogRepository.findByDeviceInAndEventTimeBetween(devices, startDate, endDate);
        }

        return logs;
    }
}
