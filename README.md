# API 명세서

## 예약 (1단계)

### 예약 생성

- **POST** `/reservations`
- Request Body
  ```json
  {
    "name": "홍길동",
    "date": "2025-06-01",
    "timeId": 1,
    "themeId": 1
  }
  ```
- Response `201 Created`
  ```json
  {
    "id": 1,
    "name": "홍길동",
    "date": "2025-06-01",
    "time": { "id": 1, "startAt": "10:00" },
    "theme": { "id": 1, "name": "테마명", "description": "설명", "thumbnailUrl": "https://..." },
    "status": "PENDING",
    "order": 0,
    "orderId": "order_...",
    "amount": 50000
  }
  ```
  > 빈 슬롯 예약은 결제 대기 상태(`PENDING`)로 생성됩니다. Toss 결제 승인 성공 후 `CONFIRMED`로 확정됩니다.
  > 슬롯이 이미 예약된 경우 `status`가 `"WAITING"`으로, `order`에 대기 순번(1~3)이 반환됩니다.

### 결제 설정 조회

- **GET** `/payments/config`
- Response `200 OK`
  ```json
  {
    "clientKey": "test_gck_docs_..."
  }
  ```
  > 서버 승인용 secret key는 응답에 포함하지 않습니다.

### 결제 승인

- **POST** `/payments/confirm`
- Request Body
  ```json
  {
    "paymentKey": "tgen_...",
    "orderId": "order_...",
    "amount": 50000
  }
  ```
- Response `200 OK`
  ```json
  {
    "id": 1,
    "name": "홍길동",
    "date": "2025-06-01",
    "time": { "id": 1, "startAt": "10:00" },
    "theme": { "id": 1, "name": "테마명", "description": "설명", "thumbnailUrl": "https://..." },
    "status": "CONFIRMED",
    "order": 0,
    "orderId": "order_...",
    "amount": 50000
  }
  ```
  > 서버는 저장된 주문 금액과 요청 금액을 먼저 비교한 뒤 Toss 승인 API를 호출합니다.

### 결제 실패 정리

- **POST** `/payments/fail`
- Request Body
  ```json
  {
    "code": "PAY_PROCESS_CANCELED",
    "message": "사용자가 결제를 취소했습니다.",
    "orderId": "order_..."
  }
  ```
- Response `204 No Content`
  > `orderId`가 없으면 아무 작업 없이 성공 응답을 반환합니다.

### 예약 취소

- **DELETE** `/reservations/{id}`
- Response `204 No Content`

### 대기 취소

- **DELETE** `/reservations/waits/{id}`
- Response `204 No Content`

---

## 내 예약 목록 조회 (2단계)

### 내 예약 + 대기 목록 조회

- **GET** `/reservations?name={name}`
- Response `200 OK`
  ```json
  [
    {
      "id": 1,
      "name": "홍길동",
      "date": "2025-06-01",
      "time": { "id": 1, "startAt": "10:00" },
      "theme": { "id": 1, "name": "테마명", "description": "설명", "thumbnailUrl": "https://..." },
      "status": "CONFIRMED",
      "order": 0,
      "orderId": "order_...",
      "amount": 50000
    },
    {
      "id": 2,
      "name": "홍길동",
      "date": "2025-06-02",
      "time": { "id": 2, "startAt": "14:00" },
      "theme": { "id": 1, "name": "테마명", "description": "설명", "thumbnailUrl": "https://..." },
      "status": "WAITING",
      "order": 2,
      "orderId": null,
      "amount": null
    }
  ]
  ```

---

# 사이클 1 - 기능 구현 목록

방탈출 사용자 예약 미션까지는 한 슬롯(날짜+시간+테마)에 한 명만 예약할 수 있었고, 이미 예약된 슬롯은 사용자에게 보이지 않았다.  
이번 사이클부터는 이미 예약된 슬롯에 대기를 신청할 수 있고, 사용자는 본인의 예약과 대기를 함께 조회할 수 있다.

이번 사이클의 작업도 백엔드 API 추가와 사용자가 보는 화면을 만드는 것 두 가지를 함께 진행한다.  
API에 맞춰 페어가 함께 사용자가 사용하는 클라이언트 화면을 만들고, 각 단계의 화면이 브라우저에서 정상 동작하는 것까지 확인한다. 화면 작성에는 AI를 활용해도 좋다.

## 1단계 - 예약 대기 신청/취소

- [x] 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
    - [x] 예약이 존재하면 예약 대기 가능 여부를 확인하고 대기를 추가한다.
- [x] 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
    - [x] 예약 대기 인원은 최대 3명으로 제한한다.
- [x] 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
    - [x] 예약이 존재할 때, 기존 예약과 새 예약의 요청자가 같으면 예외를 발생시킨다.
- [x] 사용자는 본인의 대기를 취소할 수 있다.
    - [x] 예약이 취소되었을 때 예약 대기 1번을 예약에 추가한다.

## 2단계 - 내 예약 목록 조회 (상태 구분)

- [x] 이전 미션의 내 예약 목록 조회를 확장한다.
- [x] 사용자의 예약과 대기가 상태로 구분되어 함께 표시된다.
- [x] 대기에는 본인의 대기 순번도 함께 보여준다.

# 사이클 2 - 기능 구현 목록

## 1단계 - 예약 대기 승인

- [x] 예약 대기를 자동으로 예약으로 전환한다.
- [x] 대기가 예약으로 전환되면 해당 슬롯의 나머지 대기 순번이 재정렬된다.
