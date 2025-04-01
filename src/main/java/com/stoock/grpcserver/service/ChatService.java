package com.stoock.grpcserver.service;

import chat.ChatServiceGrpc;
import chat.Chat;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import com.stoock.grpcserver.dto.NotificationDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@GrpcService
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {
    private final NotificationService notificationService;

    public ChatService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void sendNotification(Chat.SendNotificationRequest request, StreamObserver<Chat.SendNotificationResponse> responseObserver) {
        System.out.println("sendNotification: 클라이언트와 연결됨");

        String roomId = request.getRoomId();
        String sender = request.getUserId();
        String receiver = request.getReceiver();
        String message = request.getMessage();
        String roomName = request.getRoomName();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("===== 알림 데이터 =====");
        System.out.println("방   : " + roomId);
        System.out.println("보낸 유저  : " + sender);
        System.out.println("받는 유저  : " + receiver);
        System.out.println("내용  : " + message);
        System.out.println("방이름 : " + roomName);
        System.out.println("시간  : " + timestamp);

        NotificationDto dto = new NotificationDto(roomId, sender, receiver, message, roomName, timestamp);

        notificationService.sendNotification(dto);

        Chat.SendNotificationResponse response = Chat.SendNotificationResponse.newBuilder()
                .setStatus(1)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
