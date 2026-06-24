## 기능 명세

### 사용자

- [x] 전체 테마 목록을 조회 가능하다.
- [x] 날짜·시간·테마(슬롯)를 선택하여 예약을 신청할 수 있다.
    - [x] 신청한 슬롯에 확정 예약이 없으면 즉시 확정(CONFIRMED) 된다.
    - [x] 신청한 슬롯에 이미 확정 예약이 있으면 자동으로 대기(WAITING) 로 등록된다.
    - [x] 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
    - [x] 지나간 날짜·시간의 슬롯은 예약할 수 없다.
    - [x] 같은 사용자는 같은 슬롯(날짜+시간+테마)에 중복 신청할 수 없다. (확정·대기 모두)
- [x] 본인의 이름으로 예약과 대기 목록을 함께 조회할 수 있다.
    - [x] 대기에는 본인의 대기 순번도 함께 보여준다.
- [x] 본인의 예약 또는 대기를 취소할 수 있다.
    - [x] 지나간 날짜·시간의 예약 취소는 불가능하다.
    - [x] 확정 예약을 취소하면 같은 슬롯의 첫 번째 대기자가 자동으로 확정으로 승격된다.

  <br>

### 화면 작성

- [x] 웰컴 페이지에서 전체 테마 목록을 보여준다.
- [x] 웰컴 페이지에서 최근 일주일 인기 테마 10개 목록을 보여준다.
- [x] 테마 목록 중 하나를 선택하면 해당 테마를 예약할 수 있는 상세 페이지로 이동한다.
- [x] 웰컴 페이지에서 사용자의 이름을 입력하여 나의 예약·대기 관리를 할 수 있다.
- [x] 예약 시에는 이름과 날짜, 예약 시간대를 선택해야 한다.
- [x] 같은 테마, 같은 날짜에서 이미 확정된 시간대는 '대기 신청'으로 표시되어, 선택 시 대기로 등록된다.

<br>

### 관리자

- [x] 모든 예약 조회 가능하다.
- [x] 예약 시간을 추가할 수 있다.
- [x] 예약 시간을 삭제할 수 있다.
    - [x] 예약이 존재하는 예약 시간은 삭제가 불가능하다.
- [x] 테마를 추가할 수 있다.
- [x] 테마를 삭제할 수 있다.
    - [x] 예약이 존재하는 테마는 삭제가 불가능하다.

<br>

# 에러 응답 명세

**공통 에러 응답 형식**

```json
{
  "code": "ERROR_CODE",
  "message": "사용자에게 보여줄 메시지",
  "errors": [
    {
      "field": "name",
      "reason": "이름은 비어 있을 수 없습니다."
    }
  ]
}
```

errors는 입력값 검증 실패처럼 필드별 설명이 필요할 때만 포함한다.

**예약 API**

| **HTTP Status** | **code**              | **message**                |
|-----------------|-----------------------|----------------------------|
| 400             | INVALID_NAME          | 이름은 공백일 수 없고 20자 이내여야 합니다. |
| 400             | INVALID_DATE          | 예약 날짜는 필수입니다.              |
| 400             | INVALID_TIME          | 예약 시간은 필수입니다.              |
| 400             | INVALID_THEME         | 유효한 테마를 선택해주세요.            |
| 404             | RESERVATION_NOT_FOUND | 예약이 존재하지 않습니다.             |
| 409             | RESERVATION_DUPLICATE | 같은 날짜, 같은 시간에 예약이 존재합니다.   |
| 422             | RESERVATION_PAST_TIME | 과거 예약은 선택할 수 없습니다.         |

<details>
<summary>관리자 - 예약 시간 / 테마 API</summary>

**예약 시간 API**

| **HTTP Status** | **code**                    | **message**                 |
|-----------------|-----------------------------|-----------------------------|
| 400             | RESERVATION_TIME_INVALID_ID | 예약 시간 ID는 1 이상의 숫자여야 합니다.   |
| 400             | INVALID_TIME                | 예약 시간은 필수이며 정각만 선택할 수 있습니다. |
| 404             | RESERVATION_TIME_NOT_FOUND  | 예약 시간을 찾을 수 없습니다.           |
| 409             | RESERVATION_TIME_DUPLICATE  | 이미 등록된 예약 시간입니다.            |
| 409             | RESERVATION_EXIST_ON_TIME   | 예약이 있는 시간은 삭제할 수 없습니다.      |

**테마 API**

| **HTTP Status** | **code**                   | **message**                                |
|-----------------|----------------------------|--------------------------------------------|
| 400             | THEME_INVALID_NAME         | 테마 이름은 공백일 수 없고 20자 이내여야 합니다.              |
| 400             | THEME_INVALID_DESCRIPTION  | 테마 설명은 3자 이상이어야 합니다.                       |
| 400             | THEME_INVALID_URL          | 테마 이미지 URL은 비어 있을 수 없으며 올바른 URL 형식이어야 합니다. |
| 404             | THEME_NOT_FOUND            | 테마를 찾을 수 없습니다.                             |
| 409             | THEME_DUPLICATE            | 이미 등록된 테마입니다.                              |
| 409             | RESERVATION_EXIST_ON_THEME | 예약이 있는 테마는 삭제할 수 없습니다.                     |

