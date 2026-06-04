# 학습 로그 #27

**학습 범위**: 트랜잭션 경계 롤백을 테스트로 증명하기 — `@MockitoSpyBean`으로 승격 중간 실패를 주입하고, Service의 `@Transactional`이 원자적으로 롤백되는지 검증. (Mock vs Spy / `@SpringBootTest` vs `@JdbcTest` / `BDDMockito` 스타일)

## 1. 막힌 것의 종류

- [ ] 개념 자체를 모르겠다
- [ ] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [x] 코드는 돌아가는데 이게 맞는 건지 모르겠다 (테스트는 통과했는데 *왜* 이 도구/설정이어야 하는지)

테스트(`WaitingPromotionConsistencyTest`)는 이미 통과한 상태에서 출발. 의문은 "통과한다"가 아니라 **"왜 SpyBean이고, 왜 `@SpringBootTest`이고, 왜 `doThrow`인가"** — 도구 선택의 근거였다.

## 2. 이번 타임의 학습 내용

### Q1. SpyBean은 트랜잭션 때문에 쓴 게 아니다

- 처음 가설: "AOP(트랜잭션) 적용 때문에 SpyBean을 쓴 것?" → **인과 없음**.
- 롤백을 만든 트랜잭션은 **`ReservationService.cancel()`에 붙은 `@Transactional`**이 시작한 것. spy든 아니든 원래 걸려 있다.
- `@SpringBootTest`는 트랜잭션을 **안 걸어준다** (그래서 옆 `ReservationConcurrencyTest`가 `@AfterEach`에서 수동 DELETE).
- SpyBean의 진짜 용도: **"진짜 DAO인데 `delete` 하나만 실패시키기"** — 중간 실패 주입.

### Mock vs Spy

| | Mock | Spy |
|---|---|---|
| 기반 | 껍데기(전부 가짜) | **진짜 객체** |
| 정의 안 한 메서드 | null/기본값 | **진짜 동작 그대로** |
| 우리 케이스 | `findQueue`·`insert`·`existsById` 다 stub해야 | `delete`만 예외, 나머진 진짜 |

> **"전부 가짜"면 Mock, "거의 진짜인데 일부만 다르게"면 Spy.** 우리 테스트는 DAO의 다른 메서드들이 실제 DB와 동작해야 하므로 Spy.

### Q2. `@JdbcTest`로는 안 되는 이유 (두 겹)

1. `@JdbcTest`는 슬라이스 테스트 → **Service 빈을 안 띄움** → 검증 대상인 Service의 `@Transactional`이 아예 작동 안 함.
2. `@JdbcTest`는 그 자체로 **테스트 트랜잭션**을 검 → 이게 방해물.

**핵심 메커니즘 (Q2 심화)**: 트랜잭션은 쓰레드에 커넥션이 바인딩되는 방식. 테스트가 트랜잭션을 쥐고 있으면, `cancel()`의 `@Transactional`은 새 트랜잭션을 만드는 게 아니라 **그 트랜잭션에 합류(join)**한다.
- 합류 상태에서 승격 실패 → 예외는 밖으로 나가지만 **물리적 롤백은 테스트 메서드 끝날 때로 미뤄짐**.
- 그 전에 검증 `findById(...)`가 **같은(아직 안 끝난) 트랜잭션 안에서** 조회 → 방금 update한 **CANCELED를 봄** → `isEqualTo(BOOKED)` **깨짐**.

> **독립적인 커밋/롤백 경계를 관찰하려면, 테스트가 트랜잭션을 쥐고 있으면 안 된다.** → `@SpringBootTest`(트랜잭션 없음) + 수동 DELETE가 이 테스트엔 정직한 선택.

### Q3. BDD 스타일은 있다 — 단 spy엔 함정

- 주입(`@MockitoSpyBean`)은 그대로. **stubbing 문법만** `Mockito` → `BDDMockito`.
- `doThrow(...).when(spy).delete(...)` → `willThrow(...).given(spy).delete(...)`.
- **함정**: 보통 BDD는 `given(mock.method()).willReturn(...)` 형태지만, spy에 `given(spy.delete(id))`를 쓰면 괄호 안 `delete(id)`가 **진짜로 실행**돼 버린다(부작용). 그래서 spy/예외에는 메서드를 호출하지 않는 `willThrow(...).given(spy).delete(...)` 형태를 쓴다.

