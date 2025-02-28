package com.stoock.grpcserver.controller;

import com.stoock.grpcserver.service.PushTokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/push-token")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    public PushTokenController(PushTokenService pushTokenService) {
        this.pushTokenService = pushTokenService;
    }

    @PostMapping("/register")
    public String registerPushToken(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String token = request.get("token");

        pushTokenService.savePushToken(userId, token);
        return "Push token registered successfully";
    }
}
