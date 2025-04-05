package com.contrlz.contrlz_backend.repository;

import com.contrlz.contrlz_backend.model.Device;
import com.contrlz.contrlz_backend.model.DeviceLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceLogRepository extends MongoRepository<DeviceLog, String> {
    @Query(value = "{}", sort = "{startTime: -1}")
    List<DeviceLog> findTopNLogsByDevice_deviceIdOrderByEventTimeDesc(Pageable pageable,String deviceId);

    @Query(value = "{}", sort = "{startTime: -1}") // Fetch logs sorted by latest first
    List<DeviceLog> findTopNLogsSortedByTime(Pageable pageable);

    void deleteByDevice_deviceId(String deviceId);
    void deleteByDeviceIn(List<Device> devices);

    List<DeviceLog> findByEventTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<DeviceLog> findByDeviceInAndEventTimeBetween(List<Device> devices, LocalDateTime startDate, LocalDateTime endDate);

}
