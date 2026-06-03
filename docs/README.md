# 🚣🏻사이클 1 - 예약 대기

## 🍐 페어 프로그래밍
### 1단계 - 예약 대기 신청/취소
- [x] 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
- [x] 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
- [x] 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
- [x] 사용자는 본인의 대기를 취소할 수 있다.

### 2단계 - 내 예약 목록 조회 (상태 구분)
- [x] 이전 미션의 내 예약 목록 조회를 확장한다.
- [x] 사용자의 예약과 대기가 상태로 구분되어 함께 표시된다.
- [x] 대기에는 본인의 대기 순번도 함께 보여준다.

### 기능 목록
- [x] 에약 대기 기능 추가
- [x] 예약 대기 순번 계산
- [x] 예약 대기 취소 기능 추가
- [x] 예약 및 대기 조회 기능 추가
- [x] 예약 가능한 시간에 대기 인원 정보 추가
- [x] 예약 수정 기능
- [x] 예약 취소 기능

### 예외 처리
- [x] 같은 슬롯에 대한 중복 대기 불가 예외처리
- [x] 본인의 예약이 아닌 경우 대기 취소 불가
- [x] 이미 날짜가 지난 예약 대기는 취소 불가 

## ⚙️ 추가 리팩토링

#### 주목: DisplayStatus는 ReservationResponse 외에서 절대 참조하지 않는다.

- [x] 예외 정의 및 처리 스타일 정리
- [x] Controller 응답을 `ResponseEntity` 기반으로 일관화
- [x] 예약 상태 구조 정리
    - [x] `schema.sql`에 `WAITING` 상태 추가
    - [x] 대기 순번 컬럼 제거, 순번은 비즈니스 로직에서 계산
    - [x] 조회 응답용 `DisplayStatus` 추가, 완료된 예약은 `COMPLETED`로 표시
- [x] 예약 생성/수정/취소 시 대기 승격 및 순번 재계산 흐름 반영
- [x] Service 책임 일부를 Domain으로 이동
    - [x] 예약 생성, 수정, 취소 가능 여부 검증을 도메인 메서드로 위임
- [x] 예약 동시성 제어 보강
    - [x] Service 레벨 `exists()` 사전 검증
    - [x] DB unique 제약 추가
    - [x] `DuplicateKeyException` 처리로 race condition 보정
    - [ ] 예약 대기 동시성 제어(따닥) 아직 미흡
      - Reservation이 2개 들어왔을 경우
      - 한 사용자가 한 스케줄에 중복 저장 성공했을 경우
- [x] 전체 네이밍 일관성 정리
- [x] 테스트 구조 재정리
    - [x] DAO: `@JdbcTest`
    - [x] Service: Mockito 단위 테스트
    - [x] Controller: `@WebMvcTest`
    - [x] Domain 단위 테스트 추가
    - [x] `@DirtiesContext` 의존 제거

---
# 🧗🏻사이클 2 - 예약 대기 승인(자동 전환 선택)

## 🥋트랜잭션 경계 결정

**`saveReservation()`**
1. 스케줄 조회/생성 (FOR UPDATE - 락 획득)
2. 본인 중복 예약 확인
3. 현재 상태 계산 ('RESERVED' or 'WAITING')
4. 예약 생성 business 검증
5. 예약 저장
- 예약 생성 과정에서 수행되는 중복 예약 검증, 예약 상태 계산, 예약 저장은 동일한 스케줄에 대한 현재 상태를 기반으로 수행됩니다. 따라서 이 과정 중간에 다른 트랜잭션이 개입하면 잘못된 상태 결정(e.g. RESERVED가 여러 개 생성됨)이 발생할 수 있습니다. 이를 방지하기 위해 스케줄에 대한 락을 획득한 뒤 검증, 상태 계산, 저장까지를 하나의 트랜잭션으로 묶었습니다.

**`cancelReservation()`**
1. 취소하려는 예약의 reservationId로 스케줄 조회 (FOR UPDATE - 락 획득)
2. 이미 취소된 예약이면 pass
3. 예약 취소 business 검증
4. 'CANCELD' 상태로 변경
5. 취소된 예약이 해당 스케줄의 에약 확정자였을 경우 첫 번째 대기자가 승격
- 예약 취소 과정에서 수행되는 예약 취소 검증, 예약 상태 변경, 대기 예약 승격은 동일한 스케줄의 예약 상태를 변경하는 작업입니다. 따라서 이 과정 중간에 다른 트랜잭션이 개입하면 잘못된 상태가 발생할 수 있습니다. 예를 들어, 예약 확정자가 취소된 이후 대기 예약 승격 전에 다른 예약 생성 요청이 들어오면, 대기 순서가 꼬이거나 예약 확정자가 없는 상태가 일시적으로 노출될 수 있습니다. 이를 방지하기 위해 스케줄에 대한 락을 획득한 뒤 취소 검증, 상태 변경, 대기 예약 승격까지를 하나의 트랜잭션으로 묶었습니다.

