## 구현할 기능 목록

### 예약 시간 기능

- [x] 예약 시간을 추가한다.
    - [x] [예외] 예약 시간이 null이거나 공백인 경우, 400을 반환한다.
- [x] 예약 시간 목록을 조회한다.
    - [x] 테마와 날짜를 기준으로 예약 가능 여부(isNotReserved)를 함께 반환한다.
    - [x] [예외] 존재하지 않는 테마로 조회하는 경우, 404를 반환한다.
- [x] 예약 시간을 삭제한다.
    - [x] [예외] 존재하지 않는 예약 시간을 삭제하는 경우, 404를 반환한다.
    - [x] [예외] 예약이 존재하는 시간을 삭제하는 경우, 422를 반환한다.

### 테마 기능

- [x] 테마를 추가한다.
    - [x] [예외] 테마 이름이 null이거나 공백인 경우, 400을 반환한다.
    - [x] [예외] 테마 설명이 null이거나 공백인 경우, 400을 반환한다.
    - [x] [예외] 썸네일이 URL 형식이 아닌 경우, 400을 반환한다.
    - [x] [예외] 이미 존재하는 테마명으로 추가하는 경우, 409를 반환한다.
- [x] 테마 전체 목록을 조회한다.
- [x] 인기 테마 목록을 조회한다.
    - [x] 오늘 기준 최근 7일간 예약이 많은 테마 순으로 최대 10개를 반환한다.
- [x] 테마를 삭제한다.
    - [x] [예외] 존재하지 않는 테마를 삭제하는 경우, 404를 반환한다.
    - [x] [예외] 예약이 존재하는 테마를 삭제하는 경우, 422를 반환한다.

### 예약 기능

- [x] 예약을 추가한다.
    - [x] [예외] 예약자 이름이 null이거나 공백인 경우, 400을 반환한다.
    - [x] [예외] 예약 날짜가 null인 경우, 400을 반환한다.
    - [x] [예외] 존재하지 않는 시간으로 예약하는 경우, 404를 반환한다.
    - [x] [예외] 존재하지 않는 테마로 예약하는 경우, 404를 반환한다.
    - [x] [예외] 동일한 날짜+시간+테마에 이미 예약이 있는 경우, 409를 반환한다.
    - [x] [예외] 지나간 날짜·시간으로 예약하는 경우, 422를 반환한다.
- [x] 예약 전체 목록을 조회한다.
- [x] 예약자 이름으로 본인의 예약 목록을 조회한다.
- [x] 예약 날짜·시간을 변경한다.
    - [x] [예외] 존재하지 않는 예약을 변경하는 경우, 404를 반환한다.
    - [x] [예외] 지나간 날짜·시간으로 변경하는 경우, 422를 반환한다.
    - [x] [예외] 변경하려는 날짜+시간+테마에 이미 예약이 있는 경우, 409를 반환한다.
- [x] 예약을 삭제한다.
    - [x] [예외] 존재하지 않는 예약을 삭제하는 경우, 404를 반환한다.

### 예외 처리 기능

- [x] 서버 내부 오류 발생 시 500을 반환하며 사용자에게 노출되지 않도록 한다.

---

# API 명세서

## 관리자 - 예약 시간 추가

<details>
<summary>Request</summary>
<div markdown="1">

```
POST /admin/times HTTP/1.1
Content-Type: application/json

{
  "startAt": "10:00"
}
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 201
Content-Type: application/json
Location: /admin/times/1

{
  "id": 1,
  "startAt": "10:00"
}
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 예외: 예약 시간이 null이거나 공백이면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "예약 시간은 필수값 입니다."

</div>
</details>

---

## 관리자 - 예약 시간 삭제

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
HTTP/1.1 204
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 예외: 존재하지 않는 예약 시간은 삭제할 수 없다.
    - 상태 코드: 404
    - 예외 메시지: "예약 시간이 존재하지 않습니다."
- [x] 예외: 예약이 존재하는 시간을 삭제할 수 없다.
    - 상태 코드: 422
    - 예외 메시지: "예약된 시간은 삭제할 수 없습니다."

</div>
</details>

---

## 관리자 - 테마 추가

<details>
<summary>Request</summary>
<div markdown="1">

```
POST /admin/themes HTTP/1.1
Content-Type: application/json

