package com.stoock.grpcserver.service;

import com.stoock.grpcserver.GreetProto;
import com.stoock.grpcserver.GreeterGrpc;
//import com.stoock.grpcserver.NotificationRequest;
//import com.stoock.grpcserver.NotificationResponse;
//import com.stoock.grpcserver.SendNotificationRequest;
//import com.stoock.grpcserver.SendNotificationResponse;
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
        System.out.println("📩 Received notification:");
        System.out.println("Room ID: " + request.getRoomId());
        System.out.println("User ID: " + request.getUserId());
        System.out.println("Message: " + request.getMessage());

        GreetProto.SendNotificationResponse response = GreetProto.SendNotificationResponse.newBuilder()
                .setStatus("SUCCESS")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        sendStreamNotification(request.getUserId(), "New message in room " + request.getRoomId());
    }

    @Override
    public StreamObserver<GreetProto.NotificationRequest> streamNotifications(StreamObserver<GreetProto.NotificationResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(GreetProto.NotificationRequest request) {
                System.out.println("📡 Streaming request received:");
                System.out.println("User ID: " + request.getUserId());
                System.out.println("Status: " + request.getStatus());

                activeStreams.computeIfAbsent(request.getUserId(), k -> new CopyOnWriteArrayList<>()).add(responseObserver);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("❌ Streaming error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("✅ Streaming completed.");
            }
        };
    }

    private void sendStreamNotification(String userId, String message) {
        List<StreamObserver<GreetProto.NotificationResponse>> observers = activeStreams.get(userId);
        if (observers != null) {
            GreetProto.NotificationResponse response = GreetProto.NotificationResponse.newBuilder()
                    .setUserId(userId)
                    .setUpdate(message)
                    .build();

            for (StreamObserver<GreetProto.NotificationResponse> observer : observers) {
                observer.onNext(response);
            }
        }
    }
}