**`getOrCreateScheduleForUpdate()`, `lockById()`**
- 별도 트랜잭션을 새로 열지 않고, 상위 예약 트랜잭션 안에서만 실행되도록 `MANDATORY`로 제한했습니다.
- schedule lock은 이후의 중복 확인, count 계산, 저장까지 같은 트랜잭션 안에서 유지되어야 의미가 있기 때문입니다.

**`findAll()`, `findByName()`, `findAvailableTimes()`**
- 조회 전용 트랜잭션으로 분리했습니다. 클래스 레벨에 `readOnly=true`를 선언해놓았기 때문에 메서드에 별도의 어노테이션이 붙지않아도 적용됩니다.
- 데이터를 변경하지 않으며, `COMPLETED`/`EXPIRED` 같은 표시 상태와 대기 순번은 조회 시점에 계산되는 값이기 때문입니다.

**`saveTheme()`, `saveReservationTime()`**
- 중복 검증과 저장을 하나의 트랜잭션으로 묶었습니다.
- 사전 검증 후 저장 사이에 같은 값이 들어올 수 있으므로 DB unique 제약과 예외 처리를 함께 사용합니다.

**`deleteTheme()`, `deleteReservationTime()`**
- 참조 여부 확인과 삭제를 하나의 트랜잭션으로 묶었습니다.
- 확인 후 삭제 사이의 변경 가능성은 DB FK 제약으로 한 번 더 방어합니다.

## ♟️ 동시성 제어 전략

**[Lock 방식을 사용하게 된 이유]**

지금까지의 제 짧은 경험으로는, 가장 쉬운 동시성 제어 기법은 Service에서 명시적 검증 + DB UNIQUE 제약 라고 생각합니다. 하지만, 제가 설계한 reservation 단일 테이블 기준으로 동일한 `schedule_id`에 대해 여러 명이 'WAITING'으로 들어올 수 있습니다. 또한, 동일한 스케줄에 대해서 동일한 사용자가 여러번의 'CANCELED'도 가능해야 한다고 생각했습니다. 위와 같은 이유로 `(schedule_id, status)` UNIQUE 제약 조건을 사용할 수 없었습니다. 여러 중복 행이 정상적으로 존재해야 하는 구조라서요. (추가적으로, 동시성 제어 전략을 탐구하며 조건부 중복 방지를 위해 Generated Column + UNIQUE Index GENERATED 와 같은 문법이 있다는 것도 알게되었습니다. 하지만, 추가하지는 않았습니다. 추후에 조건이 추가되면 (e.g. 어떤 비즈니스적인 요구사항에 의해 활성화에 매칭되는 다른 타입이 추가) 수정이 어렵다는 단점이 보였으며, 테이블의 스키를 좀 더 깔끔하게 가져가는 쪽으로 구현을 해보고 싶었습니다.)

저는 schedule(slot)과  reservation으로 정규화하여 1:N 관계로 테이블 구조로 설계했습니다. 그러므로, 모든 예약 생성/취소는 특정 `schedule`의 row를 기준으로 발생하게 됩니다. 즉, 동시성 충돌은 항상 동일한 `schedule_id`를 가진 reservation들 사이에서만 발생한다는 것을 알 수 있습니다. 따라서, `schedule` row를 잠그면 해당 슬롯에 대한 모든 동시 접근이 직렬화되어, reservation 테이블에는 별도의 잠금 없이도 동시성 문제를 해결할 수 있다고 생각했습니다. 

**[비관적 락을 선택하게 된 이유: 낙관적 락 vs 비관적 락]**

현재 예약 도메인에서는 예약 상태를 계산하는 과정 자체가 중요한 비즈니스 규칙이었고, 충돌이 발생한 뒤 재시도하는 것보다 동일한 `schedule`에 대한 작업을 처음부터 직렬화하는 것이 더 자연스럽다고 판단했습니다. 따라서 `schedule` row를 `FOR UPDATE`로 잠근 뒤, 상태 계산과 예약 저장이 끝날 때까지 잠금을 유지하는 비관적 락 방식을 선택했습니다.