### 적용한 코드

```java
import static org.mockito.BDDMockito.willThrow;

willThrow(new RuntimeException("승격 중 강제 실패")).given(waitingDao).delete(anyLong());

assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), owner))
        .isInstanceOf(RuntimeException.class);
// 1. 취소 롤백 → 여전히 BOOKED
// 2. 승격 insert 롤백 → 대기자는 예약 없음
// 3. 대기 그대로 존재
```

## 3. 전략 평가

**효과적이었던 것**

- 가설을 본인이 먼저 던지고("AOP 때문인가?", "BDDSpyBean 있어?") 코치가 그 가설을 탈락시키는 흐름. 특히 Q2에서 **"같은 쓰레드에 커넥션이 바인딩된다"**는 메커니즘을 스스로 꺼낸 게 컸다 — 이게 join/롤백 지연 결론으로 바로 이어짐.

**아쉬운 것 (학습 방식)**

코치가 **SpyBean**에 들어갈 때, 사용자가 그 개념을 아예 모르는 상태였는데 친숙도를 먼저 묻지 않고 곧장 "AOP 때문인가?" 가설을 탈락시키는 흐름으로 들어갔다. 사용자 본인이 "처음 보는 건지 물었다면 더 좋았겠다"고 짚음. → log_26 바꿀 것 #1이 *바로 그 순간*에 안 지켜진 케이스.

## 4. 이전 "바꿀 것" 적용 여부

- **log_26 바꿀 것 #1** ("새 개념 들어가기 전 '이거 아는 거예요, 처음 보는 거예요?'로 친숙도 먼저 가늠"): **미적용**. SpyBean(사용자가 아예 모르던 개념)에 들어갈 때 친숙도를 안 묻고 바로 가설 탈락으로 진입. 사용자가 "물었으면 더 좋았겠다"고 직접 확인 → 규칙의 유효성은 재확인됐고, 실행이 안 된 것.
- **log_26 유지할 것** ("본인이 먼저 중간 의문을 던지는 흐름"): **적용됨**. 오늘 "AOP 때문인가?", "BDDSpyBean 있어?", Q2의 "커넥션이 쓰레드에 바인딩되기 때문" 등 본인이 먼저 가설/메커니즘을 능동적으로 제기.

## 5. 다음 타임에 바꿀 것

**유지할 것**

본인이 먼저 가설을 던지고 코치가 그걸 탈락시키는 흐름 (오늘 잘 작동함). 계속 가져간다.

**바꿀 것 (log_26 #1을 그대로 이월 — 새 규칙 추가 없이 이것 하나만 체득)**

1. **새 개념에 들어가기 전, 코치가 "이거 아는 거예요, 처음 보는 거예요?"로 친숙도를 먼저 묻는다.** **Why**: 오늘 SpyBean처럼 사용자가 아예 모르는 개념인데 곧장 가설 탈락으로 들어가면, 발판이 없는 상태에서 추측을 강요받는다. **How**: 새 명사(클래스/애너테이션/패턴)가 처음 등장하는 순간을 트리거로 삼아, 추측 질문을 던지기 *전에* 친숙도 한 줄을 먼저 묻는다. (적용률이 낮으므로 이번엔 이 규칙 하나에만 집중하고 다른 학습법은 건드리지 않는다.)

## 6. 다음 사이클 키워드

```
✅ @Transactional propagation — 승격을 별도 트랜잭션(REQUIRES_NEW)으로 떼면 무엇이 달라지나
   → 맥락: 이번 PR 본문에 "취소 실패를 알릴 수단이 있었다면 트랜잭션을 분리했을 것"이라고 적었다.
           그 "분리"를 실제로 한다면? cancel은 커밋되고 promote만 REQUIRES_NEW로 떼는 그림.
           - join(REQUIRED) vs 새 트랜잭션(REQUIRES_NEW)에서 롤백 범위가 어떻게 갈리나
           - promote가 실패해도 cancel은 살아남는 구조를 테스트로 어떻게 관찰하나
             (이번에 배운 "테스트가 트랜잭션 쥐면 경계가 가려진다"의 직접 응용)
   → 종류: 코드 적용 (개념 → 실제 분리 시나리오)
   → 연결: 오늘 Q2의 "join/롤백 지연", PR 본문의 트랜잭션 경계 결정
```
