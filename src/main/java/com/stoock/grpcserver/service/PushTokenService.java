package com.stoock.grpcserver.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PushTokenService {

    private final StringRedisTemplate redisTemplate;

    public PushTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void savePushToken(String userId, String token) {
        redisTemplate.opsForValue().set("push_token: " + userId, token, 30, TimeUnit.DAYS);
    }

    public String getPushToken(String userId) {
        String token = redisTemplate.opsForValue().get("push_token: " + userId);
        if (token == null) {
            System.err.println("pushToken is null");
        }
        return token;
    }
}
