# 예약 관련 API 명세서 (Reservation API)

본 문서는 `roomescape.reservation.controller` 패키지에 정의된 예약 관련 API의 요청 및 응답(성공/실패 케이스)을 정리한 명세서입니다.

---

## 0. 공통 인증 정책 (Authentication)

`roomescape.auth` 패키지 구성 요소(`AuthInterceptor`, `OwnerOnlyArgumentResolver` 등)에 의해, 특정 사용자 권한이 필요한 API는 공통적인 인증/인가 절차를 거칩니다.

* **인증 방식**: HTTP 헤더에 `Authorization` 키 값으로 **사용자 이름(String)** 을 직접 전달하여 로그인 유저를 식별합니다. (예: `Authorization: brown`)
* **예외 처리 (401 Unauthorized)**: `@Authorized`가 선언된 API(예: 본인 예약 수정/취소) 호출 시, `Authorization` 헤더가 누락되거나 비어있으면 `401 Unauthorized` 상태 코드를 반환합니다.
* **예외 처리 (403 Forbidden)**: 로그인한 사용자(`@OwnerOnly`를 통해 주입된 정보)와 해당 리소스(예약)의 실제 소유자가 일치하지 않을 경우 `403 Forbidden` 상태 코드를 반환합니다.

---

## 1. 사용자의 예약 관리 (ReservationController & MyReservationController)

### 1.1 예약 생성 (POST `/reservations`)
사용자가 날짜, 시간, 테마를 선택하여 새로운 예약을 생성합니다.

* **Request**
    * Method: `POST`
    * Path: `/reservations`
    * Body (JSON):
      ```json
      {
        "name": "사용자이름",
        "date": "2024-05-15",
        "timeId": 1,
        "themeId": 1
      }
      ```
* **Response**
    * **[성공] 201 Created**
        * Location 헤더: `/reservations/{id}`
        * Body (JSON): 예약된 정보(`id`, `name`, `date`, `time`, `theme`) 반환
    * **[실패] 400 Bad Request**
        * 사유: 잘못된 입력값이 전달되었거나 이미 예약된 시간/테마인 경우 (중복 예약)
    * **[실패] 404 Not Found**
        * 사유: `timeId`나 `themeId`에 해당하는 데이터가 존재하지 않는 경우

### 1.2 내 예약 조회 (GET `/reservations`)
주어진 이름(name)을 통해 예약 내역 및 대기 내역을 조회합니다. 예약과 대기 상태(`status`)가 포함됩니다.

* **Request**
    * Method: `GET`
    * Path: `/reservations`
    * Query Parameter: `name` (조회할 사용자의 이름)
* **Response**
    * **[성공] 200 OK**
        * Body (JSON): 예약/대기 목록 (상태 `reserved` 또는 `waiting` 포함)

### 1.3 내 예약 수정 (PATCH `/reservations/{id}`)
예약 정보를 부분적으로 수정(날짜, 시간 등)합니다. 본인 소유의 예약만 수정할 수 있습니다.

* **Request**
    * Method: `PATCH`
    * Path: `/reservations/{id}`
    * Headers:
        * `Authorization`: "사용자이름" (본인 인증)
    * Body (JSON):
      ```json
      {
        "date": "2024-05-20",
        "timeId": 2
      }
      ```
* **Response**
    * **[성공] 204 No Content**
    * **[실패] 401 Unauthorized**
        * 사유: `Authorization` 헤더가 없는 경우
    * **[실패] 403 Forbidden**
        * 사유: `Authorization`에 담긴 이름과 예약의 소유자 이름이 일치하지 않는 경우
    * **[실패] 404 Not Found**
        * 사유: 수정할 대상을 찾지 못한 경우
    * **[실패] 409 Conflict / 400 Bad Request**
        * 사유: 변경하려는 시간대에 이미 다른 예약이 존재하는 경우

### 1.4 내 예약 취소 (DELETE `/reservations/{id}`)
예약을 취소합니다. 본인 소유의 예약만 취소할 수 있습니다. (예약 취소 시 대기 1순위가 자동으로 예약 승급됩니다.)

* **Request**
    * Method: `DELETE`
    * Path: `/reservations/{id}`
    * Headers:
        * `Authorization`: "사용자이름" (본인 인증)
