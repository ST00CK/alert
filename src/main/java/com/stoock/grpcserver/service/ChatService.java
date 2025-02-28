package com.stoock.grpcserver.service;

import chat.ChatServiceGrpc;
import chat.Chat;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import com.stoock.grpcserver.dto.NotificationDto;
// import com.stoock.grpcserver.openfeign.NotificationClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@GrpcService
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {
    private final PushTokenService pushTokenService;
    private final ExpoPushService expoPushService;

    public ChatService(PushTokenService pushTokenService, ExpoPushService expoPushService) {
        this.pushTokenService = pushTokenService;
        this.expoPushService = expoPushService;
    };

    private final List<StreamObserver<Chat.NotificationResponse>> observers = new CopyOnWriteArrayList<>();

    @Override
    public void sendNotification(Chat.SendNotificationRequest request, StreamObserver<Chat.SendNotificationResponse> responseObserver) {
        System.out.println("sendNotification: 클라이언트와 연결됨");

        String roomId = request.getRoomId();
        String userId = request.getUserId();
        String message = request.getMessage();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("===== 알림 데이터 =====");
        System.out.println("방   : " + roomId);
        System.out.println("유저  : " + userId);
        System.out.println("내용  : " + message);
        System.out.println("시간  : " + timestamp);

        String pushToken = pushTokenService.getPushToken(userId);

        int status = 0;
        if (pushToken != null) {
            boolean success = expoPushService.sendPushNotification(pushToken, roomId, userId, message);
            if (success) {
                status = 1;
            }
        } else {
            System.err.println("pushToken is null");
        }

        NotificationDto dto = new NotificationDto(roomId, userId, message, timestamp);
        // notificationClient.sendNotification(dto);

        Chat.NotificationResponse notification = Chat.NotificationResponse.newBuilder()
                .setRoomId(dto.getRoomId())
                .setUserId(dto.getUserId())
                .setMessage(dto.getMessage())
                .setTimestamp(dto.getTimestamp())
                .build();

        synchronized (observers) {
            for (StreamObserver<Chat.NotificationResponse> observer : observers) {
                observer.onNext(notification);
            }
        }

        Chat.SendNotificationResponse response = Chat.SendNotificationResponse.newBuilder()
                .setStatus(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
