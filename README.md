# 예약 대기 API 명세

Base URL: `/waiting-list`

---

## 1. 예약 대기 목록 조회

예약자 이름으로 본인의 대기 목록을 조회합니다.

**`GET /waiting-list?name={name}`**

### Query Parameter

- `name` (String, 필수) : 예약자 이름

### Response `200 OK`

```json
[
  {
    "id": 1,
    "name": "홍길동",
    "date": "2025-06-01",
    "timeId": 2,
    "themeId": 3,
    "waitingOrder": 1,
    "status": "WAITING_LIST"
  }
]
```

- `id` (Long) : 예약 대기 ID
- `name` (String) : 예약자 이름
- `date` (String) : 예약 날짜 (`yyyy-MM-dd`)
- `timeId` (Long) : 예약 시간 ID
- `themeId` (Long) : 테마 ID
- `waitingOrder` (Integer) : 대기 순번 (1부터 시작)
- `status` (String) : 상태 (`WAITING_LIST` 고정)

---

## 2. 예약 대기 신청

특정 날짜, 시간, 테마에 예약 대기를 신청합니다. 해당 슬롯에 확정 예약이 존재할 때만 신청 가능합니다.

**`POST /waiting-list`**

### Request Body

```json
{
  "name": "홍길동",
  "date": "2025-06-01",
  "timeId": 2,
  "themeId": 3
}
```

- `name` (String, 필수) : 예약자 이름
- `date` (String, 필수) : 예약 날짜 (`yyyy-MM-dd`)
- `timeId` (Long, 필수) : 예약 시간 ID
- `themeId` (Long, 필수) : 테마 ID

### Response `201 Created`

```json
{
  "id": 5,
  "name": "홍길동",
  "date": "2025-06-01",
  "timeId": 2,
  "themeId": 3,
  "waitingOrder": 2,
  "status": "WAITING_LIST"
}
```

---

## 3. 예약 대기 취소

본인의 예약 대기를 취소합니다. 이름 불일치 시 취소가 거부됩니다.

**`DELETE /waiting-list/{id}`**

### Path Variable

- `id` (Long, 필수) : 예약 대기 ID

### Request Body

```json
{
  "name": "홍길동"
}
```

- `name` (String, 필수) : 예약자 이름

### Response `204 No Content`

응답 바디 없음.

---

## 에러 응답 형식

모든 에러는 아래 형식으로 반환됩니다.

```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

### 에러 코드 목록

**400 Bad Request**
- `PERSON_NAME_NULL_OR_BLANK` : 예약자 이름이 비어있음
- `DATE_NULL` : 날짜가 누락됨
- `TIME_ID_NULL` : 시간 ID가 누락됨
- `THEME_ID_NULL` : 테마 ID가 누락됨

**403 Forbidden**
- `USER_NAME_NOT_MATCHED` : 예약자 이름과 요청자 이름이 일치하지 않음

**404 Not Found**
- `WAITING_LIST_NOT_FOUND` : 해당 예약 대기 정보를 찾을 수 없음
- `THEME_NOT_FOUND` : 해당 테마 정보를 찾을 수 없음
- `TIME_NOT_FOUND` : 해당 예약 시간 정보를 찾을 수 없음

**422 Unprocessable Entity**
- `WAITING_LIST_NOT_REQUIRED` : 확정 예약이 없어 대기 신청 불가
- `ALREADY_ON_WAITING_LIST` : 동일 조건으로 이미 대기 신청이 존재함
- `DATE_ALREADY_PASSED` : 이미 지난 날짜
- `TIME_ALREADY_PASSED` : 이미 지난 시간
