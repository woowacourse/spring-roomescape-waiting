# 1단계 요구사항
- [x] JPA로 전환
  - [x] 연관관계 없는 도메인 entity 전환
  - [x] 연관관계 있는 도메인 맵핑

# 2단계 요구사항
- [x] 내 예약 목록 조회 API 구현
  - [x] `/reservations/mine` GET 요청 시 본인이 예약한 목록을 반환한다.
  - [x] 비 로그인 상태에서 요청 시 401 Unathorized 응답을 반환한다.
  - [x] 내 예약 목록에는 status가 추가되어 있다.
  - 예약 내역 응답은 다음과 같이 구성되어 있다.
    - reservationId : 예약 id
    - theme : 테마명
    - date : 예약 일자 (YYYY-MM-DD)
    - time : 예약 시간 (HH:mm)
    - status : 예약 상태
      1. 예약
- [x] 내 예약 목록 페이지 호출 시 GET /reservation/mine 요청되고, reservation-mine.html 페이지를 응답한다.
