# spring-roomescape-waiting

방탈출 예약 시스템입니다.  
사용자는 회원가입 및 세션 로그인 후 테마와 날짜를 기준으로 예약 슬롯을 조회하고, `slotId`를 이용하여 예약을 생성/변경/취소(대기 등록 포함)합니다.  
관리자는 세션 로그인을 통해 인증받으며(사용자 역할이 `ADMIN`이어야 함), 테마, 예약 시간, 예약 현황을 조회 및 수정, 생성, 삭제할 수 있습니다.

---

## 화면 (View)

- **사용자 페이지**: `GET /` (로그인, 회원가입, 테마 및 슬롯 조회, 예약 신청/변경/취소 기능 제공)
- **관리자 페이지**: `GET /admin` (테마 관리, 예약 시간 관리, 전체 예약 관리 기능 제공)

---

## 인증 (Authentication)

### `POST /signup`
새로운 사용자로 회원가입하고 자동으로 세션에 로그인 상태를 저장합니다.
- **요청 바디**:
  ```json
  {
    "name": "보예",
    "password": "password"
  }
  ```
- **응답 (201 Created)**:
  - Header: `Location: /users/{userId}`
  - Body:
    ```json
    {
      "id": 1,
      "name": "보예",
      "role": "USER"
    }
    ```

### `POST /login`
아이디(이름)와 비밀번호로 로그인하고 세션에 로그인 상태를 저장합니다.
- **요청 바디**:
  ```json
  {
    "name": "보예",
    "password": "password"
  }
  ```
- **응답 (200 OK)**:
  ```json
  {
    "id": 1,
    "name": "보예",
    "role": "USER"
  }
  ```

### `DELETE /logout`
현재 활성화된 로그인 세션을 만료하고 로그아웃 처리합니다.
- **응답 (204 No Content)**: (본문 없음)

---

## 사용자 예약 (User Reservations)

### `GET /reservations`
현재 로그인한 사용자의 예약 및 예약 대기 목록을 조회합니다.
- **응답 (200 OK)**:
  ```json
  {
    "username": "보예",
    "reservations": [
      {
        "id": 29,
        "slot": {
          "id": 5,
          "date": "2026-05-01",
          "startAt": {
            "id": 2,
            "startAt": "11:00"
          },
          "theme": {
            "id": 3,
            "name": "청춘물",
            "content": "학교 배경인 테마 입니다.",
            "url": "/themes/youth"
          }
        },
        "waitingNumber": 1,
        "status": "WAITING"
      }
    ]
  }
  ```

### `POST /reservations`
선택한 슬롯에 새로운 예약을 등록하거나 대기 신청을 진행합니다.
- **요청 바디**:
  ```json
  {
    "slotId": 20
  }
  ```
- **응답 (201 Created)**:
  - Header: `Location: /reservations/{reservationId}`
  - Body:
    ```json
    {
      "id": 29,
      "date": "2026-05-01",
      "startAt": "11:00",
      "theme": {
        "id": 3,
        "name": "청춘물",
        "content": "학교 배경인 테마 입니다.",
        "url": "/themes/youth"
      }
    }
    ```

### `PATCH /reservations/{id}`
지정된 예약 ID의 슬롯을 변경합니다.
- **요청 바디**:
  ```json
  {
    "slotId": 21
  }
  ```
- **응답 (200 OK)**:
  ```json
  {
    "id": 29,
    "date": "2026-05-01",
    "startAt": "12:00",
    "theme": {
      "id": 3,
      "name": "청춘물",
      "content": "학교 배경인 테마 입니다.",
      "url": "/themes/youth"
    }
  }
  ```

### `DELETE /reservations/{id}`
본인이 생성한 예약을 취소합니다.
- **응답 (204 No Content)**: (본문 없음)

---

## 예약 슬롯 조회 (Reservation Slots)

### `GET /reservation-slots?themeId={themeId}&date={date}`
특정 테마와 날짜의 예약 가능한 슬롯 목록 및 현재 대기 순번을 조회합니다.
- **요청 파라미터**:
  - `themeId` (Long): 테마 고유 ID
  - `date` (String, yyyy-MM-dd): 조회할 날짜
- **응답 (200 OK)**:
  ```json
  {
    "reservationSlots": [
      {
        "slotId": 1,
        "timeId": 1,
        "startAt": "10:00",
        "waitingNumber": 0
      },
      {
        "slotId": 2,
        "timeId": 2,
        "startAt": "11:00",
        "waitingNumber": 1
      }
    ]
  }
  ```

---

## 테마 (Themes)

### `GET /themes`
전체 테마 목록을 조회합니다.
- **응답 (200 OK)**:
  ```json
  {
    "themes": [
      {
        "id": 1,
        "name": "공포",
        "content": "오금이 저리는 공포입니다.",
        "url": "/themes/scary"
      }
    ]
  }
  ```

### `GET /themes/rank`
최근 7일 기준 예약 완료(BOOKED) 기록이 가장 많은 인기 테마 랭킹을 조회합니다.
- **응답 (200 OK)**:
  ```json
  {
    "popularThemes": [
      {
        "id": 1,
        "name": "공포",
        "thumbnailUrl": "/themes/scary",
        "rank": 1
      }
    ]
  }
  ```

### `GET /admin/themes`
관리자용 테마 목록을 조회합니다. (로그인 세션 필요)
- **응답 (200 OK)**:
  ```json
  {
    "themes": [
      {
        "id": 1,
        "name": "공포",
        "content": "오금이 저리는 공포입니다.",
        "url": "/themes/scary"
      }
    ]
  }
  ```

