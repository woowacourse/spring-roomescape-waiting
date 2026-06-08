## 기능 명세

## 사이클2 기능 명세

### 정책
- 본 기능은 자동 승인 방식을 사용한다.
- 예약 취소 시 대기번호 1번은 자동으로 예약으로 전환된다.
- 예약 변경 시 변경 전 슬롯의 대기번호 1번은 자동으로 예약으로 전환된다.

## 예약 승격

- [x] 예약이 취소되면 대기번호 1번 대기가 자동으로 승격된다.
- [x] 예약이 변경되면 변경 전 슬롯의 대기번호 1번 대기가 자동으로 승격된다.
- [ ] 승격 대상이 존재하는 동안 해당 슬롯에는 예약을 생성할 수 없다.
- [ ] 승격 대상이 존재하는 동안 새로운 대기 등록은 가능하다.


## 대기 순번 관리

- [x] 대기번호 1번이 예약으로 전환되면 해당 대기를 대기열에서 제거한다.
- [x] 대기열에서 제거된 대기 뒤에 존재하는 대기의 순번을 재정렬한다.
- [x] 대기가 취소되면 취소된 대기 뒤에 존재하는 대기의 순번을 재정렬한다.

## 승격 대상 재지정

- [x] 승격 대상이 예약으로 전환된 경우 대기열이 존재하면 새로운 대기번호 1번을 승격 대상으로 지정한다.
- [x] 대기열이 존재하지 않는 경우 승격 대상은 생성되지 않는다.

<br>

### 사용자

- [x] 전체 테마 목록을 조회 가능하다.
- [x] 날짜와 시간, 테마를 선택하여 예약할 수 있다.
    - [x] 지나간 날짜·시간에 대한 예약 생성은 불가능하다.
    - [x] 같은 날짜+시간+테마에 이미 예약이 있으면 중복 예약이 불가능하다.
- [x] 본인의 이름으로 예약 목록을 조회할 수 있다.
- [x] 본인의 예약의 날짜·시간을 변경할 수 있다.
    - [x] 지나간 날짜·시간으로의 예약 변경은 불가능하다.
    - [x] 같은 날짜+시간+테마에 이미 예약이 있으면 예약 변경이 불가능하다.
- [x] 본인의 예약을 취소할 수 있다.
    - [x] 지나간 날짜·시간의 예약 취소는 불가능하다.

- [x] 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
    - [x] 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
    - [x] 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
    - [x] 예약이 없는 날짜 시간 테마에는 대기할 수 없다.
    - [x] 본인의 확정된 예약 슬롯에 대기를 신청할 수 없다.
- [x] 본인의 이름으로 신청한 대기를 예약 목록과 함께 조회할 수 있다.
    - [x] 대기에는 본인의 대기 순번도 함께 보여준다.
- [x] 사용자는 본인의 대기를 취소할 수 있다.

  <br>

### 화면 작성

- [x] 웰컴 페이지에서 전체 테마 목록을 보여준다.
- [x] 웰컴 페이지에서 최근 일주일 인기 테마 10개 목록을 보여준다.
- [x] 테마 목록 중 하나를 선택하면 해당 테마를 예약할 수 있는 상세 페이지로 이동한다.
- [x] 웰컴 페이지에서 사용자의 이름을 입력하여 나의 예약 관리를 할 수 있다.
- [x] 예약 시에는 이름과 날짜, 예약 시간대를 선택해야 한다.
- [x] 같은 테마, 같은 날짜에서 이미 예약된 시간대는 선택이 불가능하다.

<br>

### 관리자

- [x] 모든 예약 조회 가능하다.
- [x] 예약 시간을 추가할 수 있다.
- [x] 예약 시간을 삭제할 수 있다.
    - [x] 예약이 존재하는 예약 시간은 삭제가 불가능하다.
- [x] 테마를 추가할 수 있다.
- [x] 테마를 삭제할 수 있다.

<br>

# 에러 응답 명세

**공통 에러 응답 형식**

