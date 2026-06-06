# 동시성 테스트 계획

## 테스트 시나리오

race condition이 가장 잘 드러나는 케이스:
- CONFIRMED 예약 1개 (id=1)
- 같은 슬롯에 WAITING 예약 2개

| 상태 | 기대 결과 |
|---|---|
| 락 없이 | 동시에 두 취소 → waiter1, waiter2 둘 다 CONFIRMED → 같은 슬롯에 CONFIRMED 2개 |
| 락 있이 | CONFIRMED 최대 1개 |

---

## 테스트 데이터 SQL

```sql
-- 미래 날짜로 설정 (validateCancellable 통과용)
INSERT INTO reservation (name, date, created_at, time_id, theme_id, status)
VALUES ('host', '2027-01-01', NOW(), 1, 1, 'CONFIRMED');

INSERT INTO reservation (name, date, created_at, time_id, theme_id, status)
VALUES ('waiter1', '2027-01-01', NOW(), 1, 1, 'WAITING');

INSERT INTO reservation (name, date, created_at, time_id, theme_id, status)
VALUES ('waiter2', '2027-01-01', NOW(), 1, 1, 'WAITING');
```

---

## k6 스크립트

```javascript
// race_condition_test.js
import http from 'k6/http';

export const options = {
  vus: 2,
  iterations: 2, // VU 2개가 동시에 1번씩 실행
};

export default function () {
  const res = http.del('http://localhost:8080/reservations/1');
  console.log(`VU ${__VU}: status=${res.status}`);
}
```

실행:
```bash
k6 run race_condition_test.js
```

---

## 결과 확인 SQL

```sql
SELECT id, name, status
FROM reservation
WHERE date = '2027-01-01' AND time_id = 1 AND theme_id = 1
ORDER BY status, id;
```

- **재현 성공**: CONFIRMED 행이 2개 (waiter1, waiter2 둘 다 승인됨)
- **락 적용 후**: CONFIRMED 행이 1개

---

## 진행 순서

1. `findByIdForUpdate` → `findById`로 임시 변경 후 앱 재시작 (MySQL로)
2. 테스트 데이터 INSERT
3. k6 실행 → DB 확인 → 캡처
4. 코드 원복 후 앱 재시작
5. 테스트 데이터 다시 INSERT
6. k6 재실행 → DB 확인 → 캡처

> 한 번에 재현되지 않으면 데이터 초기화 후 반복 실행
