package com.stoock.grpcserver.service;

import chat.ChatServiceGrpc;
import chat.Chat;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import com.stoock.grpcserver.dto.NotificationDto;
//import com.stoock.grpcserver.openfeign.NotificationClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@GrpcService
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {

    // 클라이언트들에게 응답을 전송하기 위한 Observer 리스트 (멀티스레드 환경을 고려해 CopyOnWriteArrayList 사용)
    private final List<StreamObserver<Chat.NotificationResponse>> observers = new CopyOnWriteArrayList<>();

//    private final NotificationClient notificationClient;
//    public ChatService(NotificationClient notificationClient) {
//        this.notificationClient = notificationClient;
//    }

    @Override
    public StreamObserver<Chat.NotificationRequest> streamNotifications(StreamObserver<Chat.NotificationResponse> responseObserver) {
        System.out.println("streamNotifications: 클라이언트와 연결됨");

        observers.add(responseObserver);

        // gRPC 스트림을 처리하는 익명 StreamObserver 객체 반환
        return new StreamObserver<>() {
            @Override
            public void onNext(Chat.NotificationRequest request) {
                // 클라이언트로부터 받은 데이터 추출
                String roomId = request.getRoomId();
                String userId = request.getUserId();
                String message = request.getMessage();
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // 로그 출력 (서버에서 요청 데이터 확인)
                System.out.println("===== 클라이언트 요청 데이터 =====");
                System.out.println("방   : " + roomId);
                System.out.println("유저  : " + userId);
                System.out.println("내용  : " + message);
                System.out.println("시간  : " + timestamp);

                NotificationDto dto = new NotificationDto(roomId, userId, message, timestamp);
//                notificationClient.sendNotification(dto);
                Chat.NotificationResponse notification = Chat.NotificationResponse.newBuilder()
                        .setRoomId(dto.getRoomId())
                        .setUserId(dto.getUserId())
                        .setMessage(dto.getMessage())
                        .setTimestamp(dto.getTimestamp())
                        .build();

                // 등록된 모든 클라이언트에게 메시지 전송
                for (StreamObserver<Chat.NotificationResponse> observer : observers) {
                    observer.onNext(notification);
                }
            }

            @Override
            public void onError(Throwable t) {
                // 오류 발생 시 로그 출력 및 Observer 제거
                System.err.println("Error in streamNotifications: " + t.getMessage());
                observers.remove(responseObserver);
            }

            @Override
            public void onCompleted() {
                // 클라이언트가 스트림을 종료하면 Observer에서 제거
                System.out.println("클라이언트가 스트림 종료.");
                responseObserver.onCompleted();
                observers.remove(responseObserver);
            }
        };
    }
}
