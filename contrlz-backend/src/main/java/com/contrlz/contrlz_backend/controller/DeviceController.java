package com.contrlz.contrlz_backend.controller;

import com.contrlz.contrlz_backend.model.Device;
import com.contrlz.contrlz_backend.model.DeviceLog;
import com.contrlz.contrlz_backend.service.DeviceService;
import com.contrlz.contrlz_backend.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/{deviceLocation}")
    public Optional<Device> getDeviceByName(@PathVariable String deviceLocation) {
        return deviceRepository.findByDeviceLocation(deviceLocation);
    }

    @PostMapping("/addDevice")
    public Device addDevice(@RequestBody Device device) {
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
    public List<DeviceLog> getRecentActivity(@RequestParam(defaultValue = "10") int limit) {
        return deviceService.getRecentActivityLogs(limit);
    }
}
