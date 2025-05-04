# ⏰ couponmoa-scheduler

## 📌 개요

`couponmoa-scheduler`는 **쿠폰 발급/사용 통계 집계, 쿠폰 상태 갱신, 쿠폰 만료 알림 등 주기적으로 실행되는 작업을 담당하는 배치/스케줄링 전용 서버**입니다.

Spring Batch, JobRunr, Spring Scheduler 등을 활용하여 안정적이고 자동화된 작업 처리를 목표로 합니다.

---

## ⚙️ 주요 기능

### 🗂 쿠폰 통계 집계
- 매일 새벽 6시에 전일 사용된 쿠폰 정보를 통계로 집계
- 대량 데이터를 효율적으로 처리하기 위해 **Spring Batch + 파티셔닝 + PagingItemReader** 사용
- 결과는 `coupon_daily_usage_stats` 테이블에 저장

### ♻️ 쿠폰 상태 자동 전환

- **30분마다** 다음 작업 수행:
  - 발급 가능 쿠폰 활성화
  - 유효기간이 지난 쿠폰 비활성화
  - Redis 캐시 상태와 DB 재고 수량 동기화

### 📬 쿠폰 만료 알림 트리거

- **매일 자정**, 만료 하루 전 사용자 쿠폰을 찾아 API 서버에 알림 요청
- API 서버가 알림 대상 조회 → 알림 서버가 이메일 전송

### 🔚 사용자 쿠폰 만료 처리

- **매일 자정**, 만료일이 지난 사용자 쿠폰 상태를 일괄 변경

---

## 🔧 사용 기술

- **Spring Batch**: 쿠폰 사용 통계 집계 배치
- **Spring Scheduler**: 쿠폰 상태 갱신/만료 등 반복 작업 처리
- **RestTemplate**: 서버 간 API 통신 (알림 트리거용)
- **JDBC / Redis**: 상태 조회 및 캐시 연동
- **JobLauncher + JobRegistry**: Spring Batch 잡 실행 관리

---

## 📡 시스템 흐름 예시

1. **CouponScheduler**  
   - DB 조회 → Redis 업데이트 → 쿠폰 상태 전환  
2. **CouponStatsScheduler**  
   - JobLauncher 실행 → 쿠폰 사용 통계 저장  
3. **NotificationScheduler**  
   - 알림 API 호출 → 알림 서버가 이메일 전송  
4. **UserCouponScheduler**  
   - DB에서 만료 쿠폰 상태 변경  

---
