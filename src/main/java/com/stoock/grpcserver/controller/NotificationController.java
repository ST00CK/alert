package com.stoock.grpcserver.controller;

import com.stoock.grpcserver.config.EmitterRepository;
import com.stoock.grpcserver.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/sse")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam String userId) {
        return notificationService.subscribe(userId);
    }
}
