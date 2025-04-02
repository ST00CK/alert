package com.stoock.grpcserver.service;

import com.stoock.grpcserver.config.EmitterRepository;
import com.stoock.grpcserver.dto.NotificationDto;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationService {
    private final EmitterRepository emitterRepository;

    public NotificationService(EmitterRepository emitterRepository) {
        this.emitterRepository = emitterRepository;
    }

    public SseEmitter subscribe(@RequestParam String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitterRepository.add(userId, emitter);

        emitter.onTimeout(() -> emitterRepository.remove(userId));
        emitter.onCompletion(() -> emitterRepository.remove(userId));

        System.out.println("\uD83D\uDFE2 Emitter 등록 완료: " + userId);

        try {
            emitter.send(SseEmitter.event().name("INIT").data("connected"));
        } catch (IOException e){
            emitter.completeWithError(e);
        }

        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
        schedule.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitterRepository.remove(userId);
                schedule.shutdown();
            }
        }, 0, 20, TimeUnit.SECONDS);

        return emitter;
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
