# 학습 로그 #24

**학습 범위**: 도메인 리팩토링 — WaitingService 규칙의 도메인 이전 (행 간 불변식 vs 단일 객체 판단, 정적 팩토리 패턴)

## 1. 막힌 것의 종류

- [ ] 개념 자체를 모르겠다
- [ ] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [x] 코드는 돌아가는데 이게 맞는 건지 모르겠다
- [ ] 기타: ___

WaitingService에 퍼진 규칙들 중 어느 것을 도메인으로 옮겨야 하는지, 어떤 구조로 옮겨야 하는지 불명확했다.

## 2. 이번 타임의 학습 내용

### 세 규칙의 분류

WaitingService.create()에 세 규칙이 있었다:
- ① 예약 존재 확인 (reservationDao 조회) → Repository 자리
- ② 동일 사용자 대기 불가 (reservation + member 비교) → 도메인 가능
- ③ 중복 대기 확인 (waitingDao 조회) → Repository 자리

### 분류 기준: 행 간 불변식

"행 간 불변식(여러 행을 봐야 판단되는 규칙)은 도메인 객체 하나가 자기 자신만 알기 때문에 판단 불가 → DAO/Repository 자리"

①과 ③은 이 기준으로 탈락. ②는 reservation + member 두 객체만 보면 판단 → 도메인으로 이전.

### 설계: private 생성자 + named factory

후보 탈락법:
- 생성자에 검증 넣기 → DB 복원 시에도 발동 → 탈락
- 팩토리만 쓰고 생성자는 public → 우회 가능 → 강제 불가 → 탈락
- **private 생성자 + create(검증) + reconstruct(복원용)** → 강제됨 + 의도 명확 ✓

Reservation.createByUser에서 `now`가 저장 없이 검증에만 쓰이는 선례 확인
→ Waiting.create(member, reservation)에 동일 패턴 적용.

### 결과

```
Waiting.create(member, reservation)  → isSameMember 검증 → private Waiting(...)
Waiting.reconstruct(id, member, ...)  → 검증 없음 (DB 복원용)
```

WaitingService에서 isSameMember 검사 제거, buildWaiting 제거, timeDao/themeDao 의존성 제거.

## 3. 전략 평가

**효과적이었던 것**: ①②③ 분류에서 이번 사이클에 배운 "행 간 불변식" 원칙을 새 코드에 바로 적용했다.

**아쉬운 것**: Reservation.createByUser 선례를 코치가 가리키기 전에 user 스스로 먼저 도달하지 못했다. user도 스스로 "저번에 한 것을 다시 적용한 느낌"이라고 진단했다 — 그 패턴은 이미 아는 것을 확인한 것에 가까웠다.

## 4. 이전 "바꿀 것" 적용 여부

- **바꿀 것 1** (결론부터 내리지 않기): 추론 과정에서 단계를 밟으려 했다. 부분 적용.
- **바꿀 것 2** (설계 결정을 user가 먼저 제시하도록): user가 먼저 "생성 팩토리로", "생성자를 닫고 create/reconstruct 만들면 되겠다"를 제시했다. 그러나 createByUser(now) 선례 연결은 코치가 가리켰다. **부분 적용(~60%)**.

## 5. 다음 타임에 바꿀 것

**유지할 것**

"저번에 한 것을 다시 적용한 느낌"이라고 스스로 진단한 것. 패턴이 체득됐다는 신호를 알아채고 말로 꺼내는 메타 인식 능력. 이 자기 진단이 계속 나오면 i+1 수준이 올라가고 있다는 뜻이다.

**바꿀 것**

오늘 user가 "가능할 것 같아"라고 했다 — **다음번에 실제로 확인한다.** 코치가 "Reservation 보세요"라고 가리키기 전에, user가 먼저 "이거 저번 패턴이에요"라고 말하는 것. 선례를 *발견*하는 것이 목표다.

트리거: 생성자/팩토리 설계 결정이 나오는 순간 → 코치 힌트 없이 user가 먼저 패턴 이름을 꺼내기. 오늘처럼 구조는 스스로 제시했지만(✓), 기존 코드의 선례 연결(createByUser의 now)은 아직 코치 의존(△). 그 한 단계가 다음 확인 포인트다.

## 다음 사이클 키워드

```
✅ DTO 변환을 Slot 단위로 (이미 등록)
   → 맥락: getter 위임은 1단계. DTO가 slot을 직접 받도록 점진 개선
   → 종류: 코드 적용
```
