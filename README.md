### 기능 요구 사항
- 예약 대기 생성 API
  - POST reservations/waiting
  - body: CreateReservationRequest랑 동일함
  
- 예약 대기 취소 API
  - DELETE reservations/waiting/{id}

- 사용자의 예약 및 대기 목록 조회 API
  - (기존) GET reservations/
  - header - jwt토큰 속 user_id
  - 할일: 기존 예약 조회 API의 반환 값 수정
  
- 예약 취소 로직 수정
  - 예약 취소시 1번 대기자 예약 자동 반영

### 클라이언트
- 예약 대기 창 만들기
