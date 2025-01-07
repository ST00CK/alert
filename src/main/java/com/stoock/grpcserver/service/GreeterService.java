//package com.stoock.grpcserver.service;  // 패키지 선언
//
//import io.grpc.stub.StreamObserver;  // StreamObserver 클래스 임포트
//import com.stoock.greet.GreetProto;  // GreetProto 클래스 임포트 (gRPC 메시지 정의)
//import org.springframework.grpc.server.service.GrpcService;  // Spring gRPC 서비스 어노테이션 임포트
//import org.springframework.stereotype.Service;  // Spring Service 어노테이션 임포트
//
////@Service  // Spring Service 어노테이션 (주석 처리됨)
//@GrpcService  // 이 클래스가 gRPC 서비스임을 나타내는 어노테이션
//public class GreeterService extends ServerServiceImpl {  // GreeterService 클래스 정의
//
//    @Override  // sayHello 메서드 오버라이드
//    public void sayHello(GreetProto.HelloRequest request, StreamObserver<GreetProto.HelloResponse> responseObserver) {  // sayHello 메서드 정의
//
//        String message = "Hello, " + request.getName();  // 클라이언트에서 받은 이름을 사용해 인사 메시지 생성
//
//        GreetProto.HelloResponse response = GreetProto.HelloResponse.newBuilder()  // HelloResponse 객체 빌드 시작
//                .setMessage(message)  // 메시지 필드에 생성한 인사 메시지 설정
//                .build();  // HelloResponse 객체 완성
//
//        responseObserver.onNext(response);  // 응답을 클라이언트로 전송
//
//        responseObserver.onCompleted();  // 응답 완료
//    }
//}




package com.stoock.grpcserver.service;

import com.stoock.greet.GreetProto;
import com.stoock.greet.GreeterGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class GreeterService extends GreeterGrpc.GreeterImplBase {

    private final List<StreamObserver<GreetProto.Notification>> clients = new ArrayList<>();

    @Override
    public void streamNotifications(GreetProto.Empty request, StreamObserver<GreetProto.Notification> responseObserver) {
        synchronized (clients) {
            clients.add(responseObserver);
        }
        System.out.println("클라이언트 연결 성공, 클라이언트 연결 수 : " + clients.size());
    }

    @Override
    public void sendNotification(GreetProto.Notification request, StreamObserver<GreetProto.Empty> responseObserver) {
        synchronized (clients) {
            for (StreamObserver<GreetProto.Notification> client : new ArrayList<>(clients)) {
                try {
                    client.onNext(request);
                } catch (Exception e) {
                    // 에러 발생 시 클라이언트를 리스트에서 제거
                    clients.remove(client);
                    System.err.println("Error sending to client: " + e.getMessage());
                }
            }
        }

        responseObserver.onNext(GreetProto.Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}


