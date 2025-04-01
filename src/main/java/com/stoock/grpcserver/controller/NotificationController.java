package com.stoock.grpcserver.controller;

import com.stoock.grpcserver.config.EmitterRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/sse")
public class NotificationController {
    private final EmitterRepository emitterRepository;

    public NotificationController(EmitterRepository emitterRepository) {
        this.emitterRepository = emitterRepository;
    }

    @GetMapping("/subscribe")
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
        return emitter;
    }
}
