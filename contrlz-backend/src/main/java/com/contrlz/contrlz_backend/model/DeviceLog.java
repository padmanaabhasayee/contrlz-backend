package com.contrlz.contrlz_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.LocalDateTime;

@Document(collection = "device_logs")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLog {
    @Id
    private String id;
    private String deviceId;
    private String updatedBy; // Who performed the action
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Duration getDuration() {
        return (startTime != null && endTime != null) ? Duration.between(startTime, endTime) : null;
    }
}
