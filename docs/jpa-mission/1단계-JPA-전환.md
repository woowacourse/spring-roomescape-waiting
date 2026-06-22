## 4. 1단계 - JPA 전환

평가 대기

제출 완료2026. 6. 21. 제출

# 1단계: JPA 전환

JdbcTemplate Repository를 JPA Repository로 전면 교체하고, 도메인 간 연관관계를 객체 그래프로 표현하며, 영속성 컨텍스트의 동작을 직접 관찰합니다.

이 미션에서 분량이 가장 큰 단계입니다. 매핑·연관관계·영속성 컨텍스트가 한꺼번에 등장하니 페이스를 잡을 때 가장 무거운 단계로 의식하면 좋습니다.

> **들어가기 전 자기 진단**

Q. 본인 코드의 Repository에서 가장 자주 등장하는 SQL 패턴은 무엇인가요?

Theme, ReservationTime, ReservationDate처럼 독립적인 도메인은 insert, select by id, find all, delete, exists 같은 단순 SQL이 자주 등장했습니다. 반면 Reservation과 WaitingReservation은 reservation\_date, reservation\_time, theme 테이블을 JOIN해서 한 번에 조회한 뒤, RowMapper에서 ReservationDate, ReservationTime, Theme 객체로 직접 조립하는 패턴이 반복됩니다.

Q. 객체 참조로 옮겼을 때 더 자연스러워지는 곳은 어디인가요?

객체 참조로 옮겼을 때 가장 자연스러워지는 곳은 Reservation이 ReservationDate, ReservationTime, Theme를 참조하는 부분입니다. 기존 DB 테이블은 date\_id, time\_id, theme\_id 외래 키만 가지고 있지만, Java 도메인 객체는 이미 날짜, 시간, 테마를 객체로 들고 있어 연관관계 매핑하면 reservation.getTime().getStartAt(), reservation.getTheme().getName()처럼 객체 그래프로 접근하는 방식이 더 자연스러워집니다.
* * *

## 1-1. 매핑 변환

다른 클래스에 의존하지 않는 클래스부터 시작합니다 — `Theme`, `ReservationTime` 등.

