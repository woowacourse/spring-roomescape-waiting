# 예약 대기

## 1단계 - 예약 대기 신청/취소

### 1. 예약 대기 신청 API

#### 구현

- [x] 구현 완료

#### 메서드 / URL

- POST /reservation-waitings
    - 예약 대기 신청은 예약과는 구분되는 독립된 자원이기 때문에, `/reservations-waitings`로 url을 둔다.

#### 요청

```json
{
  "name": "브라운",
  "date": "2026-05-26",
  "timeId": 1,
  "themeId": 1
}
```

#### 응답

- 201 created

- header

```text
Location: /themes/{id}
```

- body

```json
{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-28",
  "time": {
    "id": 1,
    "startAt": "10:00:00"
  },
  "theme": {
    "id": 1,
    "name": "우주선 탈출",
    "description": "고장 난 우주선에서 제한 시간 안에 탈출하세요.",
    "thumbnailUrl": "https://example.com/themes/space-escape.jpg"
  }
}
```

### 2. 예약 대기 취소 API

#### 구현

- [x] 구현 완료

#### 메서드 / URL

- DELETE /reservation-waitings/{id}

#### 요청

- header

```text
Authorization: 브라운
```

#### 응답

- 204 No Content

## 2단계 - 내 예약 목록 조회 (상태 구분)

### 1. 본인 예약 목록 조회 API

#### 구현

- [x] 구현 완료

#### 메서드 / URL

- GET /reservations
    - 전체 예약 중 특정 예약자의 예약만 필터링 하는 것이기 때문에, 쿼리 파라미터로 이름을 넘긴다.
    - 또한 단순 조회이기 때문에, 별도의 인증/인가는 수행하지 않는다.

#### 요청

- query parameter

```text
name=브라운
```

#### 응답

- 200 Ok

```json
[
  {
    "id": 1,
    "name": "브라운",
    "date": "2026-05-26",
    "time": 1,
    "theme": 1,
    "status": "waiting",
    "waitingOrder": 3
  }
]
```
