package com.contrlz.contrlz_backend.controller;

import com.contrlz.contrlz_backend.model.Device;
import com.contrlz.contrlz_backend.model.DeviceLog;
import com.contrlz.contrlz_backend.service.DeviceService;
import com.contrlz.contrlz_backend.repository.DeviceRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private WebSocketController webSocketController; // Inject WebSocket Controller
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceRepository deviceRepository, DeviceService deviceService) {
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
    }

    @GetMapping("/")
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @GetMapping("/{deviceId}")
    public @NonNull Optional<Device> getDevice(@PathVariable String deviceId){return deviceRepository.findById(deviceId);}

    @PostMapping("/addDevice")
    public Device addDevice(@RequestBody Device device) {
        device.setDeviceMac(device.getDeviceMac().replace(":",""));

        Device savedDevice = deviceRepository.save(device);

        // Notify frontend about the new device added
        webSocketController.sendDevicesUpdate();
        return savedDevice;
    }
    @PatchMapping("/update/{deviceId}")
    public void updateDevice(@PathVariable String deviceId,
                               @RequestBody Device updatedDevice) {
        deviceService.updateDevice(deviceId, updatedDevice);
    }

    @PatchMapping("/{deviceId}")
    public void updateDeviceStatus(@PathVariable String deviceId,
                                   @RequestParam boolean status,
                                   @RequestParam String updatedBy) throws Exception {
        deviceService.toggleDevice(deviceId, status, updatedBy);
    }

    @DeleteMapping("/{deviceId}")
    public void deleteDevice(@PathVariable String deviceId) {
        deviceService.deleteDevice(deviceId);
    }


    @GetMapping("/recent-activity")
    public List<DeviceLog> getRecentLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return deviceService.getRecentActivityLogs(page, limit);
    }

    @PostMapping("/bulk-add")
    public ResponseEntity<?> bulkCreateDevices(@RequestBody List<Device> devices) {
        if (devices == null || devices.isEmpty()) {
            return ResponseEntity.badRequest().body("User list is empty or missing.");
        }

        return deviceService.bulkCreateDevices(devices);
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDeleteDevices(@RequestBody List<String> deviceIds){
        return deviceService.bulkDeleteDevices(deviceIds);
    }

    @GetMapping("/recent-activity/{deviceId}")
    public List<DeviceLog> getLogOf(@PathVariable String deviceId){
        return deviceService.getRecentActivityLogsOf(deviceId);
    }

    @PostMapping("/logs")
    public ResponseEntity<List<DeviceLog>> getDeviceLogs(@RequestBody Map<String, Object> request) {
        LocalDateTime startDate = LocalDateTime.parse((String) request.get("startDate"));
        LocalDateTime endDate = LocalDateTime.parse((String) request.get("endDate"));
        List<String> deviceIds = (List<String>) request.get("deviceIds");
        List<DeviceLog> logs = deviceService.getLogs(startDate, endDate, deviceIds);
        return ResponseEntity.ok(logs);
    }
}
