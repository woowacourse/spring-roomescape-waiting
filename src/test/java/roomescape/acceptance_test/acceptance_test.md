# 인수 테스트 시나리오

## Theme

- [x] **1. 테마 생성 후 목록에서 조회된다**
  - 요청 흐름
    - `POST /admin/themes`
    - `GET /themes`
  - 검증
    - 생성한 테마가 목록 응답에 포함된다.

- [x] **2. 테마 생성 후 삭제하면 목록에서 사라진다**
  - 요청 흐름
    - `POST /admin/themes`
    - `DELETE /admin/themes/{id}`
    - `GET /themes`
  - 검증
    - 삭제한 테마가 목록 응답에 포함되지 않는다.

- [x] **3. 인기 테마는 기간 내 예약 수가 많은 순서대로 조회된다**
  - 요청 흐름
    - 테마 여러 개 생성
    - 예약 시간 여러 개 생성
    - 기간 내 예약 수가 다르게 예약 생성
    - 기간 밖 예약도 함께 생성
    - `GET /themes/popularity?days=7&size=3`
  - 검증
    - 기간 내 예약 수가 많은 테마 순서대로 조회된다.

## Reservation Time

- [x] **1. 예약 시간 생성 후 목록에서 조회된다**
  - 요청 흐름
    - `POST /admin/times`
    - `GET /times`
  - 검증
    - 생성한 예약 시간이 목록 응답에 포함된다.

- [x] **2. 중복된 예약 시간을 생성하면 400 에러가 발생한다**
  - 요청 흐름
    - `POST /admin/times`로 `10:00` 생성
    - 다시 `POST /admin/times`로 `10:00` 생성
  - 검증
    - `400 Bad Request`가 반환된다.

- [x] **3. 예약된 시간은 예약 가능 시간 목록에서 unavailable로 내려온다**
  - 요청 흐름
    - 테마 생성
    - 예약 시간 2개 생성
    - 특정 날짜에 특정 시간으로 예약 생성
    - `GET /times/availability?date=2023-08-05&themeId={themeId}`
  - 검증
    - 예약된 시간은 `isAvailable=false`로 내려온다.
    - 예약 안 된 시간은 `isAvailable=true`로 내려온다.

## Reservation

- [x] **1. 예약 생성 후 관리자 예약 목록에서 조회된다**
  - 요청 흐름
    - 테마 생성
    - 예약 시간 생성
    - 예약 생성
    - `GET /admin/reservations`
  - 검증
    - 생성한 예약이 목록 응답에 포함된다.

- [x] **2. 예약 삭제 후 관리자 예약 목록에서 사라진다**
  - 요청 흐름
    - 예약 생성
    - `DELETE /admin/reservations/{id}`
    - `GET /admin/reservations`
  - 검증
    - 삭제한 예약이 목록 응답에 포함되지 않는다.
