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

- [x] **2. 중복된 예약 시간을 생성하면 에러가 발생한다**
  - 요청 흐름
    - `POST /admin/times`로 `10:00` 생성
    - 다시 `POST /admin/times`로 `10:00` 생성
  - 검증
    - `400 Bad Request`가 반환된다.

- [x] **3. 특정 날짜와 테마의 예약 가능한 시간을 조회한다**
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

- [x] **3. 특정 사용자의 이름을 입력해 예약을 조회한다.**
  - 요청 흐름
    - 특정 사용자 이름을 넣어 예약 생성
    - `GET /reservations?guestName=<사용자 이름>`
  - 검증
    - 조회한 예약 정보의 사용자 이름이 요청한 사용자의 이름이다.

- [x] **4. 예약의 날짜와 시간을 수정한다.**
  - 요청 흐름
    - 테마 생성
    - 기존 예약 시간 생성
    - 변경할 예약 시간 생성
    - 예약 생성
    - `PATCH /reservations/{id}`
  - 검증
    - 예약 날짜와 시간이 요청한 값으로 변경된다.
    - 테마는 기존 예약의 테마로 유지된다.

- [x] **5. 수정하려는 날짜와 시간에 같은 테마의 예약이 존재하면 에러가 발생한다.**
  - 요청 흐름
    - 같은 테마로 예약 2개 생성
    - 한 예약을 다른 예약의 날짜와 시간으로 수정 요청
    - `PATCH /reservations/{id}`
  - 검증
    - `409 Conflict`가 반환된다.

- [x] **6. 이미 시작된 예약은 수정할 수 없다.**
  - 요청 흐름
    - 예약 생성
    - 현재 시간을 예약 시작 이후로 변경
    - `PATCH /reservations/{id}`
  - 검증
    - `422 Unprocessable Entity`가 반환된다.

- [x] **7. 이미 지난 날짜와 시간으로 예약을 수정할 수 없다.**
  - 요청 흐름
    - 예약 생성
    - 수정하려는 날짜와 시간이 현재 시간보다 과거가 되도록 설정
    - `PATCH /reservations/{id}`
  - 검증
    - `422 Unprocessable Entity`가 반환된다.

- [x] **8. 본인의 예약이 아니면 수정할 수 없다.**
  - 요청 흐름
    - 다른 이름으로 예약 2개 생성
    - 다른 사람의 예약 수정 시도
    - `PATCH /reservations/{id}`
  - 검증
    - `403 Forbidden`이 반환된다.

- [x] **9. 본인의 예약을 삭제한다.**
  - 요청 흐름
    - 특정 사용자 이름으로 예약 생성
    - `Authorization` 헤더에 사용자 이름을 담아 `DELETE /reservations/{id}`
    - `GET /reservations?guestName=<사용자 이름>`
  - 검증
    - 삭제 요청 시 `204 No Content`가 반환된다.
    - 삭제한 예약이 사용자 예약 목록에 포함되지 않는다.
