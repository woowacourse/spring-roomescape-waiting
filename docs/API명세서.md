# API 명세서

## 테마

### 관리자 - 테마 추가

<details>
<summary>Request</summary>
<div markdown="1">

```
POST /admin/themes HTTP/1.1
Content-Type: application/json

{
    "name": "블루룸",
    "description": "깊은 바닷속 미스터리를 풀어라. 60분 안에 탈출하지 못하면 영원히 갇힌다.",
    "thumbnail": "https://via.placeholder.com/150"
}
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 201 Created
Location: /admin/themes/1
Content-Type: application/json

{
    "id": 1,
    "name": "블루룸",
    "description": "깊은 바닷속 미스터리를 풀어라. 60분 안에 탈출하지 못하면 영원히 갇힌다.",
    "thumbnail": "https://via.placeholder.com/150"
}
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 이미 존재하는 테마면 예외가 발생한다.
    - 상태 코드: 409 Conflict
    - 에러 코드: THEME_ALREADY_EXISTS
    - 메시지: "이미 존재하는 테마입니다."

</div>
</details>

---

### 관리자 - 테마 삭제

<details>
<summary>Request</summary>
<div markdown="1">

```
DELETE /admin/themes/{themeId} HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 204 No Content
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 존재하지 않는 테마면 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: THEME_NOT_FOUND
    - 메시지: "존재하지 않는 테마입니다."

- [x] 테마에 예약이 존재하면 예외가 발생한다.
    - 상태 코드: 409 Conflict
    - 에러 코드: THEME_HAS_RESERVATION
    - 메시지: "해당 테마를 지닌 예약이 존재합니다."

</div>
</details>

---

### 사용자 - 테마 목록 조회

<details>
<summary>Request</summary>
<div markdown="1">

```
GET /themes HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "name": "블루룸",
        "description": "깊은 바닷속 미스터리를 풀어라.",
        "thumbnail": "https://via.placeholder.com/150"
    }
]
```

</div>
</details>

---

### 사용자 - 인기 테마 조회

<details>
<summary>Description</summary>
<div markdown="1">

오늘 날짜(currentDate) 기준 **직전 7일(currentDate-7 ~ currentDate-1)** 의 예약 데이터를 집계하여 예약 수 기준 상위 테마를 반환합니다.

예) 오늘이 2026-06-04이면 **2026-05-28 ~ 2026-06-03** 기간의 예약을 기준으로 집계합니다.

</div>
</details>

<details>
<summary>Request</summary>
<div markdown="1">

```
GET /themes/rankings HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "name": "인기 테마",
        "description": "인기 있는 테마입니다.",
        "thumbnail": "https://via.placeholder.com/150"
    }
]
```

</div>
</details>

---

## 예약 시간

### 관리자 - 예약 시간 추가

<details>
<summary>Request</summary>
<div markdown="1">

```
POST /admin/times HTTP/1.1
Content-Type: application/json

{
    "startAt": "14:00"
}
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 201 Created
Location: /admin/times/1
Content-Type: application/json

{
    "id": 1,
    "startAt": "14:00"
}
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 이미 존재하는 시간이면 예외가 발생한다.
    - 상태 코드: 409 Conflict
    - 에러 코드: RESERVATION_TIME_ALREADY_EXISTS
    - 메시지: "이미 존재하는 시간입니다."

- [x] 영업 시간이 아니면 예외가 발생한다. (10:00 ~ 22:00)
    - 상태 코드: 422 Unprocessable Entity
    - 에러 코드: INVALID_RESERVATION_TIME_RANGE
    - 메시지: "영업 시간이 아닙니다."

- [x] 예약 시간 단위가 정시가 아니면 예외가 발생한다. (0분 단위)
    - 상태 코드: 400 Bad Request
    - 에러 코드: INVALID_RESERVATION_TIME_UNIT
    - 메시지: "잘못된 예약 시간 단위입니다."

</div>
</details>

---

### 관리자 - 예약 시간 삭제

<details>
<summary>Request</summary>
<div markdown="1">

```
DELETE /admin/times/{timeId} HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 204 No Content
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 존재하지 않는 시간이면 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: RESERVATION_TIME_NOT_FOUND
    - 메시지: "존재하지 않는 예약 시간입니다."

