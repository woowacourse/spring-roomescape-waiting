# 기능 요구 사항
## 사이클 1
### 1단계 - 예약 대기 신청/취소
- [x] 예약 대기 테이블 생성
- [x] 예약 대기 신청 시, 예약 대기 테이블에 추가
    - [x] 본인의 정보로 이미 예약 대기가 되어있는지 검증
    - [x] 예약 대기 테이블에 순번이 자동으로 +1 되어 삽입
- [x] 예약 대기 취소 시, 예약 대기 테이블에서 삭제
    - [x] 본인 예약인지 검증
    - [x] 이미 지난 과거의 예약인지 검증

## 2단계 - 내 예약 목록 조회 (상태 구분)
- [x] 예약과 예약 대기 리스트 반환

## 사이클 2
### 1단계 - 예약 대기 승인
- [x] 예약 취소 시 대기자 예약 전환
- [x] 예약 수정 시 대기자 예약 전환

# API 명세

## 공통
- 요청/응답 본문은 JSON 형식을 사용한다.
- 현재 코드 기준 별도 인증 토큰은 사용하지 않고, `name` 값으로 본인 여부를 확인한다.
- 에러 응답 형식

```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

## 1단계 - 예약 대기 신청/취소

### 예약 대기 신청

이미 예약된 슬롯을 기준으로 예약 대기를 신청한다. 대기 슬롯은 `themeId`, `date`, `timeId` 조합으로 식별하며, 같은 슬롯의 다음 대기 순번은 기존 최대 순번에 1을 더해 부여된다.

- Method: `POST`
- URL: `/reservation-waitings`

#### Request Body

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `name` | `String` | O | 예약 대기 신청자 이름 |
| `date` | `String` | O | 예약 날짜, `yyyy-MM-dd` 형식 |
| `timeId` | `Number` | O | 예약 시간 ID |
| `themeId` | `Number` | O | 테마 ID |

```json
{
  "name": "초록",
  "date": "2026-05-28",
  "timeId": 1,
  "themeId": 1
}
```

#### Response

- Status: `201 Created`
- Header: `Location: /reservation-waitings/{id}`

```json
{
  "id": 1,
  "name": "초록",
  "themeId": 1,
  "date": "2026-05-28",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "waitingNumber": 1
}
```

#### Error

| Status | Code | 설명 |
| --- | --- | --- |
| `400 Bad Request` | `INVALID_REQUEST` | 이름, 예약 시간, 테마 등 필수 요청 값이 올바르지 않은 경우 |
| `400 Bad Request` | `INVALID_DATE_FORMAT` | 날짜 형식이 `yyyy-MM-dd`가 아닌 경우 |
| `404 Not Found` | `RESERVATION_TIME_NOT_FOUND` | 존재하지 않는 예약 시간 ID인 경우 |
| `409 Conflict` | `DUPLICATED_RESERVATION_WAITING` | 같은 사용자가 같은 슬롯에 이미 대기한 경우 |

### 예약 대기 취소

사용자는 본인의 예약 대기만 취소할 수 있다. 취소 대상이 이미 지난 날짜와 시간이면 취소할 수 없다.

- Method: `DELETE`
- URL: `/reservation-waitings/my/{id}?name={name}`

#### Path Variable

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Number` | 예약 대기 ID |

#### Query Parameter

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `name` | `String` | O | 예약 대기 신청자 이름 |

#### Response

- Status: `204 No Content`

#### Error

| Status | Code | 설명 |
| --- | --- | --- |
| `400 Bad Request` | `CANNOT_CANCEL_PAST_RESERVATION_WAITING` | 이미 지난 시간의 예약 대기를 취소하는 경우 |
| `403 Forbidden` | `FORBIDDEN_RESERVATION_WAITING_ACCESS` | 다른 사용자의 예약 대기를 취소하려는 경우 |
| `404 Not Found` | `RESERVATION_WAITING_NOT_FOUND` | 존재하지 않는 예약 대기 ID인 경우 |

## 2단계 - 내 예약 목록 조회 (상태 구분)

사용자의 예약과 예약 대기를 함께 조회한다. 현재 응답에는 별도의 `status` 필드가 없으며, `waitingNumber`가 없으면 확정 예약, `waitingNumber`가 있으면 예약 대기로 구분한다.

- Method: `GET`
- URL: `/reservations/list?name={name}`

### Query Parameter

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `name` | `String` | O | 조회할 사용자 이름 |

### Response

- Status: `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Number` | 예약 ID 또는 예약 대기 ID |
| `name` | `String` | 사용자 이름 |
| `themeName` | `String` | 테마 이름 |
| `date` | `String` | 예약 날짜 |
| `startAt` | `String` | 예약 시작 시간 |
| `waitingNumber` | `Number` | 예약 대기 순번. 확정 예약이면 응답에서 제외됨 |

```json
[
  {
    "id": 1,
    "name": "초록",
    "themeName": "은하수",
    "date": "2026-05-28",
    "startAt": "10:00:00"
  },
  {
    "id": 2,
    "name": "초록",
    "themeName": "지구",
    "date": "2026-05-28",
    "startAt": "11:00:00",
    "waitingNumber": 1
  }
]
```
