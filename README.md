# spring-roomescape-member

> 사용자 방탈출 예약 미션 저장소

페어 : [스타크](https://github.com/MODUGGAGI), [카야](https://github.com/choiyoung69)

---

## 👥 페어 프로그래밍 규칙

네비게이터, 드라이버 역할 교환은 **30분**에 한번씩

|          **네비게이터**          |            **드라이버**            |
|:---------------------------:|:------------------------------:|
| 드라이버에게 지시와 동시에 서로의 대화 내용 기록 | 자신이 작성하려는 코드의 의도를 항상 명확히 말로 전달 |

---

## 기능 요구 사항

### **예약 시간**
- [x] 관리자의 예약 시간의 생성 기능 추가
- [x] 관리자의 예약 시간의 삭제 기능 추가
  - 예약이 존재하는 시간은 삭제 불가 처리

### **테마**

- [x] 관리자의 테마 추가
    - 테마 이름, 테마 설명, 테마 썸네일 이미지 URL을 RequestBody로 보낸다.
- [x] 관리자의 테마 삭제
  - 예약이 존재하는 테마는 삭제 불가 처리
- [x] `Reservation` 클래스에 Theme 필드 추가
- [x] 최근 1주 동안 예약이 많았던 테마 상위 10개 조회 기능 추가
   - 최근 1주는 오늘 제외하고 어제 기준으로 1주 동안의 기간

### **사용자 예약**

- [x] 날짜를 선택하면 해당 날짜의 테마 조회 기능 추가
- [x] 날짜와 테마를 선택하면 예약 가능한 시간 목록 조회 기능 추가
    - 예약 불가능한 시간과 가능한 시간 전부 조회 가능
    - 불가능한 시간은 선택이 불가능하게 처리
- [x] 사용자의 방탈출 예약 기능 추가
    - Request Body로
        1. 본인 이름
        2. 선택 날짜
        3. 테마 (테마 ID)
        4. 시간 (시간 ID)

      4개의 필드 포함

    - 이미 예약되어있는 날짜, 테마, 시간에 방탈출 예약 불가
    - 지나간 시간에 대해서 예약 불가
정- [x] 사용자가 **자신의 이름으로 본인의 예약 목록을 조회**기능 추가
- [x] 사용자가 본인의 예약을 **취소**할 수 있다.
  - 이미 지나간 날짜/시간에 대한 예약은 취소할 수 없다.
- [x] 사용자가 본인의 예약의 **날짜·시간을 변경**할 수 있다.
  - 변경하려는 예약의 테마에 대한 날짜/시간에 이미 예약이 존재할 경우 변경할 수 없다.

### **잘못된 입력값 처리**
- [x] 유효하지 않은 입력값(빈 이름, 잘못된 날짜 형식 등)을 거부
  - 적절한 에러 메시지 반환
---

## API 명세서

### 공통 에러 응답

요청 처리 실패 시 다음 형식으로 에러 메시지를 반환한다.

```json
{
  "errorMessage": "에러 메시지"
}
```
- `400 Bad Request`: 빈 값, 잘못된 날짜/시간 형식, 숫자가 아닌 ID 등 요청 형식이 올바르지 않은 경우
- `404 Not Found`: 요청한 리소스가 존재하지 않는 경우
- `409 Conflict`: 이미 같은 리소스가 존재해 요청이 충돌하는 경우
- `422 Unprocessable Entity`: 요청 형식은 올바르지만 비즈니스 규칙상 처리할 수 없는 경우

### 사용자 API

#### 예약 (`/reservations`)

**전체 예약 조회** `GET /reservations` → `200 OK`

**예약자 이름으로 예약 조회** `GET /reservations?username={name}` → `200 OK`

응답
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "date": "2024-05-01",
    "theme": {
      "id": 1,
      "name": "테마 이름",
      "description": "테마 설명",
      "thumbnailImgUrl": "https://example.com/thumbnail.jpg"
    },
    "time": {
      "id": 1,
      "startAt": "10:00"
    }
  }
]
```

**예약 생성** `POST /reservations` 
→ 성공 시 `201 Created`
→ 빈 이름, 잘못된 날짜 형식, 올바르지 않은 ID 형식이 들어올 시 `400 Bad Request`
→ 같은 날짜, 테마, 시간에 이미 예약이 존재할 시 `409 Conflict`
→ 현재 시각보다 이전 날짜/시간으로 예약할 시 `422 Unprocessable Entity`

요청
```json
{
  "name": "홍길동",
  "date": "2024-05-01",
  "themeId": 1,
  "timeId": 1
}
```
응답
```json
{
  "id": 1,
  "name": "홍길동",
  "date": "2024-05-01",
  "theme": {
    "id": 1,
    "name": "테마 이름",
    "description": "테마 설명",
    "thumbnailImgUrl": "https://example.com/thumbnail.jpg"
  },
  "time": {
    "id": 1,
    "startAt": "10:00"
  }
}
```

**예약 날짜/시간 변경** `PATCH /reservations/{id}`
→ 성공 시 `200 OK`
→ 잘못된 날짜 형식, 올바르지 않은 시간 ID 형식이 들어올 시 `400 Bad Request`
→ 변경할 예약이 존재하지 않을 시 `404 Not Found`
→ 변경하려는 날짜와 시간에 이미 예약이 존재할 시 `409 Conflict`
→ 현재 시각보다 이전 날짜/시간으로 변경할 시 `422 Unprocessable Entity`

요청
```json
{
  "date": "2024-05-02",
  "timeId": 2
}
```
응답
```json
{
  "id": 1,
  "name": "홍길동",
  "date": "2024-05-02",
  "theme": {
    "id": 1,
    "name": "테마 이름",
    "description": "테마 설명",
    "thumbnailImgUrl": "https://example.com/thumbnail.jpg"
  },
  "time": {
    "id": 2,
    "startAt": "12:00"
  }
}
```

**예약 삭제** `DELETE /reservations/{id}`  
→ 성공 시 `204 No Content`  
→ 존재하지 않는 예약 삭제 시 `404 Not Found`  
→ 이미 지나간 예약 삭제 시 `422 Unprocessable Entity`

---

#### 예약 가능 시간 조회 (`/times`)

**예약 가능 시간 목록 조회** `GET /times?themeId={id}&date={date}` 
→ 성공 시 `200 OK`
→ 잘못된 날짜 형식, 숫자가 아니거나 양수가 아닌 테마 ID가 쿼리 파라미터로 올 시 `400 Bad Request`

응답
```json
[
  {
    "id": 1,
    "startAt": "10:00",
    "available": true
  },
  {
    "id": 2,
    "startAt": "12:00",
    "available": false
  }
]
```

> `available: false` 인 시간은 이미 예약된 시간

---

#### 테마 (`/themes`)

**전체 테마 조회** `GET /themes` → `200 OK`

응답
```json
[
  {
    "id": 1,
    "name": "테마 이름",
    "description": "테마 설명",
    "thumbnailImgUrl": "https://example.com/thumbnail.jpg"
  }
]
```

**인기 테마 상위 10개 조회** `GET /themes/popular-top-10` → `200 OK`

응답
```json
[
  {
    "id": 1,
    "name": "테마 이름",
    "description": "테마 설명",
    "thumbnailImgUrl": "https://example.com/thumbnail.jpg",
    "reservedCount": 42
  }
]
```

> 최근 1주(어제 기준) 예약 수 기준 내림차순 정렬

---

### 관리자 API

#### 예약 시간 관리 (`/admin/times`)

**전체 예약 시간 조회** `GET /admin/times` → `200 OK`

응답
```json
[
  {
    "id": 1,
    "startAt": "10:00"
  }
]
```

**예약 시간 생성** `POST /admin/times`
→ 성공 시 `201 Created`
→ 빈 시간, 잘못된 시간 형식이 요청 본문에 담길 시 `400 Bad Request`
→ 같은 시간이 이미 존재할 시 `409 Conflict`

요청
```json
{
  "startAt": "10:00"
}
```
응답
```json
{
  "id": 1,
  "startAt": "10:00"
}
```

**예약 시간 삭제** `DELETE /admin/times/{id}`  
→ 성공 시 `204 No Content`  
→ 존재하지 않는 예약 시간 삭제 시 `404 Not Found`  
→ 예약이 연결된 시간 삭제 시 `422 Unprocessable Entity`

---

#### 테마 관리 (`/admin/themes`)

**테마 생성** `POST /admin/themes` 
→ 성공 시 `201 Created`
→ 빈 이름, 빈 설명, 빈 썸네일 이미지 URL이 요청 본문에 담길 시 `400 Bad Request`
→ 이름과 설명이 같은 테마가 이미 존재할 시 `409 Conflict`

요청
```json
{
  "name": "테마 이름",
  "description": "테마 설명",
  "thumbnailImgUrl": "https://example.com/thumbnail.jpg"
}
```
응답
```json
{
  "id": 1,
  "name": "테마 이름",
  "description": "테마 설명",
  "thumbnailImgUrl": "https://example.com/thumbnail.jpg"
}
```

**테마 삭제** `DELETE /admin/themes/{id}`   
→ 성공 시 `204 No Content`  
→ 존재하지 않는 테마 삭제 시 `404 Not Found`  
→ 예약이 연결된 테마 삭제 시 `422 Unprocessable Entity`
