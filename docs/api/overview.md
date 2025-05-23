# 전체 API 목록

### [예약 API](reservation.md)

- 예약 추가: [`POST /reservations`](reservation.md#예약-추가-api)
- 어드민 예약 생성: [`POST /admin/reservations`](reservation.md#어드민-예약-생성-api)
- 어드민 예약 목록 조회: [`GET /reservations`](reservation.md#어드민-예약-목록-조회-api)
- 어드민 필터 예약 목록 조회: [
  `GET /admin/reservations?themeId=1&memberId=1&dateFrom='2023-08-05'&dateTo='2023-08-05'`](reservation.md#어드민-필터-예약-목록-조회-api)
- 내 예약 목록 조회: [`GET /me/reservations`](reservation.md#내-예약-목록-조회-api)
- 예약 취소: [`DELETE /reservations/{id}`](reservation.md#예약-취소-api)

### [시간 API](time.md)

- 시간 추가: [`POST /times`](time.md#시간-추가-api)
- 시간 목록 조회: [`GET /times`](time.md#시간-조회-api)
- 시간 삭제: [`DELETE /times/{id}`](time.md#시간-삭제-api)
- 예약 가능 시간 조회: [`GET /times/availability?date=2025-03-30&themeId=1`](time.md#예약-가능-시간-조회-api)

### [테마 API](theme.md)

- 테마 추가: [`POST /themes`](theme.md#테마-추가-api)
- 테마 목록 조회: [`GET /themes`](theme.md#테마-목록-조회-api)
- 테마 삭제: [`DELETE /themes/{id}`](theme.md#테마-삭제-api)
- 인기 테마 목록 조회: [`GET /popular-themes`](theme.md#인기-테마-목록-조회-api)

### [멤버 API](member.md)

- 회원 가입: [`POST /members`](member.md#회원-가입-api)
- 회원 목록 조회: [`GET /members`](member.md#회원-목록-조회-api)

### [인증 API](auth.md)

- 로그인: [`POST /login`](auth.md#로그인-api)
- 로그인 체크: [`GET /login/check`](auth.md#인증-정보-조회-api)
- 로그아웃: [`POST /logout`](auth.md#로그아웃-api)

### [예약 대기 API](waiting.md)

- 예약 대기 생성: [`POST /waitings`](waiting.md#예약-대기-생성-api)
- 내 예약 대기 취소: [`DELETE /waitings/{id}`](waiting.md#내-예약-대기-취소-api)
- 어드민 예약 대기 목록 조회: [`GET /admin/waitings`](waiting.md#어드민-예약-대기-목록-조회-api)
- 어드민 예약 대기 거절: [`DELETE /admin/waitings/{id}`](waiting.md#어드민-예약-대기-거절-api)