{
  "name": "공포의 저택",
  "description": "오래된 저택에서 탈출하세요.",
  "thumbnail": "https://example.com/theme1.jpg"
}
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 201
Content-Type: application/json
Location: /admin/themes/1

{
  "id": 1,
  "name": "공포의 저택",
  "description": "오래된 저택에서 탈출하세요.",
  "thumbnail": "https://example.com/theme1.jpg"
}
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 예외: 테마 이름이 null이거나 공백이면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "테마 이름은 필수값 입니다."
- [x] 예외: 테마 이름이 30자를 초과하면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "테마 이름은 30자 이하여야 합니다."
- [x] 예외: 테마 설명이 null이거나 공백이면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "테마 설명은 필수값 입니다."
- [x] 예외: 테마 설명이 100자를 초과하면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "테마 설명은 100자 이하여야 합니다."
- [x] 예외: 테마의 썸네일이 URL 형식이 아니면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "테마의 썸네일은 URL 형식이어야 합니다."
- [x] 예외: 테마의 썸네일이 100자를 초과하면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "테마 썸네일은 100자 이하여야 합니다."
- [x] 예외: 테마가 이미 존재하면 추가할 수 없다.
    - 상태 코드: 409
    - 예외 메시지: "이미 존재하는 테마입니다."

</div>
</details>

---

## 관리자 - 테마 삭제

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
HTTP/1.1 204
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 예외: 존재하지 않는 테마는 삭제할 수 없다.
    - 상태 코드: 404
    - 예외 메시지: "존재하지 않는 테마입니다."
- [x] 예외: 예약이 존재하는 테마는 삭제할 수 없다.
    - 상태 코드: 422
    - 예외 메시지: "예약된 테마는 삭제할 수 없습니다."

</div>
</details>

---

## 사용자 - 예약 시간 전체 조회

<details>
<summary>Query Parameter</summary>
<div markdown="1">

- themeId (필수・long)
    - 예약하고 싶은 테마의 ID입니다.
- date (필수・string)
    - 예약하고 싶은 날짜입니다.
    - `yyyy-MM-dd` 형식입니다.

</div>
</details>

<details>
<summary>Request</summary>
<div markdown="1">

```
GET /times?themeId=1&date=2026-05-05 HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200
Content-Type: application/json

[
  {
    "id": 1,
    "startAt": "10:00",
    "isNotReserved": true
  },
  {
  "id": 2,
  "startAt": "11:00",
  "isNotReserved": false
  }
]
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 예외: 존재하지 않는 테마면 예약 시간을 조회할 수 없다.
    - 상태 코드: 404
    - 예외 메시지: "존재하지 않는 테마입니다."

</div>
</details>

---

## 사용자 - 테마 전체 조회

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
HTTP/1.1 200
Content-Type: application/json

[
  {
    "id": 1,
    "name": "공포의 저택",
    "description": "오래된 저택에서 탈출하세요.",
    "thumbnail": "https://example.com/theme1.jpg"
  },
  {
    "id": 2,
    "name": "사라진 연구소",
    "description": "비밀 연구소의 진실을 밝혀내세요.",
    "thumbnail": "https://example.com/theme2.jpg"
  }
]
```

</div>
</details>

---

## 사용자 - 인기 테마 조회

<details>
<summary>Request</summary>
<div markdown="1">

```
GET /themes/popular HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200
Content-Type: application/json

[
  {
    "id": 1,
    "name": "공포의 저택",
    "description": "오래된 저택에서 탈출하세요.",
    "thumbnail": "https://example.com/theme1.jpg"
  },
  {
    "id": 2,
    "name": "사라진 연구소",
    "description": "비밀 연구소의 진실을 밝혀내세요.",
    "thumbnail": "https://example.com/theme2.jpg"
  }
]
```

</div>
</details>

---

## 사용자 - 내 예약 조회

<details>
<summary>Query Parameter</summary>
<div markdown="1">

- name (필수・string)
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
HTTP/1.1 200
Content-Type: application/json

[
  {
    "id": 1,
    "name": "브라운",
    "date": "2026-05-05",
    "time": {
      "id": 1,
      "startAt": "10:00"
    },
    "theme": {
      "id": 1,
      "name": "공포의 저택",
      "description": "오래된 저택에서 탈출하세요.",
      "thumbnail": "https://example.com/theme1.jpg"
    }
  }
]

