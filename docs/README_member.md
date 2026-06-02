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

### **1단계 - 테마 도메인 추가**

- [x]  관리자의 테마 추가
    - 테마 이름, 테마 설명, 테마 썸네일 이미지 URL을 RequestBody로 보낸다.
- [x]  관리자의 테마 삭제
- [x]  `Reservation` 클래스에 Theme 필드 추가

### **2단계 - 사용자 예약**

- [x]  날짜를 선택하면 해당 날짜의 테마 조회 기능 추가
- [x]  날짜와 테마를 선택하면 예약 가능한 시간 목록 조회 기능 추가
    - 예약 불가능한 시간과 가능한 시간 전부 조회 가능
    - 불가능한 시간은 선택이 불가능하게 처리
- [x]  사용자의 방탈출 예약 기능 추가
    - Request Body로
        1. 본인 이름
        2. 선택 날짜
        3. 테마 (테마 ID)
        4. 시간 (시간 ID)

      4개의 필드 포함

    - 이미 예약되어있는 날짜, 테마, 시간에 방탈출 예약 불가

### **3단계 - 인기 테마 조회**

- [x]  최근 1주 동안 예약이 많았던 테마 상위 10개 조회 기능 추가
    - 최근 1주는 오늘 제외하고 어제 기준으로 1주 동안의 기간

---

## API 명세서

### 사용자 API

#### 예약 (`/reservations`)

**전체 예약 조회** `GET /reservations` → `200 OK`

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

**예약 생성** `POST /reservations` → `201 Created`

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

**예약 삭제** `DELETE /reservations/{id}`  
→ 성공 시 `204 No Content`  
→ 실패 시 `404 Not Found`

---

#### 예약 가능 시간 조회 (`/times`)

**예약 가능 시간 목록 조회** `GET /times?themeId={id}&date={date}` → `200 OK`

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

**예약 시간 생성** `POST /admin/times` → `201 Created`

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
→ 실패 시 `404 Not Found`

---

#### 테마 관리 (`/admin/themes`)

**테마 생성** `POST /admin/themes` → `201 Created`

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
→ 실패 시 `404 Not Found`
