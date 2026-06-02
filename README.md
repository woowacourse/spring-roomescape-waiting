# 기능 요구 사항
## 1단계 - 예약 대기 신청/취소
- [x] 예약 대기 테이블 생성
- [x] 예약 대기 신청 시, 예약 대기 테이블에 추가
    - [x] 이미 예약이 존재하는 날짜/테마/시간에만 예약 대기 가능
    - [x] 본인 예약에는 예약 대기 불가
    - [x] 본인의 정보로 이미 예약 대기가 되어있는지 검증
    - [x] 예약 대기 순번은 같은 예약 슬롯의 생성 순서 기준으로 계산
- [x] 예약 대기 취소 시, 예약 대기 테이블에서 삭제
    - [x] 본인 예약인지 검증
    - [x] 이미 지난 과거의 예약인지 검증

## 2단계 - 내 예약 목록 조회 (상태 구분)
- [x] 예약과 예약 대기 리스트 반환
- [x] 예약 확정/대기중 상태 반환
- [x] 예약과 예약 대기를 구분할 수 있는 리소스 키 반환

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

이미 예약된 슬롯을 기준으로 예약 대기를 신청한다. 대기 슬롯은 `themeId`, `date`, `timeId` 조합으로 식별하며, 같은 슬롯의 대기 순번은 `created_at`, `id` 순서 기준으로 계산된다.

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
  "startAt": "10:00",
  "waitingNumber": 1
}
```

#### Error

| Status | Code | 설명 |
| --- | --- | --- |
| `400 Bad Request` | `INVALID_REQUEST` | 이름, 예약 시간, 테마 등 필수 요청 값이 올바르지 않은 경우 |
| `400 Bad Request` | `INVALID_DATE_FORMAT` | 날짜 형식이 `yyyy-MM-dd`가 아닌 경우 |
| `400 Bad Request` | `RESERVATION_NOT_EXISTS` | 해당 날짜, 테마, 시간에 확정 예약이 없어 대기할 수 없는 경우 |
| `400 Bad Request` | `CANNOT_CANCEL_PAST_RESERVATION_WAITING` | 이미 지난 날짜와 시간의 예약에 대기를 신청하거나 취소하는 경우 |
| `404 Not Found` | `RESERVATION_TIME_NOT_FOUND` | 존재하지 않는 예약 시간 ID인 경우 |
| `404 Not Found` | `THEME_NOT_FOUND` | 존재하지 않는 테마 ID인 경우 |
| `409 Conflict` | `DUPLICATED_RESERVATION` | 본인의 확정 예약에 대기를 신청하는 경우 |
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

사용자의 예약과 예약 대기를 함께 조회한다. `status` 값으로 확정 예약과 예약 대기를 구분하며, `resourceKey` 값으로 서로 다른 테이블의 리소스를 고유하게 식별한다.

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
| `resourceKey` | `String` | 리소스 타입과 ID를 조합한 고유 키. 예: `reservation:1`, `waiting:2` |
| `id` | `Number` | 예약 ID 또는 예약 대기 ID |
| `name` | `String` | 사용자 이름 |
| `themeName` | `String` | 테마 이름 |
| `date` | `String` | 예약 날짜 |
| `startAt` | `String` | 예약 시작 시간 |
| `status` | `String` | 예약 상태. 확정 예약은 `예약 확정`, 예약 대기는 `대기중` |
| `waitingNumber` | `Number` | 예약 대기 순번. 확정 예약이면 응답에서 제외됨 |

```json
[
  {
    "resourceKey": "reservation:1",
    "id": 1,
    "name": "초록",
    "themeName": "은하수",
    "date": "2026-05-28",
    "startAt": "10:00",
    "status": "예약 확정"
  },
  {
    "resourceKey": "waiting:2",
    "id": 2,
    "name": "초록",
    "themeName": "지구",
    "date": "2026-05-28",
    "startAt": "11:00",
    "status": "대기중",
    "waitingNumber": 1
  }
]
```
