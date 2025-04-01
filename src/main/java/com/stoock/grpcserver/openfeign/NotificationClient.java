//package com.stoock.grpcserver.openfeign;
//
//import com.stoock.grpcserver.dto.NotificationDto;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(name = "notificationClient", url = "https://front.bmops.org/")
//public interface NotificationClient {
//
//
//
//    @PostMapping("/notifications")
//    void sendNotification(@RequestBody NotificationDto notificationDto);
//}
