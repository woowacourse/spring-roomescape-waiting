# 방탈출 예약

## 엔드포인트 정리

---

### 예약 (Reservation) 관련

- [x] `GET /reservations`  
  모든 예약 목록 조회

- [x] `POST /reservations`  
  예약 생성
  - 제약사항:
    - 지나간 날짜와 시간에 대한 예약 생성은 불가능

- [x] `DELETE /reservations/{id}`  
  예약 삭제
- 제약사항:
  - 자신의 예약만 삭제 가능
  - 관리자는 모두 삭제 가능
---

### 예약 시간 (TimeSlot) 관련

- [x] `GET /times`  
  예약 가능 슬롯 목록 조회

- [x] `POST /times`  
  예약 시간 생성 (관리자 전용)
  - 제약사항: 중복 시간 추가는 불가능

- [x] `DELETE /times/{id}`  
  예약 시간 삭제 (관리자 전용)
  - 제약사항: 이미 예약이 존재하는 시간은 삭제 불가능

---

### 테마 (Theme) 관련

- [x] `GET /themes`  
  모든 테마 목록 조회

- [x] `GET /themes/ranking`  
  인기 테마 순위 조회

- [x] `POST /themes`  
  테마 생성 (관리자 전용)

- [x] `DELETE /themes/{id}`  
  테마 삭제 (관리자 전용)
  - 제약사항: 예약이 있는 테마는 삭제 불가능

---

### 관리자 페이지

- [x] `GET /admin` - 어드민 메인 페이지
- [x] `GET /admin/reservation` - 예약 관리용 페이지
- [x] `GET /admin/time` - 예약 시간 관리 페이지 
- [x] `GET /admin/theme` - 테마 관리 페이지

---

### 사용자 페이지

- [x] `GET /` - 인기 테마 페이지
- [x] `GET /reservation` - 사용자 예약 페이지
- [x] `GET /reservation-mine` - 내 예약 조회 페이지
- [x] `GET /sign-in` - 로그인 페이지
- [x] `GET /sign-up` - 회원가입 페이지
