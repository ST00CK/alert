package com.stoock.grpcserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class NotificationDto {
    private final String roomId;
    private final String userId;
    private final String receiver;
    private final String message;
    private final String roomName;
    private final String timestamp;
}
