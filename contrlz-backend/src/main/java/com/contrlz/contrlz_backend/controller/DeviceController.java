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

    @GetMapping("/{deviceName}")
    public Optional<Device> getDeviceByName(@PathVariable String deviceName) {
        return deviceRepository.findByDeviceName(deviceName);
    }

    @PostMapping("/addDevice")
    public Device addDevice(@RequestBody Device device) {
        return deviceRepository.save(device);
    }

    @PutMapping("/{deviceId}")
    public void updateDeviceStatus(@PathVariable String deviceId,
                                   @RequestParam boolean status,
                                   @RequestParam String updatedBy) {
        deviceService.toggleDevice(deviceId, status, updatedBy);
    }

    @GetMapping("/recent-activity")
    public List<DeviceLog> getRecentActivity(@RequestParam(defaultValue = "10") int limit) {
        return deviceService.getRecentActivityLogs(limit);
    }
}