* **Response**
    * **[성공] 204 No Content**
    * **[실패] 401 Unauthorized**
        * 사유: `Authorization` 헤더가 없는 경우
    * **[실패] 403 Forbidden**
        * 사유: `Authorization`에 담긴 이름과 예약의 소유자 이름이 일치하지 않는 경우
    * **[실패] 404 Not Found**
        * 사유: 삭제할 대상을 찾지 못한 경우

---

## 2. 관리자의 예약 관리 (ReservationAdminController)

### 2.1 전체 예약 조회 (GET `/admin/reservations`)
관리자용 기능으로 시스템 상의 모든 예약 내역을 조회합니다.

* **Request**
    * Method: `GET`
    * Path: `/admin/reservations`
* **Response**
    * **[성공] 200 OK**
        * Body (JSON): 전체 예약 정보 리스트 반환

### 2.2 예약 강제 삭제 (DELETE `/admin/reservations/{id}`)
관리자용 기능으로 소유자 확인 절차 없이 특정 예약을 강제로 취소시킵니다. (예약 취소 시 대기 1순위가 자동으로 예약 승급됩니다.)

* **Request**
    * Method: `DELETE`
    * Path: `/admin/reservations/{id}`
* **Response**
    * **[성공] 204 No Content**
    * **[실패] 404 Not Found**
        * 사유: 삭제할 대상을 찾지 못한 경우

---

## 3. 예약 시간 관리 (ReservationTimeController)

### 3.1 전체 예약 시간 목록 조회 (GET `/times`)
시스템에 등록된 모든 시간 슬롯을 조회합니다.

* **Request**
    * Method: `GET`
    * Path: `/times`
* **Response**
    * **[성공] 200 OK**
        * Body (JSON):
          ```json
          [
            {
              "id": 1,
              "startAt": "10:00:00"
            }
          ]
          ```

### 3.2 예약 가능한 시간 조회 (GET `/times/available-times`)
특정 날짜와 테마에 대해 존재하는 모든 시간 슬롯을 반환합니다. 
이미 누군가 예약을 완료한 슬롯일 경우 해당 슬롯의 `alreadyBooked` 값이 `true`로 반환되며, 사용자는 해당 슬롯에 대해 **예약 대기**를 신청할 수 있습니다.

* **Request**
    * Method: `GET`
    * Path: `/times/available-times`
    * Query Parameters:
        * `date`: 조회할 날짜 (ex. `2024-05-15`)
        * `themeId`: 조회할 테마 ID (ex. `1`)
* **Response**
    * **[성공] 200 OK**
        * Body (JSON): 해당 테마/날짜의 전체 시간 리스트 반환
        * 예시:
          ```json
          [
            {
              "id": 1,
              "startAt": "10:00:00",
              "alreadyBooked": false
            },
            {
              "id": 2,
              "startAt": "11:00:00",
              "alreadyBooked": true
            }
          ]
          ```
    * **[실패] 400 Bad Request**
        * 사유: 필수 파라미터(`date` 또는 `themeId`)가 누락된 경우

### 3.3 [관리자] 예약 시간 생성 (POST `/admin/times`)
관리자 권한으로 새로운 시간 슬롯을 생성합니다.

* **Request**
    * Method: `POST`
    * Path: `/admin/times`
    * Body (JSON):
      ```json
      {
        "startAt": "14:00"
      }
      ```
* **Response**
    * **[성공] 201 Created**
        * Location 헤더: `/times/{id}`
        * Body (JSON): 생성된 시간 정보(`id`, `startAt`) 반환
    * **[실패] 400 Bad Request**
        * 사유: 시간 포맷이 올바르지 않거나 값이 누락된 경우 (`InvalidRequestFormatException`)

### 3.4 [관리자] 예약 시간 삭제 (DELETE `/admin/times/{id}`)
관리자 권한으로 특정 시간 슬롯을 삭제합니다. 만약 해당 시간을 참조하고 있는 예약이나 대기가 있을 경우 삭제할 수 없습니다.

* **Request**
    * Method: `DELETE`
    * Path: `/admin/times/{id}`