```markdown
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

#### 방탈출 예약 대기 - 사이클2 추가  API

**예약 슬롯 공통 검증 API**

| **HTTP Status** | **code**                  | **message**             |
|-----------------|---------------------------|-------------------------|
| 409             | INVALID_RESERVATION_SLOT  | 유효하지 않은 예약 슬롯입니다.       |
| 422             | SLOT_PAST_TIME            | 과거 날짜 및 시간은 선택할 수 없습니다. |


#### 방탈출 예약 대기 - 사이클1 추가  API

**대기 API**

| **HTTP Status** | **code**                        | **message**                    |
|-----------------|---------------------------------|--------------------------------|
| 400             | INVALID_NAME                    | 이름은 공백일 수 없으며 20자 이내여야 합니다.    |
| 400             | INVALID_WAITING_NUMBER          | 대기 순서는 필수입니다.                  |
| 404             | WAITING_NOT_FOUND               | 대기가 존재하지 않습니다.                 |
| 409             | WAITING_DUPLICATE               | 이미 대기가 등록되어 있습니다.              |
| 422             | IMMEDIATE_RESERVATION_AVAILABLE | 즉시 예약이 가능하므로, 대기 등록이 불가능합니다.   |
| 422             | CANNOT_WAITLIST_CONFIRMED_SLOT  | 본인이 예약 확정한 슬롯에는 대기 등록이 불가능합니다. |

<details>
<summary>지난 미션에서 구현된 API</summary>

**예약 API**

| **HTTP Status** | **code**              | **message**                 |
|-----------------|-----------------------|-----------------------------|
| 400             | INVALID_NAME          | 이름은 공백일 수 없으며 20자 이내여야 합니다. |
| 400             | INVALID_DATE          | 예약 날짜 형식이 올바르지 않습니다.        |
| 400             | INVALID_TIME          | 예약 시간 형식이 올바르지 않습니다.        |
| 400             | INVALID_THEME         | 예약 테마 형식이 올바르지 않습니다.        |
| 404             | RESERVATION_NOT_FOUND | 예약이 존재하지 않습니다.              |
| 409             | RESERVATION_DUPLICATE | 같은 날짜, 같은 시간에 예약이 존재합니다.    |

**예약 시간 API**

| **HTTP Status** | **code**                    | **message**              |
|-----------------|-----------------------------|--------------------------|
| 400             | RESERVATION_TIME_INVALID_ID | ID의 형식이 올바르지 않습니다.       |
| 400             | INVALID_TIME                | 예약 시간 형식이 올바르지 않습니다.     |
| 404             | RESERVATION_TIME_NOT_FOUND  | 존재하지 않는 시간입니다.           |
| 409             | RESERVATION_TIME_DUPLICATE  | 타임 테이블에 이미 존재하는 시간입니다.   |
| 409             | RESERVATION_EXIST_ON_TIME   | 예약이 존재하는 시간은 삭제할 수 없습니다. |

**테마 API**

| **HTTP Status** | **code**                   | **message**               |
|-----------------|----------------------------|---------------------------|
| 400             | THEME_INVALID_NAME         | 테마 이름의 형식이 올바르지 않습니다.     |
| 400             | THEME_INVALID_DESCRIPTION  | 테마 설명의 형식이 올바르지 않습니다.     |
| 400             | THEME_INVALID_URL          | 테마 이미지 URL 형식이 올바르지 않습니다. |
| 404             | THEME_NOT_FOUND            | 존재하지 않는 테마입니다.            |
| 409             | THEME_DUPLICATE            | 같은 테마가 존재합니다.             |
| 409             | RESERVATION_EXIST_ON_THEME | 예약이 존재하는 테마는 삭제할 수 없습니다.  |

</details>

<br>

# 주요 API 명세

#### 방탈출 예약 대기 - 사이클1 추가  API

<details>
<summary>다른 사용자에 의해 예약된 슬롯에 대기를 신청할 수 있다.</summary>

- 다른 사용자에 의해 예약된 슬롯에 대기를 신청할 수 있다.
    - HTTP method : POST

  **Request**

    - API path : /api/waiting
    - Headers:
        - Content-Type: application/json

      ```json
      {
          "name": "코코",
          "date": "2026-05-01",
          "timeId": 1,
          "themeId": 2
      }
      ```

  **Response**

    - status: 201 Created (성공적으로 대기가 등록됨)
    - Headers:
        - Location: /api/waiting/1
    - Body

      ```json
      (Empty)
      ```

</details>

<details>
<summary>본인의 이름으로 신청한 대기와 예약을 함께 조회할 수 있다.</summary>

- 본인의 이름으로 신청한 대기와 예약을 함께 조회할 수 있다.
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
              "id": 2,
              "name": "공포의 병원",
              "description": "버려진 정신병원에서 탈출해야 합니다.",
              "imageUrl": "https://picsum.photos/200/300",
              "runningTime": 60
            }
          }
        ],
        "waitings": [
          {
            "id": 1,
            "name": "코코",
            "date": "2026-05-02",
            "time": {
              "id": 2,
              "startAt": "11:00"
            },
            "theme": {
              "id": 1,
              "name": "우주 정거장",
              "description": "무중력 상태에서의 사투!",
              "imageUrl": "https://picsum.photos/200/400",
              "runningTime": 70
            },
            "waitingNumber": 1
          }
        ]
      }
      ```

</details>

<details>
<summary>본인의 대기를 취소할 수 있다.</summary>

