# 2-1. GET /reservations-mine — 내 예약 목록

인증된 사용자의 CONFIRMED + WAITING 예약을 합산해 반환하는 API를 구현합니다.

## 체크리스트

- [x] #1 `MyReservation` 도메인 레코드 + `findAllMine` 서비스 메서드 + 단위 테스트
  `MyReservation(Reservation, Long waitingNumber)` 도메인 레코드 생성. `ReservationService.findAllMine(username)`에서 CONFIRMED + WAITING 예약을 합산해 반환. waitingNumber는 CONFIRMED이면 null, WAITING이면 `countWaitingBefore + 1`. `ReservationServiceTest`에 단위 테스트 추가.

- [x] #2 `MyReservationResponse` DTO + `GET /reservations-mine` 엔드포인트 + 통합 테스트
  `MyReservationResponse`(id, date, themeName, themeDescription, themeThumbnailUrl, time, status, waitingNumber) 생성. `ReservationController`에 `GET /reservations-mine?username=` 엔드포인트 추가. `ReservationControllerTest`에 통합 테스트 추가.
