package com.contrlz.contrlz_backend.repository;

import com.contrlz.contrlz_backend.model.DeviceLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceLogRepository extends MongoRepository<DeviceLog, String> {
    Optional<DeviceLog> findTopByDevice_deviceIdOrderByStartTimeDesc(String deviceId);

    @Query(value = "{}", sort = "{startTime: -1}") // Fetch logs sorted by latest first
    List<DeviceLog> findTopNLogsSortedByTime(Pageable pageable);

    void deleteByDevice_deviceId(String deviceId);
}