### 요구사항
-   `build.gradle`: `spring-boot-starter-jdbc` → `spring-boot-starter-data-jpa` 대체
-   `@Entity`, `@Id`, `@GeneratedValue(strategy = IDENTITY)` 부여
-   `JpaRepository<T, Long>` 인터페이스 작성, 기존 JdbcTemplate 기반 Repository 제거
-   `KeyHolder`, `SimpleJdbcInsert` 같은 JdbcTemplate 잔재 제거
-   `application.properties` 권장 설정:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.ddl-auto=create-drop
spring.jpa.defer-datasource-initialization=true
```

> **양쪽 시도 시 비교 관찰 포인트**: ① 시작 시 발행되는 DDL의 차이 ② 재시작 시 데이터 보존 여부 ③ 컬럼명·타입을 entity로만 제어할 수 있는지. 이 셋이 안 보이면 그냥 양쪽 다 돌려봤을 뿐 차이는 못 봤다는 신호입니다.

### 확인 과제

Q. 예약 생성 시 콘솔에 찍히는 INSERT SQL이 방탈출 미션과 어떻게 같고 어떻게 다른가요?

\`\`\` Hibernate: insert into reservation (date\_id, name, theme\_id, time\_id, id) \`\`\` 같은 점은 \`reservation\` 테이블에 예약자 이름, 날짜 id, 시간 id, 테마 id를 저장한다는 점입니다. 둘 다 DB에는 외래 키 값이 들어갑니다. 다른 점은 컬럼 순서가 기존 SQL의 \`name, date\_id, time\_id, theme\_id\`와 다르게 \`date\_id, name, theme\_id, time\_id, id\`처럼 찍혔고, \`id\`는 \`IDENTITY\` 전략에 따라 DB가 생성하도록 \`default\`로 처리된 것 입니다. 또 하나의 차이는 쓰기 지연입니다. JDBC에서는 예약 삭제 후 같은 슬롯의 대기를 승격할 때 DELETE가 즉시 실행되어 기존 슬롯이 바로 비었습니다. JPA에서는 DELETE와 INSERT가 영속성 컨텍스트에 모였다가 flush 시점에 동기화되면서, Hibernate 내부 ActionQueue에 따라서 INSERT가 먼저 처리될 수 있고, 실제로 발생하여 UNIQUE 제약에 걸렸습니다. 그래서 예약 취소/변경 후 대기 승격 전에 \`reservationRepository.flush()\`를 호출해 기존 슬롯을 먼저 DB에 반영하도록 조정했습니다.
* * *

## 1-2. 연관관계 매핑

다른 클래스에 의존하는 클래스에 연관관계를 매핑합니다. 예: `Reservation`은 `Member`, `Theme`, `ReservationTime`을 참조합니다.

### 요구사항
-   `@ManyToOne` + `@JoinColumn(name = "..._id")`로 객체 참조
-   **단방향으로 시작**합니다. 양방향이 필요한 이유가 생기면 그때 추가합니다.
-   양방향 시도 시 **연관관계 주인** 명시, 무한 직렬화 가능성 검토.
-   `cascade`, `orphanRemoval`은 **필요해질 때까지 적용하지 않습니다**. 적용한다면 PR 본문에 그 근거를 적습니다.

> 양방향 또는 cascade를 한 번 시도했다가 단방향/제거로 후퇴하는 사이클을 의식적으로 한 번 굴려봅니다. 시도와 후퇴를 기록에 남기는 것이 차원 B(설계 판단)의 도달점입니다.

### 확인 과제

Q. \`findById(reservationId).getTime().getStartAt()\`이 발행하는 SQL을 적어주세요.

Hibernate: select r1\_0.id,r1\_0.date\_id,r1\_0.name,r1\_0.theme\_id,r1\_0.time\_id from reservation r1\_0 where r1\_0.id=? Hibernate: select rt1\_0.id,rt1\_0.start\_at from reservation\_time rt1\_0 where rt1\_0.id=?
* * *

## 1-3. 영속성 컨텍스트 관찰

코드를 추가하기보다 **관찰**합니다. JPA가 자동으로 무엇을 하는지 직접 봅니다.

**이 미션의 본질 6개 중 영속성 컨텍스트가 가장 깊이 박히는 자리입니다.**

| 관찰 대상 | 어떤 코드로 확인하는가 | 무엇을 본다 |
| --- | --- | --- |
| **dirty checking** | `@Transactional` 메서드에서 entity 필드 수정 후 save 미호출 | commit 시점에 UPDATE 자동 발행 |
| **1차 캐시** | 같은 트랜잭션에서 `findById` 두 번 호출 | 두 번째 SELECT 생략 (1차 캐시 적중) |
| **쓰기 지연** | `save` 호출 후 `flush` 전·후의 DB 상태 비교 | INSERT가 commit/flush 시점에 일괄 발행 |
| **flush 시점** | 명시적 `flush` 호출 / 트랜잭션 종료 / JPQL 실행 직전 | 영속성 컨텍스트 → DB 동기화 트리거 |
| **fetch 기본값** | `@ManyToOne` vs `@OneToMany` 무명시 시 | EAGER vs LAZY 차이 |
| `LazyInitializationException` | 트랜잭션 밖에서 LAZY 필드 접근 | 영속성 컨텍스트 닫힌 후 프록시 미초기화 |

### 관찰 과제 1: 영속성 컨텍스트 신호 캡처

위 6개를 직접 만들어 기록에 남길수록 영속성 컨텍스트가 손에 잡힙니다. 관찰을 적을 때 다음 4가지가 함께 있으면 미션 끝난 후 가장 강한 회상 재료가 됩니다.

```
1. 시도한 코드
2. 예측한 SQL/동작
3. 실제 SQL/동작
4. 왜 다른가
```

예측과 실제 사이의 갭이 보이는 순간이 본 미션의 핵심 학습 신호입니다.

> 관찰 과제 2(N+1과 fetch join 비교)는 3단계에서 본격적으로 등장합니다. 1단계에서는 LazyInit을 만나는 것으로도 영속성 컨텍스트의 경계가 보입니다.
