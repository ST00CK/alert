package com.stoock.grpcserver.config;

import com.stoock.grpcserver.service.ChatService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Component
public class GrpcServerRunner {
    private final ChatService chatService;
    private Server server;

    public GrpcServerRunner(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                server = ServerBuilder.forPort(9091)
                        .addService(chatService)
                        .build()
                        .start();
                System.out.println("✅ gRPC Server started on port 9091...");
                server.awaitTermination(); // 기존 `awaitTermination()`을 별도 쓰레드에서 실행
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start(); // 새로운 쓰레드에서 실행
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
            System.out.println("gRPC Server stopped");
        }
    }
}
