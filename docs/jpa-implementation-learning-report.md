# JPA 전환 구현·학습 리포트

## 0. 이 문서의 목적

이 문서는 우아한테크코스 백엔드 미션 **방탈출 예약 대기** 이후 추가 미션인 **JPA 도입**을 기준으로, 현재 프로젝트에서 무엇이 바뀌었고 왜 바뀌었는지 학습용으로 정리한 기록이다.

읽는 순서는 다음을 권장한다.

1. [단계별 요구사항 반영 현황](#1-단계별-요구사항-반영-현황)
2. [JDBC에서 JPA로 바뀐 핵심](#2-jdbc에서-jpa로-바뀐-핵심)
3. [예약 대기 도메인과 JPQL](#4-예약-대기-도메인과-jpql)
4. [헥사고날 아키텍처 때문에 복잡해진 지점](#7-헥사고날-아키텍처-때문에-복잡해진-지점)
5. [남은 질문 리스트](#9-결정이-필요하거나-추가로-확인하면-좋은-질문)

---

## 1. 단계별 요구사항 반영 현황

| 단계 | 요구사항 | 현재 반영 | 핵심 파일 |
| --- | --- | --- | --- |
| 0단계 | 기존 기능 유지, JPA 전환 범위 정의 | `./gradlew test`로 기존 기능 회귀 검증 | `src/test/java/roomescape/**/*Test.java` |
| 1단계 | `JdbcTemplate` 저장소를 JPA 저장소로 교체 | `spring-boot-starter-data-jpa`, `@Entity`, `JpaRepository` 기반으로 전환 | `build.gradle`, `src/main/java/roomescape/*/domain`, `src/main/java/roomescape/*/adapter/out/persistence` |
| 2단계 | 내 예약 목록 조회 | 기존 경로 `/api/user/reservations/me` + 미션 원문 호환 경로 `/reservations-mine` 제공 | `UserReservationController`, `MissionReservationController`, `ReservationService` |
| 3단계 | 대기 생성/취소, 내 예약 목록에 대기 포함, 중복 방지, N번째 대기 표시 | 구현 완료. 기존 경로 `/api/user/waitings` + 미션 원문 호환 경로 `/waitings` 제공 | `WaitingController`, `WaitingService`, `WaitingLine`, `WaitingLines` |
| 4단계 | 어드민 대기 관리 + 예약 취소 시 대기 자동 승인 | 관리자 대기 목록/취소 추가, 수동 승인은 선택하지 않고 자동 승격 방식 선택 | `WaitingController`, `ReservationService`, `WaitingPromotionPolicy` |

### 이번 보완에서 추가한 것

- `GET /reservations-mine`
  - 미션 문서의 원문 API 경로와 현재 프로젝트의 `/api/user/reservations/me`를 모두 지원한다.
- `POST /waitings`, `DELETE /waitings/{id}`
  - 미션 문서의 원문 API 경로와 현재 프로젝트의 `/api/user/waitings`를 모두 지원한다.
- `GET /api/manager/waitings`
  - 관리자가 전체 대기 목록과 각 대기의 현재 순번을 조회한다.
- `DELETE /api/manager/waitings/{id}`
  - 관리자가 사용자 소유자 검증 없이 대기를 취소한다.
- 추가 테스트
  - 원문 경로 호환성
  - 관리자 대기 목록/취소
  - 대기 취소 후 목록 제거와 남은 순번 재계산
  - 예약 + 대기 혼합 내 목록 조회

---

## 2. JDBC에서 JPA로 바뀐 핵심

### 2.1 의존성 변경

기존 SQL Mapper 방식에서는 보통 `spring-boot-starter-jdbc`와 `JdbcTemplate`이 중심이다. 현재 프로젝트는 JPA 미션을 위해 다음처럼 JPA 의존성을 사용한다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
runtimeOnly 'com.h2database:h2'
```

JPA 설정은 다음과 같다.

```yaml
spring:
  jpa:
    show-sql: true
    defer-datasource-initialization: true
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        format_sql: true
```

### 왜 바뀌었나?

| JDBC/JdbcTemplate | JPA |
| --- | --- |
| SQL을 직접 작성한다. | 객체 매핑을 바탕으로 SQL을 Hibernate가 만든다. |
| `RowMapper`, `KeyHolder`, `SimpleJdbcInsert` 같은 코드가 필요하다. | `@Entity`, `@Id`, `@ManyToOne`, `JpaRepository`가 저장/조회 기본 동작을 맡는다. |
| join 결과를 DTO나 도메인으로 직접 조립한다. | 연관관계를 객체 참조로 표현하고 필요한 경우 JPQL로 조회 모양을 제어한다. |

### 트레이드오프

- 좋아진 점
  - 기본 CRUD 코드가 줄었다.
  - `Reservation -> Slot -> Theme/ReservationTime`처럼 객체 그래프로 도메인 의미를 표현할 수 있다.
  - 트랜잭션 안에서 dirty checking, 1차 캐시, 쓰기 지연 같은 기능을 활용할 수 있다.
- 감수할 점
  - 실제 SQL이 코드에 직접 보이지 않는다.
  - 연관관계 fetch 전략을 모르면 N+1, LazyInitializationException을 만나기 쉽다.
  - `save()` 호출 시점과 실제 `INSERT/UPDATE` 발행 시점이 다를 수 있다.

---

## 3. 엔티티 매핑과 연관관계

### 3.1 독립 엔티티

- `Member`
- `Theme`
- `ReservationTime`

이들은 다른 엔티티를 참조하지 않는 비교적 단순한 테이블이다.

예: `ReservationTime`

```java
@Entity
@Table(name = "reservation_time")
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalTime startAt;
}
```

`IDENTITY` 전략을 사용하면 DB가 PK를 생성한다. 따라서 저장 시 Hibernate는 즉시 insert를 보내 PK를 알아와야 하는 경우가 많다.

대표 SQL 형태:

```sql
insert into reservation_time (start_at, id) values (?, default)
```

### 3.2 연관 엔티티

현재 구조는 `Reservation`이 `Member`, `Slot`을 참조하고, `Slot`이 `Theme`, `ReservationTime`을 참조한다.

```text
Reservation
 ├─ Member
 └─ Slot
     ├─ Theme
     └─ ReservationTime
```

`Waiting`도 같은 방식으로 `Member`, `Slot`을 참조한다.

```text
Waiting
 ├─ Member
 └─ Slot
     ├─ Theme
     └─ ReservationTime
```

### 왜 `Reservation`이 `Theme`, `ReservationTime`을 직접 참조하지 않나?

현재 프로젝트에는 `Slot`이라는 도메인이 있다.

```text
Slot = 특정 날짜 + 특정 시간 + 특정 테마
```

따라서 예약은 “시간과 테마 각각”을 예약하는 것이 아니라 “특정 슬롯”을 예약한다. 이 설계를 유지하면 같은 날짜·시간·테마 조합의 유일성을 `slot` 테이블에서 관리할 수 있다.

```java
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "uk_slot_date_time_theme",
        columnNames = {"date", "time_id", "theme_id"}
    )
)
public class Slot { ... }
```

### 단방향을 선택한 이유

현재 연관관계는 대부분 단방향이다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "slot_id", nullable = false)
private Slot slot;
```

양방향을 만들면 `slot.getReservations()`처럼 탐색은 편해질 수 있다. 하지만 다음 비용이 생긴다.

- 연관관계 주인을 정해야 한다.
- 양쪽 컬렉션 동기화 메서드가 필요하다.
- JSON 직렬화 시 순환 참조 위험이 생긴다.
- 현재 요구사항에서는 `Slot -> Reservation 목록` 객체 탐색이 꼭 필요하지 않다.

따라서 이번 미션에서는 **필요할 때 추가한다**는 원칙으로 단방향을 유지했다.

---

## 4. 예약 대기 도메인과 JPQL

## 4.1 `Waiting`을 별도 엔티티로 둔 이유

예약 대기를 `Reservation.status = WAITING`처럼 같은 테이블에 둘 수도 있다. 하지만 현재 프로젝트는 `Waiting`을 별도 엔티티로 분리했다.

### 선택지 비교

| 선택지 | 장점 | 단점 |
| --- | --- | --- |
| `Reservation`에 status 컬럼 추가 | 테이블 하나로 예약/대기를 함께 조회하기 쉽다. | 예약과 대기가 같은 생명주기를 가진 것처럼 보인다. 대기 순번, 승격, 취소 규칙이 `Reservation`에 섞인다. |
| `Waiting` 별도 엔티티 | 예약과 대기의 생명주기를 분리한다. 대기열 도메인(`WaitingLine`)을 만들기 쉽다. | 내 예약 목록처럼 예약+대기를 함께 보여줄 때 병합 로직이 필요하다. |

현재 요구사항은 “대기 신청”, “대기 취소”, “N번째 대기”, “예약 취소 시 첫 대기 자동 승격”처럼 대기 자체의 규칙이 많다. 그래서 별도 엔티티가 더 자연스럽다.

## 4.2 중복 방지

중복 방지는 애플리케이션 검증과 DB 제약을 함께 둔다.

```java
@Table(uniqueConstraints = @UniqueConstraint(
    name = "uk_waiting_member_slot",
    columnNames = {"member_id", "slot_id"}
))
public class Waiting { ... }
```

서비스에서도 먼저 검사한다.

```java
if (waitingRepository.existsBySlotIdAndMemberId(memberId, slotId)) {
    throw new EscapeRoomException(ErrorCode.WAITING_ALREADY_EXIST);
}
```

### 왜 둘 다 필요한가?

- 서비스 검증: 사용자에게 명확한 409 응답을 주기 쉽다.
- DB unique constraint: 동시에 두 요청이 들어와 서비스 검증을 둘 다 통과해도 최종 데이터 중복을 막는 마지막 방어선이다.

단, 현재 코드는 동시 중복 대기 생성에서 DB 예외를 사용자 친화적 409로 변환하는 검증까지는 깊게 다루지 않았다. 이 부분은 남은 질문에 포함한다.

## 4.3 N번째 대기 계산

대기 순번은 컬럼으로 저장하지 않는다. 조회 시점에 같은 슬롯의 대기들을 id 순서로 정렬해 계산한다.

```java
public class WaitingLine {
    private final List<Waiting> waitings;

    public long orderOf(Long waitingId) {
        for (int index = 0; index < waitings.size(); index++) {
            if (waitings.get(index).getId().equals(waitingId)) {
                return index + 1L;
            }
        }
        throw new IllegalArgumentException("대기열에 존재하지 않는 대기입니다.");
    }
}
```

### 왜 순번을 저장하지 않나?

| 방식 | 장점 | 단점 |
| --- | --- | --- |
| `waiting_order` 컬럼 저장 | 조회가 단순하다. | 앞 사람이 취소/승격될 때 뒤 사람 순번을 모두 업데이트해야 한다. 동시성 처리가 어려워진다. |
| 조회 시 계산 | 취소/승격 때 순번 업데이트가 필요 없다. | 조회 시 같은 슬롯의 대기열을 추가로 읽어야 한다. |

현재 요구사항에서는 대기열 크기가 작다고 가정할 수 있고, 순번 변경이 자주 생길 수 있으므로 조회 시 계산을 선택했다.

---

## 5. 내 예약 목록 조회와 N+1 회피

내 예약 목록은 예약과 대기를 함께 내려준다.

```java
public List<ReservationDetailFindResponse> findMyReservations(long memberId) {
    List<ReservationDetailFindResponse> reservations = findMyReservationResponses(memberId);
    List<ReservationDetailFindResponse> waitings = findMyWaitingResponses(memberId);

    return Stream.concat(reservations.stream(), waitings.stream())
            .toList();
}
```

### 5.1 JPQL 생성자 프로젝션 사용

예약 상세 조회는 엔티티를 가져와 DTO 변환 중 lazy 필드를 하나씩 건드리는 방식이 아니라, 필요한 필드를 JPQL에서 바로 projection으로 조회한다.

```java
@Query("""
    SELECT new roomescape.reservation.application.port.out.projection.ReservationDetailProjection(
        r.id,
        r.member.id,
        r.member.name,
        r.slot.date,
        r.slot.theme.id,
        r.slot.theme.name,
        r.slot.theme.description,
        r.slot.theme.thumbnailUrl,
        r.slot.time.id,
        r.slot.time.startAt
    )
    FROM Reservation r
    WHERE r.member.id = :memberId
    ORDER BY r.id
    """)
List<ReservationDetailProjection> findAllReservationDetailsByMemberId(@Param("memberId") long memberId);
```

대표 SQL 형태는 다음과 같다.

```sql
select
    r.id,
    m.id,
    m.name,
    s.date,
    t.id,
    t.name,
    t.description,
    t.thumbnail_url,
    rt.id,
    rt.start_at
from reservation r
join member m on m.id = r.member_id
join slot s on s.id = r.slot_id
join theme t on t.id = s.theme_id
join reservation_time rt on rt.id = s.time_id
where m.id = ?
order by r.id
```

### 5.2 fetch join 대신 projection을 쓴 이유

N+1을 피하는 대표 방식은 fetch join 또는 `@EntityGraph`다. 현재 프로젝트에서는 상세 응답 조회에 projection을 사용했다.

| 방식 | 장점 | 단점 |
| --- | --- | --- |
| 엔티티 조회 + LAZY 접근 | 도메인 객체를 그대로 다룬다. | DTO 변환 중 N+1이 발생하기 쉽다. |
| fetch join | 엔티티 그래프를 한 번에 초기화한다. | 조회 목적이 DTO라면 필요 이상의 엔티티를 영속성 컨텍스트에 올릴 수 있다. |
| 생성자 projection | 응답에 필요한 필드만 조회한다. N+1을 피하기 쉽다. | JPQL이 DTO/Projection 구조를 알게 된다. 도메인 객체 변경 추적에는 적합하지 않다. |

현재 내 예약 목록은 “화면 조회”에 가깝다. 수정할 엔티티가 필요한 유스케이스가 아니므로 projection이 적합하다.

### 5.3 대기 순번 계산에서 추가 조회가 필요한 이유

`WaitingDetailProjection`은 내 대기 자체의 상세 정보만 가진다. 하지만 “내 대기가 몇 번째인지”는 같은 슬롯의 다른 대기까지 알아야 계산된다.

그래서 다음 흐름을 사용한다.

1. 내 대기 상세 목록 조회
2. 그 대기들이 속한 slot id를 모음
3. 해당 slot들의 모든 대기 목록을 한 번에 조회
4. `WaitingLines`가 slot별 대기열을 만들고 순번 계산

이 방식은 각 대기마다 `findAllBySlotId`를 반복하지 않기 때문에 N+1을 줄인다.

---

## 6. 예약 취소와 대기 자동 승격

4단계에서는 수동 승인과 자동 승인 중 **자동 승인**을 선택했다.

```java
@Transactional
public void deleteById(long reservationId) {
    findReservationIfExists(reservationId)
            .ifPresent(this::cancelReservation);
}
```

예약 취소의 핵심 흐름:

```java
private void cancelReservation(Reservation reservation) {
    reservation.validateCancelable(LocalDateTime.now(clock));

    WaitingLine waitingLine = findWaitingLineFor(reservation);
    deleteReservationOnly(reservation);
    promoteFirstWaitingIfExists(reservation, waitingLine);
}
```

승격 흐름:

```java
private void promoteFirstWaitingIfExists(Reservation canceledReservation, WaitingLine waitingLine) {
    waitingLine.first()
            .ifPresent(waiting -> {
                Reservation promotedReservation = waitingPromotionPolicy.promote(waiting,
                        canceledReservation.getSlot());
                reservationRepository.save(promotedReservation);
                waitingRepository.deleteById(waiting.getId());
            });
}
```

### 왜 한 트랜잭션으로 묶었나?

예약 취소와 대기 승격은 함께 성공하거나 함께 실패해야 한다.

- 예약만 삭제되고 승격이 실패하면 빈 슬롯이 된다.
- 승격 예약은 생겼는데 대기가 삭제되지 않으면 같은 사용자가 대기와 예약을 동시에 가진 것처럼 보일 수 있다.

따라서 `@Transactional` 하나 안에서 처리한다.

### 왜 비관적 락을 사용했나?

예약 취소와 대기 취소가 동시에 일어날 수 있다.

```text
A: 기존 예약 취소 → 첫 번째 대기 승격 대상 선택
B: 같은 첫 번째 대기 사용자가 대기 취소
```

이때 같은 waiting row를 동시에 처리하면 사용자는 “대기를 취소했다”고 생각하는데 시스템에는 예약이 생기는 문제가 생길 수 있다.

그래서 명령 흐름에서는 `FOR UPDATE` 조회를 사용한다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT w FROM Waiting w WHERE w.slot.id = :slotId ORDER BY w.id")
List<Waiting> findAllBySlotIdOrderByIdForUpdate(@Param("slotId") long slotId);
```

### 트레이드오프

- 좋아진 점
  - 예약 취소 승격과 대기 취소가 같은 waiting row 기준으로 직렬화된다.
  - 동시성 테스트로 “예약 취소가 먼저인 경우”, “대기 취소가 먼저인 경우”를 검증한다.
- 감수한 점
  - 락 때문에 같은 row를 다루는 요청은 기다린다.
  - H2의 `FOR UPDATE`와 운영 DB의 락 동작이 완전히 같다고 보장할 수 없다.
  - 모든 동시성 문제를 해결한 것은 아니다. 예를 들어 같은 슬롯 예약 생성 동시 요청, 같은 사용자 대기 생성 동시 요청은 DB unique constraint까지 포함해 추가 검증 여지가 있다.

---

## 7. 헥사고날 아키텍처 때문에 복잡해진 지점

현재 프로젝트는 헥사고날 아키텍처 형태를 갖는다.

```text
adapter/in/web -> application/port/in -> application/service -> application/port/out -> adapter/out/persistence -> JPA
```

예를 들어 대기 목록 조회는 다음 경로를 지난다.

```text
WaitingController
 -> FindWaitingUseCase
 -> WaitingService
 -> WaitingRepository(port)
 -> JpaWaitingRepository(adapter)
 -> SpringDataWaitingRepository(JpaRepository)
```

### 복잡해진 부분

#### 1. Repository가 두 겹이다

```text
WaitingRepository            // application port
JpaWaitingRepository         // port 구현체
SpringDataWaitingRepository  // Spring Data JPA 인터페이스
```

레이어드 아키텍처였다면 서비스가 바로 `SpringDataWaitingRepository`를 주입받았을 가능성이 크다.

```text
Controller -> Service -> SpringDataWaitingRepository
```

헥사고날에서는 JPA가 외부 어댑터다. 그래서 application layer는 `JpaRepository`를 직접 알지 않고 port만 안다.

장점:

- 서비스가 Spring Data JPA 세부 구현에 덜 묶인다.
- 테스트에서 port를 mock하기 쉽다.
- 저장소 구현을 바꾸더라도 application service의 의존 방향이 유지된다.

단점:

- 단순 CRUD도 파일이 늘어난다.
- Spring Data 쿼리 메서드 하나를 추가해도 port, adapter, SpringData interface를 함께 수정해야 한다.
- projection 위치를 어디에 둘지 고민이 생긴다.

#### 2. Projection이 application port에 위치한다

현재 projection은 다음 위치에 있다.

```text
reservation/application/port/out/projection/ReservationDetailProjection.java
waiting/application/port/out/projection/WaitingDetailProjection.java
```

이 선택은 “애플리케이션이 필요한 조회 결과 형태”를 port 계약으로 둔 것이다.

레이어드였다면 repository 패키지 안에 projection interface/record를 두고 service가 바로 사용했을 가능성이 크다.

트레이드오프:

- 장점: service가 필요한 데이터 모양이 명확하다.
- 단점: JPQL 생성자 표현식이 application projection의 패키지명을 직접 참조한다.

#### 3. 관리자 대기 기능 추가도 포트를 먼저 바꿔야 한다

이번 보완에서 `GET /api/manager/waitings`를 추가하기 위해 다음이 함께 바뀌었다.

- `FindWaitingUseCase` 추가
- `WaitingService`가 `FindWaitingUseCase` 구현
- `WaitingRepository.findAllWaitingDetails()` 추가
- `JpaWaitingRepository` 위임 추가
- `SpringDataWaitingRepository` JPQL 추가
- `WaitingDetailFindResponse` 추가
- API 통합 테스트 추가

레이어드라면 controller/service/repository 3곳 정도로 끝났을 수 있다. 헥사고날은 더 길지만, 각 변경의 방향과 책임이 명확하다.

---

## 8. 테스트와 검증 기록

### 실행한 검증

```bash
./gradlew test --tests 'roomescape.waiting.WaitingServiceTest' \
  --tests 'roomescape.waiting.WaitingApiIntegrationTest' \
  --tests 'roomescape.reservation.ReservationApiIntegrationTest'
```

결과:

```text
BUILD SUCCESSFUL
```

전체 테스트도 기준선에서 통과를 확인했다.

```bash
./gradlew test
```

결과:

```text
BUILD SUCCESSFUL
```

### 새로 고정한 동작

- `/reservations-mine`으로 내 예약 목록 조회 가능
- `/waitings`로 대기 생성 가능
- 대기 취소 후 취소한 사용자의 목록에서 사라짐
- 첫 번째 대기 취소 후 다음 대기자의 `waitingOrder`가 1로 재계산됨
- 관리자는 `/api/manager/waitings`로 대기 목록을 조회할 수 있음
- 관리자는 `/api/manager/waitings/{id}`로 대기를 취소할 수 있음
- 한 사용자의 내 예약 목록에 `RESERVED`와 `WAITING`이 함께 내려올 수 있음

---

## 9. 결정이 필요하거나 추가로 확인하면 좋은 질문

현재 구현을 더 엄격하게 만들려면 다음 결정이 필요하다.

1. **API 경로를 최종적으로 하나로 통일할 것인가?**
   - 현재는 기존 프로젝트 경로(`/api/user/reservations/me`, `/api/user/waitings`)와 미션 원문 경로(`/reservations-mine`, `/waitings`)를 모두 지원한다.
   - 장점: 기존 클라이언트와 미션 요구사항을 모두 만족한다.
   - 단점: 같은 기능의 입구가 두 개라 API 문서 관리 비용이 생긴다.

2. **대기 상태 응답을 `status="WAITING" + waitingOrder=1`로 유지할 것인가, `status="1번째 예약대기"` 문자열로 바꿀 것인가?**
   - 현재는 enum 상태와 순번 숫자를 분리했다.
   - 장점: 프론트엔드가 다국어/표현을 자유롭게 만들 수 있다.
   - 단점: 미션 문서의 예시 문자열과는 다르다.

3. **관리자 수동 승인을 추가할 것인가?**
   - 현재 4단계 승인은 “예약 취소 시 자동 승격”을 선택했다.
   - 수동 승인까지 추가하면 관리자 UX는 좋아지지만, 자동 승격 정책과 충돌할 수 있어 정책 정의가 필요하다.

4. **동시 중복 대기 생성 시 DB unique 예외를 명시적 409로 변환할 것인가?**
   - 서비스 검증은 있지만 race condition에서는 DB 제약이 마지막 방어선이다.
   - 운영 수준으로 가려면 `DataIntegrityViolationException`을 도메인 예외로 변환하는 테스트가 있으면 좋다.

5. **조회 정렬 기준을 확정할 것인가?**
   - 현재 내 예약 목록은 예약 목록 뒤에 대기 목록을 붙인다.
   - 사용자 관점에서는 날짜/시간순 정렬이 더 자연스러울 수 있다.

6. **운영 DB 기준으로 `FOR UPDATE` 락 범위를 재검증할 것인가?**
   - 현재 테스트는 H2 기반이다.
   - MySQL/PostgreSQL로 바뀌면 인덱스, 격리 수준, `ORDER BY ... FOR UPDATE`의 실제 락 범위를 확인해야 한다.

---

## 10. 핵심 회고 요약

이번 JPA 전환에서 가장 중요한 학습 포인트는 다음이다.

1. JPA는 SQL 작성을 줄여주지만 SQL 이해를 없애주지 않는다.
2. 연관관계를 객체 참조로 표현하면 도메인 코드는 자연스러워지지만 fetch 전략과 트랜잭션 경계를 알아야 한다.
3. `@ManyToOne(fetch = LAZY)`를 명시하면 불필요한 즉시 로딩을 줄일 수 있지만, 트랜잭션 밖 접근은 조심해야 한다.
4. 화면 조회는 entity graph보다 projection이 단순하고 효율적일 수 있다.
5. 대기 순번처럼 계속 바뀌는 값은 저장보다 계산이 더 안전할 수 있다.
6. 자동 승격처럼 여러 엔티티를 함께 바꾸는 기능은 트랜잭션 경계와 동시성 정책이 핵심이다.
7. 헥사고날 아키텍처는 파일 수를 늘리지만, JPA를 외부 어댑터로 격리하고 application service의 의존 방향을 지켜준다.