- [x] 해당 시간에 예약이 존재하면 예외가 발생한다.
    - 상태 코드: 409 Conflict
    - 에러 코드: RESERVATION_TIME_HAS_RESERVATION
    - 메시지: "해당 예약 시간에 예약이 존재합니다."

</div>
</details>

---

### 사용자 - 시간 목록 조회

<details>
<summary>Query Parameter</summary>
<div markdown="1">

- themeId (필수・long)
    - 예약하고 싶은 테마의 ID 입니다.
- baseDate (필수・string)
    - 예약하고 싶은 날짜 입니다.
    - `yyyy-MM-dd` 형식입니다.

</div>
</details>

<details>
<summary>Request</summary>
<div markdown="1">

```
GET /times?themeId=1&baseDate=2026-06-10 HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "startAt": "14:00",
        "reserved": true
    },
    {
        "id": 2,
        "startAt": "15:00",
        "reserved": false
    }
]
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 존재하지 않는 테마면 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: THEME_NOT_FOUND
    - 메시지: "존재하지 않는 테마입니다."

- [x] 지난 날짜면 예외가 발생한다.
    - 상태 코드: 400 Bad Request
    - 에러 코드: PAST_DATE_NOT_ALLOWED
    - 메시지: "지난 날짜는 선택할 수 없습니다."

</div>
</details>

---

## 예약

### 사용자 - 예약 추가

<details>
<summary>Request</summary>
<div markdown="1">

```
POST /reservations HTTP/1.1
Content-Type: application/json

{
    "name": "브라운",
    "date": "2026-05-08",
    "timeId": 1,
    "themeId": 1
}
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 201 Created
Location: /reservations/1
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2026-05-08",
    "time": {
        "id": 1,
        "startAt": "14:00"
    },
    "theme": {
        "id": 1,
        "name": "블루룸",
        "description": "깊은 바닷속 미스터리를 풀어라.",
        "thumbnail": "https://via.placeholder.com/150"
    }
}
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 해당 시간에 이미 예약이 존재하면 예외가 발생한다.
    - 상태 코드: 409 Conflict
    - 에러 코드: RESERVATION_ALREADY_EXISTS
    - 메시지: "해당 시간에는 이미 예약이 존재합니다."

- [x] 지난 날짜는 선택할 수 없다.
    - 상태 코드: 400 Bad Request
    - 에러 코드: PAST_DATE_NOT_ALLOWED
    - 메시지: "지난 날짜는 선택할 수 없습니다."

- [x] 존재하지 않는 시간이면 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: RESERVATION_TIME_NOT_FOUND
    - 메시지: "존재하지 않는 예약 시간입니다."

- [x] 존재하지 않는 테마면 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: THEME_NOT_FOUND
    - 메시지: "존재하지 않는 테마입니다."

</div>
</details>

---

### 사용자 - 예약 목록 조회

<details>
<summary>Query Parameter</summary>
<div markdown="1">

- name (선택・string)
    - 예약자 이름입니다.

</div>
</details>

<details>
<summary>Request</summary>
<div markdown="1">

```
GET /reservations?name=브라운 HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2026-05-08",
        "time": {
            "id": 1,
            "startAt": "14:00"
        },
        "theme": {
            "id": 1,
            "name": "블루룸",
            "description": "깊은 바닷속 미스터리를 풀어라.",
            "thumbnail": "https://via.placeholder.com/150"
        }
    }
]
```

</div>
</details>

---

### 사용자 - 예약 수정

> 날짜(`date`)와 시간(`timeId`)만 변경 가능합니다. 예약자 이름과 테마는 변경할 수 없습니다.

<details>
<summary>Request</summary>
<div markdown="1">