예를 들어 빈 슬롯에 두 명이 동시에 예약하면:
- 낙관적 락: 한 명 성공, 한 명 충돌 예외 가능
- 비관적 락: 한 명 RESERVED, 다음 한 명 WAITING -> 이 방식이 적절하다고 판단했습니다.

**[동시성 문제 해결 시나리오]**

다음은 제가 동시성 문제를 해결하기 위해 생각했던 대표적인 동시성 문제 발생 상황과 문제 해결 시나리오입니다.
1. 같은 사용자 같은 스케줄 RESERVED 예약 중복
```
A: schedule 락 획득 → 중복 확인 → 저장 → 커밋(락 해제)
B: 대기 → 락 획득 → 중복 확인 → 예외 발생
```
2. save-save 동시 실행으로 같은 슬롯 RESERVED 2개
```
A: schedule 락 획득 → RESERVED 없음 → RESERVED 저장 → 커밋
B: 대기 → 락 획득 → RESERVED 있음 → WAITING 저장
```
3. cancel-cancel 동시 실행으로 대기 승격 중복
```
A: schedule 락 획득 → 취소 → 승격 → 커밋
B: 대기 → 락 획득 → isAlreadyCanceled() → return
```
4. cancel 후 승격 중 save 실행
```
A (cancel): schedule 락 획득 → 취소 → 승격 중...
B (save):   대기 → A 커밋 후 락 획득 → RESERVED 있음(승격된 것) → WAITING 저장
```

## 🔒 인증 / 인가 

> - 사용자는 본인의 대기를 취소할 수 있다.
> - 요구사항에 명시되지 않은 엣지 케이스를 스스로 식별하고 처리한다.

지금까지는 로그인 서비스가 구현되지 않은 상태이기 때문에 관리자 모드와 사용자 모드의 경계와 사용자 간의 경계가 전혀 없었습니다. 누구나 관리자 API에 접근할 수 있었고, 다른 사용자의 예약을 취소하는 것도 가능했습니다. 이번 단계에서는 이러한 문제를 해결하기 위해 이번 사이클 2에서 인증과 인가도 추가로 구현해보았습니다.

세션 기반 인증 + Interceptor/ArgumentResolver 로 구현하였습니다.
- 현재 브라우저 기반 same-origin 환경이기도 하고, 보안 구현 복잡도를 낮추기 위해 세션 쿠키 방식을 선택했습니다.

### 인증 (Authentication)
**판단 위치: 1. 로그인 인증: `AuthController` / `AuthService` 2. 인증 정보 복원: `LoginMemberArgumentResolver`**

- 로그인 시 `AuthController.login()`에서 loginId/password를 검증하고, 
- 성공하면 `session.setAttribute("loginMemberId", member.getId())`로 세션에 memberId를 저장합니다.
- 이후 요청마다 `@LoginMember` 어노테이션이 붙은 컨트롤러 파라미터가 있으면 `LoginMemberArgumentResolver.resolveArgument()`가 실행됩니다. 
- 세션에서 `loginMemberId`를 꺼내 `AuthService.getLoginMember(id)`로 Member를 조회해 파라미터에 주입합니다. 
- 세션이 없거나 memberId가 없으면 `UNAUTHENTICATED` 예외를 던집니다.

### 인가 (Authorization)

#### 1. 관리자/사용자 간 인가
**판단 위치: `AdminAuthorizationInterceptor` (Interceptor)**

- `@AdminOnly` 어노테이션 을 사용해 관리자를 구분하도록 했습니다.
- `preHandle()`에서 핸들러 메서드 또는 컨트롤러 클래스에 `@AdminOnly`가 붙어있는지 확인합니다. 
- 없으면 통과, 있으면 세션에서 Member를 조회해 Role이 `ADMIN`이 아니면 `UNAUTHORIZED_ADMIN` 예외를 던집니다. 

#### 2. 사용자 간 인가
**판단 위치: `Member.validateSameMember()` (domain)**

- 예약 취소 시 `Reservation.cancelBy(member, now)` 내부에서 예약자와 요청자의 id를 비교합니다. 
- 본인 예약이 아니면 `UNAUTHORIZED_RESERVATION` 예외를 던집니다. 
- 관리자는 `cancelByAdmin()`을 통해 이 검증 없이 취소할 수 있습니다.

### 전체 흐름 요약

```
요청 
  → AdminAuthorizationInterceptor.preHandle(): 관리자/사용자 간 인가
    → Controller 메서드 진입
      → LoginMemberArgumentResolver.resolveArgument(): 인증 (Member 주입)
        → 비즈니스 로직
         → Member.validateSameMember(): 사용자 간 인가
```

