# 학습 로그 #17

**학습 범위**: InnoDB 갭락(Gap Lock), Insert Intention Lock, Next-Key Lock, S락

## 1. 막힌 것의 종류

- [x] 개념 자체를 모르겠다
- [ ] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [ ] 코드는 돌아가는데 이게 맞는 건지 모르겠다
- [ ] 기타: ___

갭락을 걸 때 로우가 없는데 어떻게 락을 거는지 궁금해서 시작.

## 2. 이번 타임의 학습 전략

### 학습 과정

#### 갭락(Gap Lock)

- B-Tree 인덱스 구조 상에 존재하는 "빈 공간"에 거는 락
- 실제 행이 없어도 인덱스 구조는 존재하기 때문에 가능
- **INSERT만** 막는다 — SELECT/UPDATE/DELETE는 막지 않음
- 갭락끼리는 충돌하지 않는다 — 둘 다 "삽입 금지"를 원하는 것이라 공존 가능
- **거는 주체**: 범위 조회(SELECT FOR UPDATE 등)를 한 트랜잭션 — DB가 자동으로 거는 게 아님

---

#### Next-Key Lock

- Record Lock(X락) + Gap Lock을 합친 것
- 범위 조회(SELECT ... FOR UPDATE)할 때 걸린다
- 목적: **팬텀리드 방지** — 읽는 쪽이 거는 락

---

#### Insert Intention Lock

- INSERT 연산이 거는 특수한 락 (갭락이 아님)
- 같은 갭에 서로 다른 위치에 INSERT하면 충돌하지 않음
- 하지만 기존 Gap Lock과는 충돌 → 대기
- INSERT 흐름: Insert Intention Lock 획득 → 행 삽입 → Record Lock(X) 획득

---

#### S락 (Shared Lock)

- 일반 SELECT는 S락을 걸지 않는다 — MVCC로 스냅샷 읽기
- S락이 걸리는 경우: FK 체크, `SELECT ... FOR SHARE` 명시
- `SELECT ... FOR SHARE`: 읽은 값 기반으로 계산할 때 그 사이 수정을 막고 싶은 경우

---

#### 락 정리

| 연산 | 거는 락 |
|---|---|
| 범위 조회 (SELECT FOR UPDATE) | Next-Key Lock (Record + Gap) |
| INSERT | Insert Intention Lock → 성공 후 Record Lock(X) |
| UPDATE/DELETE | Record Lock (기존 행에만 작용, 갭락 무관) |
| 일반 SELECT | 락 없음 — MVCC로 스냅샷 읽기 |
| SELECT ... FOR SHARE | S락 |

---

#### tx 순서에 따른 충돌

| tx 순서 | 결과 |
|---|---|
| INSERT 먼저 → SELECT FOR UPDATE | SELECT가 Record Lock에서 대기 |
| SELECT FOR UPDATE 먼저 → INSERT | Insert Intention Lock이 Gap Lock과 충돌 → 대기 |

## 3. 전략 평가

- 효과적이었던 것: 갭락이 X락이라는 오개념을 충돌 여부 질문으로 스스로 수정했다
- 아쉬운 것: 개념의 특성과 충돌 여부를 연결해서 생각하는 훈련이 더 필요하다

## 4. AI 피드백

"INSERT를 하려는 트랜잭션이 갭락을 걸면 자기 자신을 막는 것" — 이 질문이 오늘 핵심이었다. Insert Intention Lock이 갭락과 다른 이유를 스스로 발견하게 유도했다.

## 5. 다음 타임에 바꿀 것

- 유지할 것: 개념의 목적에서 출발해서 충돌 여부를 추론하는 방식
- 바꿀 것: 오개념이 생겼을 때 더 빠르게 자기 점검하기

## 다음 사이클 키워드

```
✅ B-Tree에서 갭락이 물리적으로 어떻게 구현되는가
   → 맥락: 다음 키 삽입 공간을 어떻게 막는지 — 인덱스 구조 레벨에서
   → 종류: 흐름 파악 (갭락 개념은 이해했으니 구현 레벨로)

✅ SELECT ... FOR SHARE가 필요한 시나리오
   → 맥락: 읽은 값 기반 계산 중 다른 트랜잭션의 수정을 막아야 하는 경우
   → 종류: 실전 판단 (개념은 알았으니 언제 쓸지 판단하는 단계로)
```
