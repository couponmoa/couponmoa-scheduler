package com.couponmoa.backend.couponmoascheduler.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final RestTemplate restTemplate;

    // TODO: url 서버 명으로 수정
    @Scheduled(cron = "0 0 0 * * *")
    public void expireCouponNotifications() {
        String url = "http://localhost:8080/api/v1/notifications";
        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("만료 전 알림 스케줄러 실행");
            } else {
                System.err.println("API 호출 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
        }
    }
}

