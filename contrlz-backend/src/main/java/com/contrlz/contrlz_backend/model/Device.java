package com.contrlz.contrlz_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Device {

    @Id
    private String deviceId; // Unique ID
    private String deviceType; // Fan, Light, etc.
    private String deviceLocation;
    private boolean status; // ON = true, OFF = false
    private LocalDateTime lastUpdated; // Last updated timestamp

    public Device(String deviceType, String deviceLocation, boolean status) {
        this.deviceType = deviceType;
        this.deviceLocation = deviceLocation;
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }

    public void setStatus(boolean status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }
}
