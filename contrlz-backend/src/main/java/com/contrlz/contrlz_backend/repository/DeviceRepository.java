package com.contrlz.contrlz_backend.repository;

import com.contrlz.contrlz_backend.model.Device;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeviceRepository extends MongoRepository<Device, String> {
    Optional<Device> findByDeviceLocation(String deviceLocation);
    @NonNull
    Optional<Device> findById(@NonNull String deviceId);
}
