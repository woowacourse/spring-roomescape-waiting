# 사이클2 사전학습: 함께 일어나는 작업 분석

---

## 실험1: 함께 일어나는 작업 찾기

### 결론: 가장 위험한 기능은 `cancelReservation`이다

`cancelReservation`은 최대 3개의 DB 변경이 순서대로 일어나고, 중간 실패 시 대기자가 영원히 PENDING에 갇히는 사용자 경험 문제가 생긴다.

---

### 기능 1 — 예약 생성 (`saveReservation`)

**함께 변경되는 데이터**
1. `theme_slot.is_reserved` → `true` (UPDATE)
2. `reservation` 행 INSERT, 상태 `CONFIRMED`

**첫 번째만 성공 시 (ThemeSlot 업데이트 ✓, Reservation 저장 ✗)**

| 테이블 | 상태 |
|---|---|
| `theme_slot` | `is_reserved = true` |
| `reservation` | 행 없음 |

**사용자가 보게 되는 것**
슬롯이 예약된 것처럼 보이지만 실제 예약자가 없다.
다음 사용자가 같은 슬롯에 예약을 시도하면 대기(PENDING)로 빠지고, 영원히 CONFIRMED로 승격되지 않는다.

---

### 기능 2 — 예약 취소 (`cancelReservation`) ← 가장 위험

**함께 변경되는 데이터**

대기자가 있는 경우 (3단계):
1. 취소 대상 Reservation 상태 → `CANCELLED` (UPDATE)
2. 첫 번째 PENDING 대기자 상태 → `CONFIRMED` (UPDATE)
3. ThemeSlot `is_reserved` 유지 (대기자가 있으므로 true 유지)

대기자가 없는 경우 (2단계):
1. 취소 대상 Reservation 상태 → `CANCELLED` (UPDATE)
2. `theme_slot.is_reserved` → `false` (UPDATE)

**대기자 있을 때: 1단계만 성공 시 (취소 ✓, 대기자 승격 ✗)**

| 테이블 | 상태 |
|---|---|
| 취소 대상 Reservation | `CANCELLED` |
| 대기자 Reservation | 여전히 `PENDING` |
| `theme_slot` | `is_reserved = true` |

**사용자가 보게 되는 것**
대기자의 화면에서 "대기 1번"이 그대로 남아 있다.
슬롯은 예약된 것처럼 표시되지만 실제 CONFIRMED 예약자가 없다.
이후 어떤 사용자도 자동으로 CONFIRMED가 되지 않아 슬롯이 반영구적으로 잠긴다.

**대기자 없을 때: 1단계만 성공 시 (취소 ✓, ThemeSlot 해제 ✗)**

| 테이블 | 상태 |
|---|---|
| 취소 대상 Reservation | `CANCELLED` |
| `theme_slot` | `is_reserved = true` (여전히 예약됨으로 표시) |

**사용자가 보게 되는 것**
슬롯이 여전히 예약된 것처럼 보인다.
다른 사용자가 해당 슬롯에 예약을 시도하면 CONFIRMED가 아닌 PENDING으로 처리된다.
실제로는 빈 슬롯인데 대기열이 쌓이는 상황이 된다.

---

### 기능 3 — 예약 변경 (`modifyReservation`)

**함께 변경되는 데이터**
1. 기존 `theme_slot.is_reserved` → `false` (UPDATE)
2. 새 `theme_slot.is_reserved` → `true` (UPDATE)
3. `reservation.theme_slot_id` 변경 (UPDATE)

**1단계만 성공 시 (기존 슬롯 해제 ✓, 새 슬롯 예약 ✗)**

| 테이블 | 상태 |
|---|---|
| 기존 `theme_slot` | `is_reserved = false` (해제됨) |
| 새 `theme_slot` | `is_reserved = false` (예약 안 됨) |
| `reservation` | 여전히 기존 슬롯 참조 |

**사용자가 보게 되는 것**
기존 슬롯이 풀려 다른 사람이 예약할 수 있게 된다.
내 예약은 기존 슬롯을 가리키고 있어 데이터 불일치 발생.
예약 목록 조회 시 이전 날짜/시간이 그대로 표시된다.

---

### 기능 4 — 예약 삭제 (`removeReservation`)

