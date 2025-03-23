package com.contrlz.contrlz_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
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
    @DBRef
    private Device device;
    private String turnedOnBy;
    private String turnedOffBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Duration getDuration() {
        return (startTime != null && endTime != null) ? Duration.between(startTime, endTime) : null;
    }
}
