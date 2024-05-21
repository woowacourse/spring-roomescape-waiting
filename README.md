# 1단계 요구사항
- [x] JPA로 전환
  - [x] 연관관계 없는 도메인 entity 전환
  - [x] 연관관계 있는 도메인 맵핑

# 2단계 요구사항
- [x] 내 예약 목록 조회 API 구현
  - [x] `/reservations/mine` GET 요청 시 본인이 예약한 목록을 반환한다.
  - [x] 예약 내역에 status를 추가한다.
  - [x] 내 예약 목록 호출 시 GET /reservation-mine 요청되고, reservation-mine.html 페이지를 응답한다.

# 3단게 요구사항
## 예약
- [x] 사용자는 중복 예약을 할 수 없다.

## 예약 대기
- [x] 사용자는 예약 대기 요청을 할 수 있다.

## 내 예약 목록 조회
- [x] 사용자는 내 예약 대기가 몇 번째인지 확인할 수 있다.
- [x] 사용자는 예약 대기 요청을 취소할 수 있다.

# 4단계 요구사항
## 어드민 예약 대기 관리
- [x] 관리자는 예약 대기 목록을 조회할 수 있다.
- [x] 관리자는 예약 대기를 취소시킬 수 있다.
- [x] 관리자는 예약 대기를 승인할 수 있다.
  - [x] 확정된 예약이 없고 1번째 예약 대기인 경우에만 수동으로 승인할 수 있다.