**함께 변경되는 데이터**
1. `reservation` 행 DELETE
2. `theme_slot.is_reserved` → `false` (UPDATE)

**1단계만 성공 시 (Reservation 삭제 ✓, ThemeSlot 해제 ✗)**

| 테이블 | 상태 |
|---|---|
| `reservation` | 삭제됨 |
| `theme_slot` | `is_reserved = true` (여전히 예약됨으로 표시) |

**사용자가 보게 되는 것**
슬롯이 영구적으로 잠긴다.
예약 목록에서는 사라졌는데 해당 슬롯으로는 새 예약을 해도 CONFIRMED가 되지 않는다.

---

### 현재 코드 관찰: `@Transactional`이 이미 붙어 있지만

`saveReservation`, `cancelReservation`, `modifyReservation`, `removeReservation` 모두 `@Transactional`이 붙어 있다.
그러나 `completeReservation`은 `@Transactional`이 없다.
완료 처리도 상태를 바꾸는 쓰기 작업임에도 트랜잭션 보호가 없다.

---

## 실험2: 대기 승인 흐름 설계

### 결론: 자동 전환을 선택한다

예약 취소 시 대기 1번이 즉시 CONFIRMED로 승격되는 자동 전환 방식을 선택한다.
사용자가 별도 액션 없이 순서대로 혜택을 받고, 관리자 개입이 필요 없으므로 사용자 경험과 시스템 단순성 모두 좋다.

---

### 자동 전환 흐름에서 함께 일어나야 하는 데이터 변경

```
CONFIRMED 예약 취소 요청
    │
    ├─ 1. 취소 대상 Reservation.status → CANCELLED
    │
    ├─ [대기자 있음]
    │      ├─ 2. 대기 1번 Reservation.status → CONFIRMED
    │      └─ 3. ThemeSlot.is_reserved 유지 (true)
    │
    └─ [대기자 없음]
           ├─ 2. ThemeSlot.is_reserved → false
           └─ (끝)
```

**하나로 묶어야 하는 변경 (원자성이 필요한 이유)**

1, 2번은 반드시 함께 성공하거나 함께 실패해야 한다.
- 1번 성공 + 2번 실패 → CONFIRMED 예약자가 없는데 슬롯은 잠긴 상태
- 이 상태에서 슬롯을 되돌릴 방법이 없고, 사용자는 자신이 대기 1번인지조차 알 수 없다

---

### 동시 요청 시나리오: 지금 코드로는 막을 수 없다

두 사용자 A, B가 같은 슬롯에 동시에 취소 요청을 보내는 경우:

```
A의 트랜잭션: reservation 조회 → CONFIRMED 확인 → ...
B의 트랜잭션: reservation 조회 → CONFIRMED 확인 → ...
A: status → CANCELLED, 대기자 없음 → ThemeSlot.is_reserved = false
B: status → CANCELLED (이미 취소된 것에 또 취소)
```

현재 코드는 조회 후 변경 사이에 다른 트랜잭션이 끼어들 수 있다.
낙관적 락(버전 컬럼)이나 비관적 락(`SELECT FOR UPDATE`) 없이는 두 트랜잭션이 같은 데이터를 동시에 읽고 각자 변경할 수 있다.

또한 두 사용자가 동시에 같은 빈 슬롯에 예약을 시도하는 경우:

```
A: existsByThemeSlotId → false (비어 있음 확인)
B: existsByThemeSlotId → false (비어 있음 확인)
A: ThemeSlot.is_reserved = true, Reservation INSERT (CONFIRMED)
B: ThemeSlot.is_reserved = true, Reservation INSERT (CONFIRMED) ← 두 명 모두 CONFIRMED
```

같은 슬롯에 두 개의 CONFIRMED 예약이 생길 수 있다.

---

## 토론에서 묻고 싶은 질문

**`cancelReservation`에서 대기자 승격(2번)이 실패했을 때, 이미 커밋된 취소(1번)를 어떻게 보상할 것인가?**

트랜잭션으로 묶으면 함께 롤백되어 해결된다.
하지만 트랜잭션 없이 보상 트랜잭션(Saga 패턴)을 직접 구현해야 한다면 어떻게 접근해야 하는가?
그리고 비동기 처리가 들어간다면 트랜잭션 경계는 어디까지가 적절한가?