</details>

<br>

# 주요 API 명세

#### 예약 · 대기 통합 API

<details>
<summary>날짜·시간·테마를 선택해 예약을 신청한다. (서버가 확정/대기를 결정)</summary>

- 날짜·시간·테마(슬롯)를 선택해 예약을 신청한다.
    - HTTP method : POST

  **Request**

    - API path : /api/reservations
    - Headers:
        - Content-Type: application/json

      ```json
      {
          "name": "코코",
          "date": "2026-05-01",
          "timeId": 1,
          "themeId": 1
      }
      ```

  **Response**

    - status: 201 Created (성공적으로 예약 또는 대기가 등록됨)
    - Headers:
        - Location: /api/reservations/1
        - Content-Type: application/json
    - Body
        - `status` 는 슬롯 점유 여부에 따라 `CONFIRMED` 또는 `WAITING` 으로 서버가 결정한다.

      ```json
      {
        "id": 1,
        "name": "코코",
        "date": "2026-05-01",
        "time": {
          "id": 1,
          "startAt": "10:00"
        },
        "theme": {
          "id": 1,
          "name": "공포의 병원",
          "description": "버려진 정신병원에서 탈출해야 합니다.",
          "imageUrl": "https://picsum.photos/200/300",
          "runningTime": 60
        },
        "status": "CONFIRMED"
      }
      ```

  경로 설정 이유

    - 예약과 대기를 별도 리소스로 두지 않고 하나의 예약(/api/reservations)으로 통합했다. 동일 슬롯에 확정 예약이 이미 존재하면 같은 요청이 `WAITING` 으로 등록된다.

</details>

<details>
<summary>본인의 이름으로 신청한 예약과 대기를 함께 조회할 수 있다.</summary>

- 본인의 이름으로 신청한 예약과 대기를 함께 조회할 수 있다.
    - HTTP method : GET

  **Request**

    - API path : /api/reservations
    - Query Parameters:
        - name: 코코

  **Response**

    - status: 200 OK
    - Headers:
        - Content-Type: application/json
    - Body
        - 확정 예약은 `reservations`, 대기는 `waitings` 로 나뉘며 대기에는 `waitingNumber` 가 포함된다.

      ```json
      {
        "reservations": [
          {
            "id": 1,
            "name": "코코",
            "date": "2026-05-01",
            "time": {
              "id": 1,
              "startAt": "10:00"
            },
            "theme": {
              "id": 1,
              "name": "공포의 병원",
              "description": "버려진 정신병원에서 탈출해야 합니다.",
              "imageUrl": "https://picsum.photos/200/300",
              "runningTime": 60
            },
            "status": "CONFIRMED",
            "waitingNumber": null
          }
        ],
        "waitings": [
          {
            "id": 2,
            "name": "코코",
            "date": "2026-05-02",
            "time": {
              "id": 2,
              "startAt": "11:00"
            },
            "theme": {
              "id": 2,
              "name": "박물관 침입",
              "description": "전설의 다이아몬드를 훔쳐 나오세요.",
              "imageUrl": "https://picsum.photos/200/300",
              "runningTime": 60
            },
            "status": "WAITING",
            "waitingNumber": 1
          }
        ]
      }
      ```

</details>

<details>
<summary>본인의 예약 또는 대기를 취소할 수 있다.</summary>

- 본인의 예약 또는 대기를 취소할 수 있다.
    - HTTP method : DELETE

  **Request**

    - API path : /api/reservations/{id}
    - Path Variable:
        - id(Long) 취소할 예약 또는 대기의 고유 id

  **Response**

    - status: 204 No Content (성공적으로 취소됨)
    - Body

      ```json
      (Empty)
      ```

  동작 설명

    - 확정 예약을 취소하면 같은 슬롯의 첫 번째 대기자가 자동으로 확정으로 승격된다.
    - 대기를 취소하면 해당 대기만 제거된다.

</details>

<details>
<summary>날짜·테마별 시간대 예약 현황을 조회할 수 있다.</summary>

- 같은 테마, 같은 날짜의 시간대별 예약 현황을 조회한다.
    - HTTP method : GET

  **Request**

    - API path : /api/reservations/booked-times
    - Query Parameters:
        - selectedDate: 2026-05-07
        - themeId: 1

  **Response**

    - status: 200 OK
    - Headers:
        - Content-Type: application/json
    - Body
        - `reserved` 가 `true` 면 해당 시간대에 확정 예약이 있어, 신청 시 대기로 등록된다.

      ```json
      [
        {
          "timeId": 1,
          "startAt": "10:00",
          "reserved": true
        },
        {
          "timeId": 2,
          "startAt": "11:00",
          "reserved": false
        }
      ]
      ```


