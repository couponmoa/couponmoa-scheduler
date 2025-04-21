package com.couponmoa.backend.couponmoascheduler.controller;

import com.couponmoa.backend.couponmoascheduler.scheduler.NotificationScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationTestController {

    private final NotificationScheduler notificationScheduler;

    @PostMapping("/trigger-expire-notification")
    public ResponseEntity<String> triggerExpireNotification() {
        notificationScheduler.expireCouponNotifications(); // 직접 호출
        return ResponseEntity.ok("스케줄러 직접 실행 완료");
    }
}