* **Response**
    * **[성공] 204 No Content**
    * **[실패] 400 Bad Request** (또는 500)
        * 사유: 외래키 제약조건 위반으로 삭제 실패 (해당 시간에 예약이 존재함)

---

## 4. 예약 대기 관리 (ReservationWaitingController)

### 4.1 예약 대기 생성 (POST `/reservations-waitings`)
이미 예약된 시간(슬롯)에 대해 예약 대기를 신청합니다.

* **Request**
    * Method: `POST`
    * Path: `/reservations-waitings`
    * Body (JSON):
      ```json
      {
        "name": "사용자이름",
        "date": "2024-05-15",
        "timeId": 1,
        "themeId": 1
      }
      ```
* **Response**
    * **[성공] 201 Created**
        * Location 헤더: `/reservations-waitings/{id}`
        * Body (JSON): 대기 예약 정보(`id`, `name`, `date`, `time`, `theme`) 반환
    * **[실패] 400 Bad Request**
        * 사유: 잘못된 입력값이 전달되었거나 필수값이 누락된 경우

### 4.2 내 예약 대기 취소 (DELETE `/reservations-waitings/{id}`)
본인이 신청한 예약 대기를 취소합니다.

* **Request**
    * Method: `DELETE`
    * Path: `/reservations-waitings/{id}`
    * Headers:
        * `Authorization`: "사용자이름" (본인 인증)
* **Response**
    * **[성공] 204 No Content**
    * **[실패] 401 Unauthorized**
        * 사유: `Authorization` 헤더가 없는 경우
    * **[실패] 403 Forbidden**
        * 사유: `Authorization`에 담긴 이름과 대기 예약의 소유자 이름이 일치하지 않는 경우
    * **[실패] 404 Not Found**
        * 사유: 삭제할 대기 예약을 찾지 못한 경우

---

## 5. 테마 관리 (ThemeController & ThemeAdminController)

### 5.1 전체 테마 목록 조회 (GET `/themes`)
시스템에 등록된 모든 테마 목록을 조회합니다.

* **Request**
    * Method: `GET`
    * Path: `/themes`
* **Response**
    * **[성공] 200 OK**
        * Body (JSON): 전체 테마 정보 리스트 반환
        * 예시:
          ```json
          [
            {
              "id": 1,
              "name": "우아한 테마",
              "description": "우아한테크코스 전용 테마입니다.",
              "thumbnailUrl": "https://example.com/image.png"
            }
          ]
          ```

### 5.2 인기 테마 조회 (GET `/themes?popular=true`)
특정 기간 동안 가장 많이 예약된 인기 테마 목록을 조회합니다.

* **Request**
    * Method: `GET`
    * Path: `/themes`
    * Query Parameters:
        * `popular`: `true`
        * `period`: 조회 기준이 될 최근 기간(일) (ex. `7`)
        * `limit`: 반환받을 인기 테마 최대 개수 (ex. `10`)
* **Response**
    * **[성공] 200 OK**
        * Body (JSON): 조건에 맞는 인기 테마 정보 리스트 반환
    * **[실패] 400 Bad Request**
        * 사유: `period` 또는 `limit` 값이 1 미만으로 잘못 전달된 경우

### 5.3 [관리자] 테마 생성 (POST `/admin/themes`)
새로운 방탈출 테마를 생성합니다.

* **Request**
    * Method: `POST`
    * Path: `/admin/themes`
    * Body (JSON):
      ```json
      {
        "name": "새로운 테마",
        "description": "새로 추가되는 테마에 대한 설명입니다.",
        "thumbnailUrl": "https://example.com/new-theme.png"
      }
      ```
* **Response**
    * **[성공] 201 Created**
        * Location 헤더: `/themes/{id}`
        * Body (JSON): 생성된 테마 정보(`id`, `name`, `description`, `thumbnailUrl`) 반환
    * **[실패] 400 Bad Request**
        * 사유: 이름이나 설명, URL 등 필수 입력값이 누락되거나 비어 있는 경우

### 5.4 [관리자] 테마 삭제 (DELETE `/admin/themes/{id}`)
등록된 특정 방탈출 테마를 삭제합니다.

* **Request**
    * Method: `DELETE`
    * Path: `/admin/themes/{id}`
* **Response**
    * **[성공] 204 No Content**
