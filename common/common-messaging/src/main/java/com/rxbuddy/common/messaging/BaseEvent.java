package com.rxbuddy.common.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

    private String eventId;
    private String eventType;
    private Long tenantId;
    private LocalDateTime timestamp;
    private String correlationId;

    protected void initializeEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        if (this.correlationId == null) {
            this.correlationId = UUID.randomUUID().toString();
        }
    }
}
