# 02 · 1단계 — JPA 전환 (매핑 / 연관관계 / 영속성 컨텍스트)

> 이 미션에서 **분량 최대** 단계. 1-1 매핑 → 1-2 연관관계 → 1-3 관찰의 3중 구조.
> 커밋: `[1단계] ...` / 모델:시작 전 `01-concepts §3`(코드 다시 읽기) 펴기.

---

## 들어가기 전 자기진단

**Q. 본인 코드의 Repository에서 가장 자주 등장하는 SQL 패턴은?**
> 🖊️ (가설) `01-concepts §3①` 참고 — SELECT+3중 조인이 조회 핵심. _(여기 작성)_

**Q. 객체 참조로 옮겼을 때 더 자연스러워지는 곳은?**
> 🖊️ (가설) RowMapper의 손수 조립 → `reservation.getTime().getStartAt()`. _(여기 작성)_

---

## 1-1. 매핑 변환 (독립 클래스부터: Theme, ReservationTime)

### 요구사항 (LMS)

- `build.gradle`: `spring-boot-starter-jdbc` → `spring-boot-starter-data-jpa`
- `@Entity`·`@Id`·`@GeneratedValue(strategy = IDENTITY)`
- `JpaRepository<T, Long>` 작성, Jdbc 기반 Repository 제거, `KeyHolder`/`SimpleJdbcInsert` 잔재 제거
- `application.properties`: `show-sql` / `hibernate.format_sql` / `ddl-auto=create-drop` /
  `defer-datasource-initialization=true`

### 전환 전 (JDBC)

> 🖊️ `JdbcThemeRepository.save` INSERT + KeyHolder 블록 발췌 _(붙여넣기)_

### 전환 후 (JPA)

> 🖊️ `@Entity` Theme + `ThemeRepository extends JpaRepository` _(작성)_
> 🖊️ 막힘 예상: 기존 도메인이 `withId` 정적 팩토리·불변 → protected 기본 생성자 추가, id 할당 방식.

### 비교 관찰 포인트 (LMS — 이 셋 안 보이면 "그냥 양쪽 돌려본 것")

| 관찰                                         | 예측 | 실제 |
|--------------------------------------------|----|----|
| ① 시작 시 발행 DDL 차이 (schema.sql vs JPA 자동 생성) |    |    |
| ② 재시작 시 데이터 보존 여부                          |    |    |
| ③ 컬럼명·타입을 entity로만 제어 가능한지                 |    |    |

### 확인 과제

**Q. 예약 생성 시 콘솔 INSERT SQL이 방탈출 미션과 어떻게 같고 다른가?**
> 🖊️ (예측) 컬럼·VALUES 동일, 단 JPA는 바인딩 로깅·생성 키 회수 방식 다름. (실제) _(작성)_

---

## 1-2. 연관관계 매핑 (Reservation → Theme, ReservationTime)

> ⚠️ step2엔 member 없음 → `Reservation`은 `Theme`·`ReservationTime`만 `@ManyToOne`. 예약자 `name`은 String 유지. (member 도입 여부는
> 2단계에서 별도 판단)

### 요구사항 (LMS)

- `@ManyToOne` + `@JoinColumn(name="..._id")`로 객체 참조
- **단방향 시작**, 양방향은 필요 생기면
- 양방향 시 연관관계 주인 명시 + 무한 직렬화 검토
- `cascade`/`orphanRemoval`은 필요 전 미적용 (적용 시 PR 근거)

### 전환 전 (JDBC)

> 🖊️ `JdbcReservationRepository`의 3중 조인 SELECT + RowMapper 발췌 _(붙여넣기)_

### 전환 후 (JPA)

> 🖊️ `@ManyToOne @JoinColumn(name="time_id")` 등 _(작성)_

### 확인 과제

**Q. `findById(reservationId).getTime().getStartAt()`이 발행하는 SQL은?**
> 🖊️ (예측) EAGER면 처음부터 join? LAZY면 getStartAt() 시점 추가 SELECT? (실제) _(작성, 발행 SQL 붙여넣기)_

### 결정 기록 — 단/양방향 시도→후퇴 (차원 B)

> 🖊️ LMS가 "양방향/cascade를 한 번 시도했다 후퇴하는 사이클을 의식적으로 굴려라"고 명시. 그 흔적을 여기 남긴다.

- 선택한 것:
- 비교한 대안: (단방향 / 양방향+mappedBy / cascade 적용)
- 선택 비교 기준:
- 한계 / 다음에 망가질 지점:
- 시도→후퇴 흔적: _(무엇을 시도했다 왜 되돌렸나)_ **(B)**

---

## 1-3. 영속성 컨텍스트 관찰 ⭐ 차원 A 핵심

> 코드를 추가하기보다 **관찰**. 아래 6개를 직접 만들어 4필드(시도 코드 / 예측 / 실제 / 왜 다른가)로 캡처. 예측과 실제의 갭이 이 미션의 핵심 학습 신호.

### ① dirty checking

```
시도 코드:  @Transactional 메서드에서 entity 필드 수정 후 save 미호출
예측:       commit 시점 UPDATE 자동 발행?
실제:
왜 다른가:
```

### ② 1차 캐시

```
시도 코드:  같은 트랜잭션에서 findById 두 번
예측:       두 번째 SELECT 생략?
실제:
왜 다른가:
```

### ③ 쓰기 지연

```
시도 코드:  save 호출 후 flush 전·후 DB 상태 비교
예측:       INSERT가 flush/commit 시점 일괄? (단 IDENTITY는 즉시?)
실제:
왜 다른가:
```

### ④ flush 시점

```
시도 코드:  명시적 flush / 트랜잭션 종료 / JPQL 실행 직전
예측:       각 트리거에서 동기화?
실제:
왜 다른가:
```

### ⑤ fetch 기본값 (@ManyToOne vs @OneToMany)

```
시도 코드:  무명시 상태로 조회
예측:       @ManyToOne EAGER / @OneToMany LAZY 차이?
실제:
왜 다른가:
```

### ⑥ LazyInitializationException

```
시도 코드:  트랜잭션 밖에서 LAZY 필드 접근
예측:       컨텍스트 닫힌 후 프록시 미초기화 예외?
실제:
왜 다른가:
```

> 🖊️ reads를 트랜잭션-free로 둔 내 결정이 ⑥의 발생 구간을 어떻게 넓히는지도 함께 기록.

---

## 테스트 변경 사항 (차원 C)

> 🖊️ 헥사고날 repository slice / 통합 테스트가 JDBC 구현 제거로 깨질 것. 무엇이 왜 깨졌고 어떻게 고쳤나.

- 깨진 테스트 / 원인 / 수정: **(C)**

## 피드백 채널 신호 (차원 C)

- SQL 로그에서 의외였던 것:
- 예외(LazyInit 등)가 먼저 알려준 것:

## 막힌 지점 → 다음 정의

- 막힘:
- 다음 정의:

## 얻은 인사이트

- _(A/B/C 태그와 함께)_

## 이 단계가 회수한 차원

- A: / B: / C:
