# 방탈출 예약 대기 (Room Escape Waiting)

## 예약 대기 승인

### 변경 요구사항

- 예약 대기를 승인하여 예약으로 전환
- 대기가 예약으로 전환되면 해당 슬롯의 나머지 대기 순번이 재정렬된다.
- 예약이 취소되면 해당 슬롯의 대기 순번이 재정렬된다.
- 요구사항에 명시되지 않은 엣지 케이스를 스스로 식별하고 처리한다.

**추가된 요구사항**

토론에서 정한 트랜잭션 경계에 맞춰 함께 일어나야 하는 데이터 변경을 묶고, 중간 실패 시 데이터 일관성이 유지되는지 테스트로 확인한다.

---

## 기능 목록

### 예약 취소 시 대기 자동 승격 (이번 사이클)

- [x] 예약을 취소하면 해당 슬롯의 첫 번째 대기자를 예약으로 자동 승격한다.
  - [x] 대기자가 없으면 아무도 승격하지 않고 취소만 수행한다.
  - [x] 승격된 대기는 대기 목록에서 제거된다.
- [x] 대기 승격 / 예약 취소 시 해당 슬롯의 남은 대기 순번이 재정렬된다. (순번은 `id` 기준으로 동적 계산)
- [x] 예약 취소와 대기 승격을 하나의 트랜잭션으로 묶어 원자적으로 처리한다.
- [x] 동시 취소 시 한 대기자가 중복 승격되지 않도록 첫 대기자 조회에 `FOR UPDATE` 락을 적용한다.

### 엣지 케이스 처리

- [x] 본인 예약이 아니면 취소할 수 없다. (`DomainConflictException`)
- [x] 존재하지 않는 예약을 취소하면 `RESERVATION_NOT_FOUND` 예외를 던진다.
- [x] 예약이 없는 슬롯에는 대기를 생성할 수 없다. (`WAITING_WITHOUT_RESERVATION`)
- [x] 이미 예약한 사람은 같은 슬롯에 대기를 걸 수 없다. (`DUPLICATE_RESERVATION`)
- [x] 같은 슬롯에 같은 사람이 중복 대기할 수 없다. (`DUPLICATE_WAITING`)

---

## API 명세

### 예약

| Method | URI | 설명 | 성공 응답 |
|---|---|---|---|
| GET | `/reservations?name={name}&page={page}&size={size}` | 내 예약 목록 조회 | 200 OK |
| POST | `/reservations` | 예약 생성 | 201 Created |
| PATCH | `/reservations/{id}?name={name}` | 예약 변경 | 200 OK |
| DELETE | `/reservations/{id}?name={name}` | 예약 취소 (→ 첫 대기자 자동 승격) | 204 No Content |

### 예약 대기

| Method | URI | 설명 | 성공 응답 |
|---|---|---|---|
| GET | `/waitings?name={name}&page={page}&size={size}` | 내 대기 목록 + 순번 조회 | 200 OK |
| POST | `/waitings` | 예약 대기 생성 | 201 Created |
| DELETE | `/waitings/{id}?name={name}` | 예약 대기 취소 | 204 No Content |

#### POST `/waitings` — 요청

```json
{
  "name": "레서",
  "date": "2026-06-07",
  "timeId": 1,
  "themeId": 1
}
```

#### POST `/waitings` — 응답 (201 Created)

```json
{
  "id": 1,
  "name": "레서",
  "date": "2026-06-07",
  "time": { "id": 1, "startAt": "10:00" },
  "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" },
  "order": 1
}
```

> `order`: 같은 슬롯(날짜·시간·테마) 내 대기 순번. `id`가 자신보다 작은 대기 수 + 1로 계산하므로,
> 앞선 대기가 승격·취소되면 재조회 시 자동으로 당겨진다.

#### GET `/waitings?name={name}` — 응답 (200 OK)

```json
{
  "waitingList": [
    {
      "id": 2,
      "name": "레서",
      "date": "2026-06-07",
      "time": { "id": 1, "startAt": "10:00" },
      "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" },
      "order": 2
    }
  ]
}
```
