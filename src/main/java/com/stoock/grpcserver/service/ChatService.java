package com.stoock.grpcserver.service;

import chat.ChatServiceGrpc;
import chat.Chat;

import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@GrpcService
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {

    // 연결된 클라이언트의 StreamObserver를 저장하는 리스트
    private final List<StreamObserver<Chat.NotificationResponse>> observers = new CopyOnWriteArrayList<>();

    // 단방향 알림
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

        // streamNotifications 구독자들에게 알림 브로드캐스트
        Chat.NotificationResponse notification = Chat.NotificationResponse.newBuilder()
                .setRoomId(roomId)
                .setUserId(userId)
                .setMessage(message)
                .setTimestamp(timestamp)
                .build();

        for (StreamObserver<Chat.NotificationResponse> observer : observers) {
            observer.onNext(notification);
        }

        // 단방향 응답 생성
        Chat.SendNotificationResponse response = Chat.SendNotificationResponse.newBuilder()
                .setStatus("success")
                .build();

        // 응답 전송 및 스트림 종료
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // 양방향 통신
    @Override
    public StreamObserver<Chat.NotificationRequest> streamNotifications(StreamObserver<Chat.NotificationResponse> responseObserver) {
        System.out.println("streamNotifications: 클라이언트와 연결됨");

        // 새로운 클라이언트의 StreamObserver를 리스트에 추가
        observers.add(responseObserver);

        return new StreamObserver<>() { // 양방향 통신이라 StreamObserver 객체 생성
            @Override
            public void onNext(Chat.NotificationRequest request) {
                String roomId = request.getRoomId();
                String userId = request.getUserId();
                String message = request.getMessage();
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                System.out.println("===== 클라이언트 요청 데이터 =====");
                System.out.println("방   : " + roomId);
                System.out.println("유저  : " + userId);
                System.out.println("내용  : " + message);
                System.out.println("시간  : " + timestamp);

                // 응답 생성
                Chat.NotificationResponse response = Chat.NotificationResponse.newBuilder()
                        .setRoomId(roomId)
                        .setUserId(userId)
                        .setMessage(message)
                        .setTimestamp(timestamp)
                        .build();

                // 클라이언트로 응답 전송
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                // 오류 처리
                System.err.println("Error in streamNotifications: " + t.getMessage());
                // 오류 발생 시 클라이언트 스트림 제거
                observers.remove(responseObserver);
            }

            @Override
            public void onCompleted() {
                // 클라이언트가 스트림을 종료했음을 로그로 출력
                System.out.println("Stream completed by client.");
                // 스트림 종료를 클라이언트에 알림
                responseObserver.onCompleted();
                // 스트림 종료 시 클라이언트 제거
                observers.remove(responseObserver);
            }
        };
    }
}