</details>

#### 테마 · 시간 조회 API

<details>
<summary>지난 일주일 기간 동안의 인기 테마 10개를 조회할 수 있다.</summary>

- 지난 일주일 기간 동안의 인기 테마 10개를 조회할 수 있다.
    - HTTP method : GET

  **Request**

    - API path : /api/themes/popular-themes
    - Headers: (특별한 요청 없음)

  **Response**

    - status: 200 OK (성공적으로 인기 테마 10개가 조회됨)
    - Headers:
        - Content-Type: application/json
    - Body

      ```json
      [
        {
          "id": 1,
          "name": "공포의 병원",
          "description": "버려진 정신병원에서 탈출해야 합니다.",
          "imageUrl": "https://picsum.photos/200/300",
          "runningTime": 60
        },
        {
          "id": 2,
          "name": "박물관 침입",
          "description": "전설의 다이아몬드를 훔쳐 나오세요.",
          "imageUrl": "https://picsum.photos/200/300",
          "runningTime": 60
        }
      ]
      ```

  경로 설정 이유

    - 인기 테마 집계는 테마 리소스에 대한 조회이므로 테마 리소스(/api/themes) 하위 경로에 둔다.

</details>

#### 관리자 API

<details>
<summary>관리자는 테마를 추가할 수 있다.</summary>

- 관리자는 테마를 추가할 수 있다.
    - HTTP method : POST

  **Request**

    - API path : /api/admin/themes
    - Headers:
        - Content-Type: application/json

      ```json
      {
          "name": "공포의 병원",
          "description": "버려진 정신병원에서 탈출해야 합니다.",
          "imageUrl": "https://picsum.photos/200/300"
      }
      ```

  **Response**

    - status: 201 Created (성공적으로 테마가 생성됨)
    - Headers:
        - Location: /themes/1
    - Body

      ```json
      (Empty)
      ```

  경로 설정 이유

    - 관리자 계층 (/api/admin)을 분리하여 관리자만 접근할 수 있다는 것을 명시한다.
    - POST 메소드로 테마 리소스(/themes)에 새로운 테마를 생성한다는 것을 명시한다.

</details>

<details>
<summary>관리자는 테마를 삭제할 수 있다.</summary>

- 관리자는 테마를 삭제할 수 있다.
    - HTTP method : DELETE

  **Request**

    - API path : /api/admin/themes/{id}
    - Path Variable:
        - id(Long) 삭제할 테마의 고유 id

  **Response**

    - status: 204 No Content (성공적으로 테마가 삭제됨)
    - Body

      ```json
      (Empty)
      ```

  경로 설정 이유

    - 관리자 계층 (/api/admin)을 분리하여 관리자만 접근할 수 있다는 것을 명시한다.
    - DELETE 메소드로 요청 id에 해당하는 테마 리소스(/themes)를 삭제하는 것을 명시한다.

</details>

<details>
<summary>관리자는 예약 시간을 추가할 수 있다.</summary>

- 관리자는 예약 시간을 추가할 수 있다.
    - HTTP method : POST

  **Request**

    - API path : /api/admin/times
    - Headers:
        - Content-Type: application/json

      ```json
      {
          "startAt": "10:00"
      }
      ```

  **Response**

    - status: 201 Created (성공적으로 예약 시간이 생성됨)
    - Headers:
        - Location: /api/times/1
    - Body

      ```json
      (Empty)
      ```

</details>

<details>
<summary>관리자는 예약 시간을 삭제할 수 있다.</summary>

- 관리자는 예약 시간을 삭제할 수 있다.
    - HTTP method : DELETE

  **Request**

    - API path : /api/admin/times/{id}
    - Path Variable:
        - id(Long) 삭제할 예약 시간의 고유 id

  **Response**

    - status: 204 No Content (성공적으로 예약 시간이 삭제됨)
    - Body

      ```json
      (Empty)
      ```

</details>

<details>
<summary>관리자는 모든 예약을 조회할 수 있다.</summary>

- 관리자는 모든 예약을 조회할 수 있다.
    - HTTP method : GET

  **Request**

    - API path : /api/admin/reservations
    - Headers: (특별한 요청 없음)

  **Response**

    - status: 200 OK
    - Headers:
        - Content-Type: application/json
    - Body

      ```json
      [
        {
          "id": 1,
          "name": "코코",
          "date": "2026-05-01",
          "time": {
            "id": 1,
            "startAt": "10:00"
          },
          "theme": {
            "id": 1,
            "name": "공포의 병원",
            "description": "버려진 정신병원에서 탈출해야 합니다.",
            "imageUrl": "https://picsum.photos/200/300",
            "runningTime": 60
          },
          "status": "CONFIRMED"
        }
      ]
      ```

</details>