```

</div>
</details>

---

## 사용자 - 예약 날짜·시간 변경

<details>
<summary>Request</summary>
<div markdown="1">

```
PATCH /reservations/{reservationId} HTTP/1.1
Content-Type: application/json

{
  "date": "2026-05-05",
  "timeId": 1
}
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200
Content-Type: application/json

{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-05",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
   "theme": {
    "id": 1,
    "name": "공포의 저택",
    "description": "오래된 저택에서 탈출하세요.",
    "thumbnail": "https://example.com/theme1.jpg"
  }
}
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 예외: 존재하지 않는 예약을 변경할 수 없다.
    - 상태 코드: 404
    - 예외 메시지: "존재하지 않는 예약입니다."
- [x] 예외: 변경하려는 날짜+시간+테마에 이미 예약이 있으면 변경을 거부한다.
    - 상태 코드: 409
    - 예외 메시지: "동일한 날짜, 시간, 테마에 이미 예약이 존재합니다."
- [x] 예외: 이미 지난 날짜·시간으로 변경할 수 없다.
    - 상태 코드: 422
    - 예외 메시지: "지나간 날짜·시간으로 변경할 수 없습니다."

</div>
</details>

---

## 사용자/관리자 - 예약 추가

<details>
<summary>Request</summary>
<div markdown="1">

```
POST /reservations HTTP/1.1
Content-Type: application/json

{
  "name": "브라운",
  "date": "2026-05-05",
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
HTTP/1.1 201
Content-Type: application/json
Location: /reservations/1

{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-05",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "공포의 저택",
    "description": "오래된 저택에서 탈출하세요.",
    "thumbnail": "https://example.com/theme1.jpg"
  }
}
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 예외: 예약자 이름이 null이거나 공백이면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "예약자 이름은 필수값 입니다."
- [x] 예외: 예약자 이름이 10자를 초과하면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "예약자명은 10자 이하여야 합니다."
- [x] 예외: 예약 날짜가 null이거나 공백이면 예외가 발생한다.
    - 상태 코드: 400
    - 예외 메시지: "예약 날짜는 필수값 입니다."
- [x] 예외: 존재하지 않는 시간의 예약을 추가할 수 없다.
    - 상태 코드: 404
    - 예외 메시지: "존재하지 않는 예약 시간입니다."
- [x] 예외:  존재하지 않는 테마의 예약을 추가할 수 없다.
    - 상태 코드: 404
    - 예외 메시지: "존재하지 않는 테마입니다."
- [x] 예외: 같은 날짜+시간+테마에 이미 예약이 있으면 중복 예약을 거부한다.
    - 상태 코드: 409
    - 예외 메시지: "동일한 날짜, 시간, 테마에 이미 예약이 존재합니다."
- [x] 예외: 지나간 날짜·시간에 대한 예약 생성은 불가능하다.
    - 상태 코드: 422
    - 예외 메시지: "지나간 날짜·시간에 대한 예약 생성은 불가능 합니다."

</div>
</details>

---

## 사용자/관리자 - 예약 전체 조회

<details>
<summary>Request</summary>
<div markdown="1">

```
GET /reservations HTTP/1.1
```

</div>
</details>

<details>
<summary>Response</summary>
<div markdown="1">

```
HTTP/1.1 200
Content-Type: application/json

[
  {
    "id": 1,
    "name": "브라운",
    "date": "2026-05-05",
    "time": {
      "id": 1,
      "startAt": "10:00"
    },
    "theme": {
      "id": 1,
      "name": "공포의 저택",
      "description": "오래된 저택에서 탈출하세요.",
      "thumbnail": "https://example.com/theme1.jpg"
    }
  }
]
```

</div>
</details>

---

## 사용자/관리자 - 예약 삭제

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
HTTP/1.1 204
```

</div>
</details>

<details>
<summary>Exception</summary>
<div markdown="1">

- [x] 예외: 존재하지 않는 예약을 삭제할 수 없다.
    - 상태 코드: 404
    - 예외 메시지: "존재하지 않는 예약입니다."

</div>
</details>
