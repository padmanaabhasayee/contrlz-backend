package com.contrlz.contrlz_backend.model;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
    private String event; // ON or OFF
    private String eventBy; // done by who
    private LocalDateTime eventTime; // when is it done
}
