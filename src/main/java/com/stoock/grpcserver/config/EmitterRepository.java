package com.stoock.grpcserver.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmitterRepository {
    // 멀티쓰레드 환경에서 더 유용한 HashMap 기법
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 유저의 SSE 연결을 등록
    public void add(String userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
    }

    // 유저의 SSE 연결을 해제 (더 이상 사용하지 않게 제거)
    public void remove(String userId) {
        emitters.remove(userId);
    }

    // 유저의 연결된 emitter 가 있는지 조회하는 용도
    public Optional<SseEmitter> get(String userId) {
        return Optional.ofNullable(emitters.get(userId));
    }
}