- 본인의 대기를 취소할 수 있다.
    - HTTP method : DELETE

  **Request**

    - API path : /api/waiting/{id}
    - Path Variable:
        - id(Long) 취소할 대기의 고유 id

  **Response**

    - status: 204 No Content (성공적으로 대기가 취소됨)
    - Body

      ```json
      (Empty)
      ```

</details>

#### 지난 미션에서 구현된 API

<details>
<summary>관리자는 테마를 추가할 수 있다.</summary>

- 관리자는 테마를 추가할 수 있다.
    - HTTP method : POST

  **Request**

    - API path : /admin/themes
    - Headers:
        - Content-Type: application/json

      ```json
      {
          "name": "공포의 병원",
          "description": "버려진 정신병원에서 탈출해야 합니다.",
          "thumbnailUrl": "https://picsum.photos/200/300"
      }
      ```

  **Response**

    - status: 201 Created (성공적으로 테마가 생성됨)
    - Headers:
        - Location: /admin/themes/1
    - Body

      ```json
      (Empty)
      ```

  경로 설정 이유

    - 관리자 계층 (/admin)을 분리하여 관리자만 접근할 수 있다는 것을 명시한다.
    - POST 메소드로 테마 리소스(/themes)에 새로운 테마를 생성한다는 것을 명시한다.

</details>

<details>
<summary>관리자는 테마를 삭제할 수 있다.</summary>

- 관리자는 테마를 삭제할 수 있다.
    - HTTP method : DELETE

  **Request**

    - API path : /admin/themes/{id}
    - Path Variable:
        - id(Long) 삭제할 테마의 고유 id
    - Headers:
        - Content-Type: application/json

      ```json
      {
          "id": 1
      }
      ```

  **Response**

    - status: 204 No Content (성공적으로 테마가 삭제됨)
    - Body

      ```json
      (Empty)
      ```

  경로 설정 이유

    - 관리자 계층 (/admin)을 분리하여 관리자만 접근할 수 있다는 것을 명시한다.
    - DELETE 메소드로 요청 id에 해당하는 테마 리소스(/themes)를 삭제하는 것을 명시한다.

</details>

<details>
<summary>테마, 날짜, 시간을 선택해서 예약할 수 있다.</summary>

- 테마, 날짜, 시간을 선택해서 예약할 수 있다.
    - HTTP method : Post

  **Request**

    - API path : /reservations
    - Headers:
        - Content-Type: application/json

      ```json
      {
          "name": "코코",
          "date": "2026-05-01",
          "timeId": 1,
          "themeId": 2
      }
      ```

  **Response**

    - status: 201 Created (성공적으로 테마가 생성됨)
    - Headers:
        - Location: /reservations/1
    - Body

      ```json
      (Empty)
      ```

  경로 설정 이유

    - POST 메소드로 예약 리소스(/reservations)에 새로운 예약을 생성한다는 것을 명시한다.

</details>

<details>
<summary>지난 일주일 기간 동안의 인기 테마 10개를 조회할 수 있다.</summary>

- 지난 일주일 기간 동안의 인기 테마 10개를 조회할 수 있다.
    - HTTP method : GET

  **Request**

    - API path : /reservations/popular-themes
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
          "thumbnailUrl": "https://picsum.photos/200/300"
        },
        {
          "id": 2,
          "name": "우주 정거장",
          "description": "무중력 상태에서의 사투!",
          "thumbnailUrl": "https://picsum.photos/200/400"
        }
      ]
      ```

  경로 설정 이유

    - ReservationService는 Reservation 객체를 생성하기 위해 이미 ReservationTimeRepository와 ThemeRepository를 모두 참조하고 있다. 즉, 서비스 계층에서
      예약, 시간, 테마 정보에 대한 도메인 지식을 자연스럽게 모두 가지고 있는 상태다.
    - ThemeService는 테마의 생성과 관리라는 역할만 수행하므로 ThemeRepository 하나만 의존하고 있다. 여기서 인기 테마를 산출하기 위해 예약 관련 의존성을 추가하는 것은, 서비스의 복잡도를
      높이고 불필요한 결합을 만드는 것이라 생각했다.

</details>

<details>
<summary>같은 테마, 같은 날짜, 같은 시간에 예약이 이미 존재하면 예약할 수 없다.</summary>

- 같은 테마, 같은 날짜, 같은 시간에 예약이 이미 존재하면 예약할 수 없다.
    - HTTP method : GET

  **Request**

    - API path : /reservations/booked-times
    - Query Parameters:
    - selectedDate: 2026-05-07
    - themeId: 1

  **Response**

    - status: 200 OK
    - Headers:
        - Content-Type: application/json
    - Body

      ```json
      [
        {
          "id": 1,
          "startAt": "10:00"
        },
        {
          "id": 2,
          "startAt": "14:00"
        }
      ]
      ```

</details>

