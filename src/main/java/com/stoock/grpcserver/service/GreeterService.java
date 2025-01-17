package com.stoock.grpcserver.service;

import com.stoock.grpcserver.GreetProto;
import com.stoock.grpcserver.GreeterGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@GrpcService
public class GreeterService extends GreeterGrpc.GreeterImplBase {

    private final Map<String, List<StreamObserver<GreetProto.NotificationResponse>>> activeStreams = new ConcurrentHashMap<>();

    @Override
    public void sendNotification(GreetProto.SendNotificationRequest request, StreamObserver<GreetProto.SendNotificationResponse> responseObserver) {
        // 알림을 보내는 메서드. SendNotificationRequest를 받고 SendNotificationResponse를 반환

        System.out.println("📩 Received notification:"); // 알림 수신 로그 출력
        System.out.println("Room ID: " + request.getRoomId()); // 요청에서 방 ID 출력
        System.out.println("User ID: " + request.getUserId()); // 요청에서 사용자 ID 출력
        System.out.println("Message: " + request.getMessage()); // 요청에서 메시지 내용 출력

        GreetProto.SendNotificationResponse response = GreetProto.SendNotificationResponse.newBuilder()
                .setStatus("SUCCESS") // 응답의 상태를 "SUCCESS"로 설정
                .build(); // 응답 객체 빌드

        responseObserver.onNext(response); // 클라이언트에게 응답 전송
        responseObserver.onCompleted(); // 응답 전송 완료

        sendStreamNotification(request.getUserId(), "New message in room " + request.getRoomId());
        // 사용자에게 실시간 알림을 보내는 메서드 호출
    }

    @Override
    public StreamObserver<GreetProto.NotificationRequest> streamNotifications(StreamObserver<GreetProto.NotificationResponse> responseObserver) {
        // 스트리밍 방식으로 알림을 받는 메서드

        return new StreamObserver<>() { // StreamObserver 인터페이스 구현

            @Override
            public void onNext(GreetProto.NotificationRequest request) {
                // 새로운 스트리밍 요청이 올 때마다 호출

                System.out.println("📡 Streaming request received:"); // 스트리밍 요청 수신 로그 출력
                System.out.println("Room ID: " + request.getRoomId()); // 요청에서 방 ID 출력
                System.out.println("User ID: " + request.getUserId()); // 요청에서 사용자 ID 출력
                System.out.println("Message: " + request.getMessage()); // 요청에서 메시지 출력
                System.out.println("Status: " + request.getStatus()); // 요청에서 상태 출력

                activeStreams.computeIfAbsent(request.getUserId(), k -> new CopyOnWriteArrayList<>()).add(responseObserver);
                // 사용자 ID별로 응답 스트림을 관리, 존재하지 않으면 새로 추가
            }

            @Override
            public void onError(Throwable t) {
                // 스트리밍 중 에러 발생 시 호출
                System.err.println("❌ Streaming error: " + t.getMessage()); // 에러 로그 출력
            }

            @Override
            public void onCompleted() {
                // 스트리밍 완료 시 호출
                System.out.println("✅ Streaming completed."); // 완료 로그 출력
            }
        };
    }

    private void sendStreamNotification(String userId, String message) {
        // 사용자에게 실시간 알림을 보내는 메서드

        List<StreamObserver<GreetProto.NotificationResponse>> observers = activeStreams.get(userId);
        // 해당 사용자 ID에 해당하는 활성 스트림을 가져옴
        if (observers != null) { // 스트림이 존재하는 경우

            GreetProto.NotificationResponse response = GreetProto.NotificationResponse.newBuilder()
                    .setUserId(userId) // 사용자 ID 설정
                    .setUpdate(message) // 알림 메시지 설정
                    .build(); // 알림 응답 객체 빌드

            for (StreamObserver<GreetProto.NotificationResponse> observer : observers) {
                observer.onNext(response); // 각 스트림에 알림을 전송
            }
        }
    }
}