```
PUT /reservations/{reservationId} HTTP/1.1
Content-Type: application/json

{
    "date": "2026-06-10",
    "timeId": 2
}
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2026-06-10",
    "time": {
        "id": 2,
        "startAt": "15:00"
    },
    "theme": {
        "id": 1,
        "name": "블루룸",
        "description": "깊은 바닷속 미스터리를 풀어라.",
        "thumbnail": "https://via.placeholder.com/150"
    }
}
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 존재하지 않는 예약이면 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: RESERVATION_NOT_FOUND
    - 메시지: "존재하지 않는 예약입니다."

- [x] 해당 시간에 이미 다른 예약이 존재하면 예외가 발생한다.
    - 상태 코드: 409 Conflict
    - 에러 코드: RESERVATION_ALREADY_EXISTS
    - 메시지: "해당 시간에는 이미 예약이 존재합니다."

- [x] 지난 날짜/시간으로 수정할 수 없다.
    - 상태 코드: 400 Bad Request
    - 에러 코드: PAST_DATE_NOT_ALLOWED
    - 메시지: "지난 날짜는 선택할 수 없습니다."

- [x] 예약 변경 가능 시간이 지나면 예외가 발생한다.
    - 상태 코드: 422 Unprocessable Entity
    - 에러 코드: RESERVATION_CANCEL_DEADLINE_PASSED
    - 메시지: "예약 변경 및 취소 가능한 시간이 지났습니다."

- [x] 존재하지 않는 시간이나 테마로 수정 시 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: RESERVATION_TIME_NOT_FOUND / THEME_NOT_FOUND

</div>
</details>

---

### 사용자 - 예약 삭제

<details>
<summary>Request</summary>
<div markdown="1">

```
DELETE /reservations/{reservationId} HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 204 No Content
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 존재하지 않는 예약이면 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: RESERVATION_NOT_FOUND
    - 메시지: "존재하지 않는 예약입니다."

- [x] 예약 변경 및 취소 가능한 시간이 지나면 예외가 발생한다.
    - 상태 코드: 422 Unprocessable Entity
    - 에러 코드: RESERVATION_CANCEL_DEADLINE_PASSED
    - 메시지: "예약 변경 및 취소 가능한 시간이 지났습니다."

</div>
</details>

---

## 예약 대기

### 사용자 - 예약 대기 신청

<details>
<summary>Request</summary>
<div markdown="1">

```
POST /waitings HTTP/1.1
Content-Type: application/json

{
    "date": "2026-05-08",
    "timeId": 1,
    "themeId": 1,
    "name": "브라운"
}
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 201 Created
Location: /waitings/1
Content-Type: application/json

{
    "id": 1,
    "createdAt": "2026-05-05T09:00:00",
    "slotId": 1,
    "name": "브라운"
}
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 이미 동일한 대기를 신청한 경우 예외가 발생한다.
    - 상태 코드: 409 Conflict
    - 에러 코드: WAITING_ALREADY_EXISTS
    - 메시지: "이미 존재하는 대기입니다."

- [x] 본인이 예약한 슬롯에 대기를 신청할 경우 예외가 발생한다.
    - 상태 코드: 409 Conflict
    - 에러 코드: CANNOT_WAIT_OWN_RESERVATION
    - 메시지: "본인의 예약에는 대기를 신청할 수 없습니다."

- [x] 존재하지 않는 슬롯(날짜, 시간, 테마)일 경우 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: SLOT_NOT_FOUND
    - 메시지: "존재하지 않는 슬롯입니다."

- [x] 해당 슬롯에 예약이 존재하지 않는 경우 예외가 발생한다.
    - 상태 코드: 400 Bad Request
    - 에러 코드: RESERVATION_REQUIRED_FOR_WAITING
    - 메시지: "예약이 없는 슬롯에는 대기를 생성할 수 없습니다."

</div>
</details>

---

### 사용자 - 예약 대기 목록 조회

<details>
<summary>Query Parameter</summary>
<div markdown="1">

- name (필수・string)
    - 대기자 이름입니다.

</div>
</details>

<details>
<summary>Request</summary>
<div markdown="1">

```
GET /waitings?name=브라운 HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "createdAt": "2026-05-05T09:00:00",
        "slotId": 1,
        "name": "브라운",
        "rank": 1,
        "date": "2026-05-08",
        "startAt": "14:00",
        "themeName": "블루룸"
    }
]
```

</div>
</details>

---

### 사용자 - 예약 대기 취소

<details>
<summary>Query Parameter</summary>
<div markdown="1">

- name (필수・string)
    - 대기 신청 시 사용한 이름입니다.

</div>
</details>

<details>
<summary>Request</summary>
<div markdown="1">

```
DELETE /waitings/{waitingId}?name=브라운 HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 204 No Content
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 존재하지 않거나 본인의 대기가 아닌 경우 예외가 발생한다.
    - 상태 코드: 404 Not Found
    - 에러 코드: WAITING_NOT_FOUND
    - 메시지: "존재하지 않는 대기입니다."

</div>
</details>
