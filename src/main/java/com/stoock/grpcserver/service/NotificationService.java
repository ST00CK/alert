package com.stoock.grpcserver.service;

import com.stoock.grpcserver.config.EmitterRepository;
import com.stoock.grpcserver.dto.NotificationDto;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
public class NotificationService {
    private final EmitterRepository emitterRepository;

    public NotificationService(EmitterRepository emitterRepository) {
        this.emitterRepository = emitterRepository;
    }

    public void sendNotification(NotificationDto notificationDto) {
        emitterRepository.get(notificationDto.getReceiver()).ifPresentOrElse(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("chat-alert")
                        .data(notificationDto));
                System.out.println("✅ 알림 전송 완료: " + notificationDto.getReceiver());
            } catch (IOException e){
                System.out.println("❌ 알림 전송 실패: " + notificationDto.getReceiver());
                emitterRepository.remove(notificationDto.getReceiver());
            }
        }, () -> {
            System.out.println("⚠\uFE0F 알림 전송 실패 (emitter 없음): " + notificationDto.getReceiver());
        });
    }
}
