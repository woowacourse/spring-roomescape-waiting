# 0000. WaitlistController 도입 이유

## 상태

Accepted

## 배경

현재 예약 취소와 예약 대기 취소가 모두 `DELETE /reservations/{id}?name=...` 형태의 API로 표현되고 있다.

예약과 예약 대기는 서로 다른 리소스임에도 동일한 HTTP Method, 동일한 Path, 동일한 Query Parameter 조건을 사용하면서 Spring MVC가 어떤 핸들러를 호출해야 하는지 구분할 수 없는
문제가 발생한다.

또한 도메인 관점에서도 예약 대기는 단순히 예약의 부가 상태가 아니라, 별도의 식별자와 생명주기를 가지는 리소스다.
실제로 예약 대기는 예약과 다른 저장소, 다른 취소 규칙, 다른 순번 개념을 가진다.

## 결정

예약 대기 취소 API를 기존 `ReservationController`에 추가하지 않고, 별도의 `WaitlistController`로 분리한다.

예약 취소와 예약 대기 취소는 다음과 같이 서로 다른 리소스 경로를 사용한다.

- 예약 취소: `DELETE /reservations/{id}?name=...`
- 예약 대기 취소: `DELETE /waitlists/{id}?name=...`

`POST /reservations`는 유지한다. 사용자는 예약을 시도하는 것이고, 서버는 예약 가능 여부에 따라 `RESERVED` 또는 `WAITING` 상태를 응답한다.
반면 취소 시점에는 대상 리소스가 예약인지 예약 대기인지 이미 확정되어 있으므로, 취소 API는 리소스별로 분리한다.

## 고려한 대안

### 1. `DELETE /reservations/waitlist/{id}`

기존 `ReservationController` 내부에 예약 대기 취소 API를 추가할 수 있어 변경 범위가 작다.

하지만 `waitlist`가 `reservations`의 하위 리소스처럼 보이기 때문에, `{id}`가 예약 ID인지 예약 대기 ID인지 혼동될 수 있다.
또한 예약 대기 관련 기능이 늘어날수록 `ReservationController`의 책임이 커지고 경계가 흐려진다.

### 2. `DELETE /waitlists/{id}`

예약 대기를 독립적인 리소스로 표현한다.

예약 취소와 예약 대기 취소의 경로가 명확히 분리되며, 이후 예약 대기 목록 조회, 순번 조회, 대기 취소, 대기 승격 등의 기능을 자연스럽게 확장할 수 있다.

## 근거

예약과 예약 대기는 서로 다른 생명주기를 가진다.

예약은 확정된 이용 권한이고, 예약 대기는 특정 예약 슬롯에 대한 대기 상태다. 예약 대기는 순번, 생성 시각, 중복 대기 검증 등 예약과 다른 정책을 가진다.

따라서 API 계층에서도 두 리소스를 분리하는 것이 리소스 표현에 더 적합하다.

또한 컨트롤러를 분리하면 책임이 명확해진다.

- `ReservationController`: 예약 생성, 조회, 수정, 취소
- `WaitlistController`: 예약 대기 조회, 취소, 순번 관련 기능

초기에는 `WaitlistController`가 기존 서비스의 대기 취소 메서드를 호출할 수 있지만, 대기 관련 정책이 늘어나면 `WaitlistService`로 자연스럽게 분리할 수 있다.

## 결과

예약 취소와 예약 대기 취소의 라우팅 충돌이 제거된다.

클라이언트는 예약 생성 응답의 상태값을 기준으로 취소 API를 선택한다.

- `RESERVED` 상태이면 `/reservations/{id}` 호출
- `WAITING` 상태이면 `/waitlists/{id}` 호출

대기 관련 기능이 추가될 때 기존 `ReservationController`가 비대해지는 것을 방지할 수 있다.