### `POST /admin/themes`
관리자가 새로운 테마를 생성합니다. (로그인 세션 필요)
- **요청 바디**:
  ```json
  {
    "name": "추리",
    "content": "단서를 조합해 탈출하는 테마입니다.",
    "thumbnailUrl": "/themes/detective"
  }
  ```
- **응답 (201 Created)**:
  - Header: `Location: /themes/{themeId}`
  - Body:
    ```json
    {
      "id": 2,
      "name": "추리",
      "content": "단서를 조합해 탈출하는 테마입니다.",
      "url": "/themes/detective"
    }
    ```

### `DELETE /admin/themes/{id}`
지정된 ID의 테마를 삭제합니다. (로그인 세션 필요)
- **응답 (204 No Content)**: (본문 없음)

---

## 예약 시간 (Reservation Times)

### `GET /admin/times`
전체 예약 시간 정보 목록을 조회합니다. (로그인 세션 필요)
- **응답 (200 OK)**:
  ```json
  {
    "times": [
      {
        "id": 1,
        "startAt": "10:00"
      },
      {
        "id": 2,
        "startAt": "11:00"
      }
    ]
  }
  ```

### `POST /admin/times`
예약 가능한 시작 시간을 생성합니다. (로그인 세션 필요)
- **요청 바디**:
  ```json
  {
    "startAt": "18:00"
  }
  ```
- **응답 (201 Created)**:
  - Header: `Location: /times/{timeId}`
  - Body:
    ```json
    {
      "id": 3,
      "startAt": "18:00"
    }
    ```

### `DELETE /admin/times/{id}`
지정된 ID의 예약 시간을 삭제합니다. (로그인 세션 필요)
- **응답 (204 No Content)**: (본문 없음)

---

## 관리자 예약 관리 (Admin Reservations)

### `GET /admin/reservations`
시스템 내에 등록된 전체 사용자 예약 및 대기 현황 목록을 조회합니다. (로그인 세션 필요)
- **응답 (200 OK)**:
  ```json
  {
    "reservations": [
      {
        "id": 1,
        "slot": {
          "id": 1,
          "date": "2026-05-01",
          "startAt": {
            "id": 1,
            "startAt": "11:00"
          },
          "theme": {
            "id": 1,
            "name": "공포",
            "content": "오금이 저리는 공포입니다.",
            "url": "/themes/scary"
          }
        },
        "username": "보예",
        "waitingNumber": 0,
        "status": "BOOKED"
      }
    ]
  }
  ```

### `POST /admin/reservations`
관리자 권한으로 특정 사용자 이름과 슬롯을 지정하여 예약을 강제 추가하거나 대기로 등록합니다. (로그인 세션 필요)
- **요청 바디**:
  ```json
  {
    "username": "보예",
    "slotId": 20
  }
  ```
- **응답 (201 Created)**:
  - Header: `Location: /reservations/{reservationId}`
  - Body:
    ```json
    {
      "id": 30,
      "date": "2026-05-01",
      "startAt": "11:00",
      "theme": {
        "id": 1,
        "name": "공포",
        "content": "오금이 저리는 공포입니다.",
        "url": "/themes/scary"
      }
    }
    ```

### `PATCH /admin/reservations/{id}`
특정 예약 내역의 슬롯 정보를 강제 변경합니다. (로그인 세션 필요)
- **요청 바디**:
  ```json
  {
    "slotId": 21
  }
  ```
- **응답 (200 OK)**:
  ```json
  {
    "id": 30,
    "date": "2026-05-01",
    "startAt": "12:00",
    "theme": {
      "id": 1,
      "name": "공포",
      "content": "오금이 저리는 공포입니다.",
      "url": "/themes/scary"
    }
  }
  ```

### `DELETE /admin/reservations/{id}`
지정된 예약 고유 ID의 예약(혹은 대기 등록) 정보를 강제 삭제합니다. (로그인 세션 필요)
- **응답 (204 No Content)**: (본문 없음)

---

## 공통 규칙 및 에러 핸들링 (Error Handling)

- **관리자 API 인증**: `/admin/**` 경로 아래의 모든 API는 요청 시 로그인 세션이 필수이며, 해당 세션 사용자의 `role`이 `ADMIN`이어야 접근이 허용됩니다. (단, `/admin` HTML 화면 렌더링 요청은 세션 필터링에서 제외)
- **에러 응답 형식**: API 수행 실패 시 반환되는 JSON 응답 객체의 표준 형식은 다음과 같습니다.
  ```json
  {
    "code": "INPUT_FORMAT_ERROR",
    "message": "시간은 필수 사항 입니다. 시간을 선택해주세요.",
    "timestamp": "2026-06-07T16:08:11.123456",
    "fieldErrors": [
      {
        "field": "startAt",
        "message": "시간은 필수 사항 입니다. 시간을 선택해주세요."
      }
    ]
  }
  ```
  - `code` (String): 시스템 내부 에러 코드 (예: `INPUT_FORMAT_ERROR`, `USER_ALREADY_EXISTS`, `UNAUTHORIZED` 등)
  - `message` (String): 일반 에러 메시지
  - `timestamp` (String): 에러 발생 일시
  - `fieldErrors` (Array): 입력 값 검증 실패 시, 필드별 검증 오류 정보 목록 (성공 및 단순 비즈니스 오류 시에는 빈 배열 `[]`)
