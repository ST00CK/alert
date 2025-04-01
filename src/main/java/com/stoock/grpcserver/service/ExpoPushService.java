package com.stoock.grpcserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExpoPushService {
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendPushNotification(String pushToken, String roomId, String userId, String message, String roomName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = userId + ": " + message;

        Map<String, Object> payload = new HashMap<>();
        payload.put("to", pushToken);
        payload.put("title", roomName);
        payload.put("body", body);
        payload.put("sound", "default");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try{
            ResponseEntity<String> response = restTemplate.exchange(EXPO_PUSH_URL, HttpMethod.POST, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Expo Push Notification ERROR: " + response.getBody());
            }
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Expo Push Notification ERROR: " + e.getMessage());
            return false;
        }
    }
}
