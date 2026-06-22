# JPA 선택 미션 학습 로그

## 0단계 시작 상태

- 시작 브랜치: `miniminjae92`
- 작업 브랜치: `jpa-migration`
- 동작 확인: `./gradlew test` 통과

이번 선택 미션은 기존 방탈출 미션 코드를 처음부터 다시 구현하지 않고, JdbcTemplate 기반 저장소 코드를 JPA로 전환하는 것이 목적이다. 따라서 Controller API, Service의 비즈니스 규칙, 예외 처리 흐름, 관리자 토큰 기반 접근 제어, 현재 패키지 구조는 최대한 유지한다.

전환 대상은 JdbcTemplate 기반 Repository 구현체, 직접 작성한 SQL, parameter binding, RowMapper이다. 단순 CRUD는 Spring Data JPA Repository로 전환하고, 예약 가능 여부 조회, 인기 테마 조회, 예약대기 순번 조회처럼 조건이 복잡한 쿼리는 기존 SQL의 의미를 보존하면서 JPQL이나 별도 조회 전략을 검토한다.

## 사전 학습 정리

### JDBC, JdbcTemplate, JPA의 역할

JDBC는 Java 애플리케이션이 DBMS와 통신하기 위한 표준 API이다. Java 애플리케이션은 JDBC API를 사용하고, JDBC Driver가 SQL과 파라미터를 DBMS 프로토콜에 맞는 바이트 흐름으로 변환해 DBMS와 통신한다.

JdbcTemplate은 JDBC의 반복 작업을 줄여준다. Connection 관리, Statement 실행, 예외 변환 같은 부분은 도와주지만, SQL 작성, 파라미터 바인딩, ResultSet을 도메인 객체로 바꾸는 RowMapper는 개발자가 직접 작성해야 한다.

JPA는 객체와 관계형 데이터베이스의 매핑 규칙을 선언하면, 그 규칙을 바탕으로 SQL 생성, 바인딩, 결과 매핑을 자동화한다. 대신 실제 SQL이 언제 어떤 형태로 실행되는지 숨겨질 수 있으므로, 실행 SQL과 트랜잭션 경계를 의식해야 한다.

### 핵심 키워드

- ORM: 객체와 관계형 데이터베이스 테이블을 매핑하는 기술
- 객체-관계 임피던스 불일치: 객체는 참조와 행위를 중심으로 표현하고, RDB는 테이블과 외래 키를 중심으로 표현해서 생기는 구조적 차이
- 영속성 컨텍스트: 엔티티를 저장하고 관리하는 논리적인 공간
- 1차 캐시: 같은 트랜잭션 안에서 이미 조회한 엔티티를 메모리에서 다시 사용할 수 있게 하는 저장소
- Dirty Checking: 트랜잭션 종료 시점에 엔티티 변경사항을 감지해 UPDATE SQL을 생성하는 기능
- 쓰기 지연: INSERT, UPDATE, DELETE SQL을 즉시 실행하지 않고 모아두었다가 flush 시점에 DB와 동기화하는 방식
- Flush: 영속성 컨텍스트의 변경 내용을 DB에 반영하는 동기화 작업
- 연관관계의 주인: 양방향 관계에서 실제 외래 키 변경 권한을 가지는 쪽
- LAZY: 연관 객체를 실제 사용할 때 조회하는 지연 로딩 전략
- Fetch Join: 연관 객체를 한 번의 JPQL 조회에서 함께 가져오기 위한 쿼리 전략

## 사전 질문 답변

### JPA는 무엇을 자동화하고 무엇을 감추는가?

JPA는 반복적인 CRUD SQL 작성, 파라미터 바인딩, 조회 결과를 객체로 변환하는 매핑 작업을 자동화한다. 대신 실제 SQL의 형태와 실행 시점이 코드에서 바로 보이지 않는다. 이 때문에 N+1 문제, 의도하지 않은 JOIN, 예상보다 늦거나 빠른 flush를 추적할 수 있어야 한다.

### 영속성 컨텍스트는 언제 의식해야 하는가?

트랜잭션 경계를 의식해야 한다. 영속성 컨텍스트는 보통 트랜잭션과 함께 동작하므로, 엔티티를 조회하고 변경하는 시점, flush가 발생하는 시점, 트랜잭션 밖에서 LAZY 연관 객체에 접근하는 시점을 주의해야 한다.

### SQL JOIN을 객체 그래프로 옮기면 부담은 어디로 가는가?

기존에는 SQL JOIN과 RowMapper가 연관 데이터를 한 번에 조회하고 객체로 조립했다. JPA에서는 `reservation.getTime().getStartAt()`처럼 객체 그래프로 접근할 수 있지만, 연관관계 매핑과 fetch 전략을 잘못 잡으면 DB 조회 횟수와 애플리케이션 메모리 사용량이 늘어날 수 있다.

### 어노테이션 한 줄이 만드는 실제 SQL을 추적할 수 있는가?

추적할 수 있어야 한다. `spring.jpa.show-sql`, SQL logging, parameter binding logging 등을 사용해 JPA가 생성한 SQL과 바인딩 값을 확인할 계획이다. JPA 전환 후에도 기존 JdbcTemplate SQL과 의미가 같은지 비교할 기준으로 사용한다.

## 기존 코드 회고

### 현재 구조

현재 코드는 예약, 예약날짜, 예약시간, 테마, 예약대기 기능을 중심으로 Controller, Service, Repository 계층이 분리되어 있다. 일반 사용자 인증/인가는 없고, 예약과 예약대기 조회는 `name` 요청 값을 기준으로 처리한다. 관리자 기능은 `/admin/**` 경로에 대해 `X-ADMIN-TOKEN` 기반 인터셉터로 접근을 제한한다.

주요 도메인 객체는 `Reservation`, `ReservationDate`, `ReservationTime`, `Theme`, `WaitingReservation`이다. `ReservationSlot`과 `ReservationSlotResolver`는 날짜, 시간, 테마 조합의 예약 가능 여부를 판단하는 역할을 한다.

### Repository 코드에서 확인한 점

기존 Repository는 INSERT, SELECT, UPDATE, DELETE SQL을 모두 직접 다룬다. 특히 조회 쿼리에서는 예약, 날짜, 시간, 테마 테이블을 JOIN해서 하나의 ResultSet으로 가져온 뒤 RowMapper에서 도메인 객체를 조립한다.

예약대기 쪽은 더 복잡하다. 같은 슬롯에서 가장 오래된 대기를 찾거나, 사용자 이름으로 예약대기 목록과 순번을 조회할 때 SQL의 정렬, JOIN, 윈도우 함수에 많이 의존한다. 이 부분은 JPA 전환 시 단순 CRUD처럼 기계적으로 바꾸기 어렵고, JPQL이나 별도 조회 전략을 신중히 선택해야 한다.

테이블과 도메인 객체도 완전히 1:1은 아니다. DB의 `reservation` 테이블은 `date_id`, `time_id`, `theme_id` 외래 키를 가지지만, Java의 `Reservation` 객체는 `ReservationDate`, `ReservationTime`, `Theme` 객체를 직접 참조한다. 이 차이가 JPA 연관관계 매핑에서 가장 먼저 다뤄야 할 지점이다.

## 기존 정책 로그

### 예약 대기

- 이미 예약된 슬롯에만 예약대기를 신청할 수 있다.
- 같은 사용자는 같은 날짜, 시간, 테마 조합에 중복 대기할 수 없다.
- 예약대기는 이름, 날짜, 시간, 테마, 생성시간을 가진다.
- 예약대기 순번은 컬럼으로 저장하지 않고 조회 시 계산한다.
- 순번은 같은 날짜, 시간, 테마 슬롯 안에서 생성시간과 id 순서로 결정한다.

### 자동 승인

- 사용자가 예약을 취소하면 같은 슬롯의 가장 오래된 예약대기를 예약으로 자동 승격한다.
- 사용자가 예약을 변경하면 기존 슬롯의 가장 오래된 예약대기를 예약으로 자동 승격한다.
- 예약대기가 예약으로 승격되면 승격된 예약대기는 삭제한다.
- 예약대기가 승격되거나 취소되면 남은 대기 순번은 다음 조회 시 재계산한다.

### 트랜잭션 경계

- 예약 취소, 예약 생성, 승격된 예약대기 삭제는 하나의 트랜잭션으로 처리한다.
- 예약 변경, 기존 슬롯의 예약 생성, 승격된 예약대기 삭제는 하나의 트랜잭션으로 처리한다.
- 중간에 일부 작업만 성공하면 빈 슬롯과 대기 상태가 불일치할 수 있으므로 전체 롤백이 필요하다.
- 예약대기 취소는 단일 대기 삭제로 처리하며 예약 승격 트랜잭션과 분리한다.

### 예약 마감

- 사용자 예약 신청, 변경, 취소와 사용자 예약대기 신청은 예약 시작 10분 전까지만 허용한다.
- 예약대기 취소는 슬롯을 비우거나 예약 승격을 발생시키지 않으므로 마감 정책을 적용하지 않는다.
- 관리자 예약 생성, 변경, 삭제는 운영 목적의 강제 조작으로 보고 마감 정책을 적용하지 않는다.
- 예약 가능 여부는 날짜만 보지 않고 예약 날짜와 예약 시작 시간을 합친 일시를 기준으로 판단한다.

## 테스트 전략

단위 테스트와 통합 테스트를 나누는 기준은 외부 의존성을 대체하고 한 객체의 책임만 검증하는지 여부이다. JUnit과 테스트 더블을 사용해 특정 객체의 책임만 확인하면 단위 테스트로 보고, Spring Context, DB, HTTP 요청 처리, 트랜잭션처럼 여러 구성요소가 함께 동작하면 통합 테스트로 본다.

### Controller

Controller의 역할은 HTTP 요청을 받고, 요청 값을 검증한 뒤 Service에 위임하고, 적절한 상태 코드와 응답을 반환하는 것이다. Controller 테스트에서는 MockMvc 또는 RestAssured를 사용해 요청 파라미터와 요청 바디 검증, 응답 상태 코드, JSON 응답 형태, Service 호출 흐름을 확인한다.

### Service

Service의 역할은 도메인 객체와 Repository를 조합해 비즈니스 규칙을 지키는 것이다. 서비스 단위 테스트에서는 실제 DB 대신 메모리 기반 Fake Repository를 사용한다. 이를 통해 SQL이나 저장 방식에 의존하지 않고 예약 생성, 중복 예약 방지, 마감된 예약 생성/수정/취소 제한, 이름 기반 조회 같은 비즈니스 규칙을 검증한다.

예약 취소/수정 시 예약대기가 승격되고 실패하면 롤백되는 흐름처럼 여러 저장소와 트랜잭션이 함께 동작해야 하는 부분은 서비스 통합 테스트로 확인한다.

### Domain

Domain의 역할은 핵심 규칙과 유효성을 스스로 지키는 것이다. 도메인 테스트에서는 예약자 이름, 날짜, 시간, 테마, 예약대기 생성 조건처럼 객체가 생성될 때 지켜야 하는 규칙과 예약 가능 시간이 지났는지 판단하는 로직을 검증한다.

### Repository

Repository의 역할은 도메인 객체를 DB에 저장하고 조회하며, SQL 결과를 객체로 올바르게 변환하는 것이다. Repository 테스트에서는 `@JdbcTest`와 H2 DB를 사용해 실제 SQL, parameter binding, RowMapper가 의도대로 동작하는지 확인한다. 특히 예약대기 순번 조회, 특정 슬롯의 가장 오래된 대기 조회, 중복 제약처럼 SQL에 의존하는 로직을 검증한다.

JPA 전환 후에는 이 Repository 테스트와 서비스 통합 테스트가 기존 동작 보존 여부를 확인하는 주요 기준이 된다.

## JPA 전환 시 확인할 점

- 기존 SQL이 하던 일을 JPA 매핑, Repository 메서드, JPQL 중 어디로 옮길지 구분한다.
- 단순 CRUD와 복잡한 조회를 같은 방식으로 처리하려 하지 않는다.
- `Reservation`이 `ReservationDate`, `ReservationTime`, `Theme`를 참조하는 구조를 JPA 연관관계로 어떻게 표현할지 먼저 결정한다.
- `WaitingReservation`의 순번 계산은 저장 값이 아니라 파생 값이므로 조회 전략을 별도로 검토한다.
- 예약 취소/변경과 예약대기 승격은 트랜잭션 경계가 깨지면 안 된다.
- JPA가 생성한 SQL을 로그로 확인하고, 기존 JdbcTemplate SQL의 의미와 비교한다.
- 전환 후에도 `./gradlew test`로 전체 자동화 테스트를 통과시키는 것을 기준으로 삼는다.

## 영속성 컨텍스트 관찰 기록

### 1. Dirty Checking

- 시도한 코드: `@Transactional` 범위 안에서 `reservation.changeSlot(newDate, newTime)`만 호출하고 `save()`는 호출하지 않았다.
- 예측: `save()`를 안 했으니 `UPDATE`가 안 나갈 수도 있다고 생각했다.
- 실제: `flush()` 시점에 `update reservation set ... where id=?`가 발생했다.
- 이유: 조회된 엔티티는 영속 상태라서, JPA가 트랜잭션 안에서 변경 전후를 비교해 변경을 자동 반영한다.

### 2. 1차 캐시

- 시도한 코드: 같은 트랜잭션에서 `findById(reservationId)`를 두 번 호출했다.
- 예측: `SELECT`가 두 번 나갈 수도 있다고 생각했다.
- 실제: 첫 번째만 `SELECT`가 발생했고, 두 번째는 SQL이 생략됐다. `first == second`도 `true`였다.
- 이유: 같은 영속성 컨텍스트 안에서는 같은 id의 엔티티를 1차 캐시에 보관한다.

### 3. 쓰기 지연

- 시도한 코드: `reservationRepository.save(...)` 후 `flush()` 전후 SQL을 봤다.
- 예측: `INSERT`가 `flush`나 `commit`까지 지연될 것이라고 생각했다.
- 실제: 현재 엔티티가 `GenerationType.IDENTITY`라서 `save()` 직후 `INSERT`가 바로 발생했다.
- 이유: `IDENTITY` 전략은 DB가 id를 만들어 주므로, Hibernate가 id를 얻기 위해 `INSERT`를 먼저 실행해야 한다.

### 4. Flush 시점

- 시도한 코드: 엔티티 필드를 변경한 뒤 JPQL `select r from Reservation r`을 실행했다.
- 예측: JPQL `SELECT`만 나갈 것이라고 생각했다.
- 실제: JPQL 실행 직전에 `UPDATE`가 먼저 나가고, 그 다음 `SELECT`가 실행됐다.
- 이유: JPQL 결과가 DB 상태와 어긋나지 않도록 Hibernate가 쿼리 실행 전에 자동 flush를 수행한다.

### 5. Fetch 기본값

- 시도한 코드: `Reservation` 조회 후 `reservation.getTime().getStartAt()`에 접근했다.
- 예측: `@ManyToOne` 기본값은 `EAGER`라서 처음 조회 때 같이 가져올 수 있다고 생각했다.
- 실제: 현재 코드는 `@ManyToOne(fetch = FetchType.LAZY)`로 명시되어 있어 예약 조회 시에는 `reservation`만 조회되고, `time` 접근 시 추가 `SELECT`가 발생했다.
- 이유: JPA 기본값은 `ManyToOne = EAGER`, `OneToMany = LAZY`지만, 현재 매핑은 명시적으로 `LAZY`를 선택했기 때문이다.

### 6. LazyInitializationException

- 시도한 코드: 트랜잭션 안에서 `Reservation`만 조회하고, 트랜잭션 밖에서 `reservation.getTime().getStartAt()`을 호출했다.
- 예측: `LAZY` 필드라서 접근 시 조회가 발생할 것이라고 생각했다.
- 실제: `org.hibernate.LazyInitializationException: no session`이 발생했다.
- 이유: 트랜잭션이 끝나며 영속성 컨텍스트가 닫혔고, 닫힌 뒤에는 LAZY 프록시를 초기화할 수 없다.

## 내 예약 목록 조회 전략 기록

### 배경

2단계 요구사항은 화면에 보여줄 유효한 내 예약 목록을 조회하는 것이다. 여기서 유효한 예약은 지난 예약을 제외한 예약이다. 단순히 `findByName`으로 예약을 모두 가져온 뒤 Java 코드에서 필터링할 수도 있지만, 유효 여부는 예약 날짜와 예약 시간이 결정한다. 이 값들은 DB가 이미 가지고 있으므로 DB에서 먼저 조건을 걸어 조회하는 편이 더 적절하다고 판단했다.

### JPQL을 선택한 이유

조회 조건은 단순히 이름이 같은 예약을 찾는 것이 아니었다. 오늘 이후 예약이거나, 오늘 예약이라면 현재 시간 이후인 예약만 조회해야 했다.

```text
date > currentDate
or (date = currentDate and time > currentTime)
```

이 조건을 메서드 이름 쿼리로 표현하면 `Or` 조건 때문에 이름이 길어지고, 괄호로 묶이는 조건의 의도가 잘 드러나지 않는다고 느꼈다. 그래서 복잡한 조건은 JPQL의 조건식으로 표현하는 편이 더 읽기 쉽다고 판단했다.

### EntityGraph를 함께 사용한 이유

JPQL을 사용한다고 N+1 문제가 자동으로 해결되는 것은 아니다. 현재 `Reservation`은 `ReservationDate`, `ReservationTime`, `Theme`를 `LAZY`로 참조한다. 그런데 응답 DTO를 만들 때는 날짜, 시간, 테마 정보가 모두 필요하다.

그래서 조회 조건은 JPQL에 두고, 응답 생성에 필요한 연관 객체 로딩 범위는 `@EntityGraph(attributePaths = {"date", "time", "theme"})`로 분리했다.

```java
@EntityGraph(attributePaths = {"date", "time", "theme"})
@Query("""
        select r
        from Reservation r
        where r.name = :name
          and (r.date.playDay > :currentDate
            or (r.date.playDay = :currentDate and r.time.startAt > :currentTime))
        order by r.date.playDay, r.time.startAt
        """)
List<Reservation> findUpcomingByName(
        @Param("name") String name,
        @Param("currentDate") LocalDate currentDate,
        @Param("currentTime") LocalTime currentTime
);
```

`join fetch`도 N+1을 줄일 수 있지만, 이번에는 조건과 로딩 책임을 분리해서 읽을 수 있는 `@EntityGraph`가 더 적절하다고 봤다. JPQL은 유효한 예약을 찾는 조건에 집중하고, EntityGraph는 DTO 변환에 필요한 객체 그래프를 어디까지 로딩할지 표현한다.

### 한계

JPQL과 `@EntityGraph`는 모두 문자열 기반이다. 엔티티 필드명이 변경되어도 Java 컴파일러가 바로 잡아주지 못하고, 실행 시점이나 쿼리 검증 시점에 오류를 발견할 수 있다.

또한 페이징이 필요해지면 다시 점검해야 한다. 현재처럼 `ManyToOne` 연관 객체를 함께 로딩하는 정도는 비교적 안전하지만, `OneToMany` 같은 컬렉션을 함께 로딩하면 row 중복과 count query 문제가 생길 수 있다. 페이징이 들어오면 실제 SQL과 count query를 확인하면서 조회 방식을 다시 판단해야 한다.

## 예약 대기 N+1 / EntityGraph 관찰 기록

### 배경

3단계에서 `WaitingReservation`도 JPA 엔티티로 전환했다. `Reservation`과 일관되게 `ReservationDate`, `ReservationTime`, `Theme`는 `@ManyToOne(fetch = FetchType.LAZY)`로 참조하도록 했다.

처음에는 예약 대기 목록을 조회한 뒤 응답 DTO를 만들 때 `getDate().getPlayDay()`, `getTime().getStartAt()`, `getTheme().getName()`에 접근하므로 N+1이 발생할 수 있다고 봤다. 따라서 순번 계산 조건은 JPQL에 두고, 응답 생성에 필요한 연관 객체는 `@EntityGraph(attributePaths = {"date", "time", "theme"})`로 함께 로딩하도록 했다.

### 시도한 코드

```java
@EntityGraph(attributePaths = {"date", "time", "theme"})
@Query("""
        select new roomescape.domain.waitingreservation.dto.WaitingReservationWithRank(
            w,
            (
                select count(w2) + 1
                from WaitingReservation w2
                where w2.date = w.date
                  and w2.time = w.time
                  and w2.theme = w.theme
                  and (
                    w2.createdAt < w.createdAt
                    or (w2.createdAt = w.createdAt and w2.id < w.id)
                  )
            )
        )
        from WaitingReservation w
        where w.name = :name
        order by w.date.playDay, w.time.startAt, w.id
        """)
List<WaitingReservationWithRank> findAllByNameWithRank(@Param("name") String name);
```

### 예측 SQL

`waiting_reservation`을 기준으로 조회하되, 순번은 같은 날짜, 시간, 테마 슬롯 안에서 현재 대기보다 먼저 생성된 row 수를 `count` 서브쿼리로 계산할 것이라고 예상했다. 또한 `@EntityGraph` 때문에 DTO 변환에 필요한 `reservation_date`, `reservation_time`, `theme`도 함께 조회될 것이라고 봤다.

### 실제 SQL

테스트 로그에서 확인한 Hibernate SQL은 다음과 같았다.

```sql
select
    wr1_0.id,
    wr1_0.created_at,
    d3_0.id,
    d3_0.play_day,
    wr1_0.name,
    t5_0.id,
    t5_0.content,
    t5_0.name,
    t5_0.url,
    t6_0.id,
    t6_0.start_at,
    (select
        (count(wr2_0.id)+1)
    from
        waiting_reservation wr2_0
    where
        wr2_0.date_id=wr1_0.date_id
        and wr2_0.time_id=wr1_0.time_id
        and wr2_0.theme_id=wr1_0.theme_id
        and (
            wr2_0.created_at<wr1_0.created_at
            or (
                wr2_0.created_at=wr1_0.created_at
                and wr2_0.id<wr1_0.id
            )
        ))
from
    waiting_reservation wr1_0
join
    reservation_date d3_0
        on d3_0.id=wr1_0.date_id
left join
    theme t5_0
        on t5_0.id=wr1_0.theme_id
join
    reservation_time t6_0
        on t6_0.id=wr1_0.time_id
where
    wr1_0.name=?
order by
    d3_0.play_day,
    t6_0.start_at,
    wr1_0.id
```

### 왜 이렇게 나왔는가

JPQL의 순번 계산은 SQL에서도 `count + 1` 서브쿼리로 변환됐다. 같은 슬롯인지 비교하기 위해 `date_id`, `time_id`, `theme_id`를 현재 row와 비교했고, 순서를 판단하기 위해 `created_at`과 `id`를 함께 비교했다.

`@EntityGraph`로 지정한 `date`, `time`, `theme`는 실제 SQL에서 join으로 함께 조회됐다. 이 덕분에 응답 DTO를 만들 때 날짜, 시간, 테마 필드에 접근하더라도 각 예약 대기마다 추가 select가 반복되는 상황을 피할 수 있다.

### 정리

메서드 이름 쿼리로도 단순 조건 조회는 가능하지만, 이번 순번 계산은 단순 필터링이 아니라 같은 슬롯 안에서 상대적인 위치를 계산하는 문제였다. 조건이 이름, 날짜, 시간, 테마, 생성 시간, id 비교까지 이어지므로 메서드 이름으로 표현하면 의도를 읽기 어렵다고 판단했다.

서비스에서 대기 목록을 가져온 뒤 순번을 계산하는 방법도 가능하지만, 필요한 것보다 많은 데이터를 애플리케이션으로 가져올 수 있다. 이번 구현에서는 DB가 가진 데이터로 순번을 계산하도록 JPQL을 사용하고, N+1 대응은 `@EntityGraph`로 분리하는 방식이 더 적절하다고 봤다.

## 4단계 이후 JPA 설계 토론 메모

### 결정 #1: 내 예약 목록 조회 전략

- 선택한 것:
  - JPQL + `@EntityGraph`
  - 유효 예약 조건은 JPQL로 표현하고, DTO 생성에 필요한 `date`, `time`, `theme` 로딩은 EntityGraph로 분리했다.

- 비교한 대안:
  - 메서드 이름 쿼리
  - JPQL + `join fetch`
  - 예약 목록을 모두 조회한 뒤 Java에서 필터링

- 선택의 비교 기준:
  - 조건의 가독성
  - 조회 조건과 로딩 책임의 분리
  - 로딩 범위 변경 시 수정 범위
  - 같은 로딩 범위를 다른 조회에서도 재사용할 수 있는지

- 선택의 근거:
  - 날짜와 시간 조건이 `date > today or (date = today and time > now)` 형태라 메서드 이름 쿼리로 표현하면 의도가 흐려진다고 봤다.
  - JPQL은 "어떤 예약을 조회할 것인가"에 집중하고, EntityGraph는 "어디까지 객체 그래프를 로딩할 것인가"에 집중하도록 나누고 싶었다.
  - 나중에 로딩 범위만 바뀐다면 JPQL 조건을 건드리지 않고 EntityGraph만 조정할 수 있다.
  - 같은 로딩 범위가 반복된다면 EntityGraph 쪽이 재사용하기 좋다고 느꼈다.
  - `join`과 `join fetch`가 한 쿼리 안에 섞이면 조건을 위한 조인인지 로딩을 위한 조인인지 계속 구분해서 읽어야 해서 피로도가 있다고 생각했다.

- 이 선택의 한계 / 다음에 망가질 수 있는 지점:
  - EntityGraph도 문자열 기반이라 필드명 변경을 컴파일 시점에 잡지 못한다.
  - 실제 SQL이 코드에 직접 드러나는 `join fetch`보다 생성 SQL을 로그로 확인해야 한다.
  - 페이징이나 컬렉션 로딩이 들어오면 row 중복과 count query 문제를 다시 확인해야 한다.

- 동료에게 묻고 싶은 것:
  - 조건과 로딩 책임을 분리하는 장점이 `join fetch`의 명시성을 포기할 만큼 충분하다고 보는지 궁금하다.

### 결정 #2: 예약 대기 모델링

- 선택한 것:
  - `Reservation.status`로 통합하지 않고 `WaitingReservation`을 별도 엔티티로 유지했다.

- 비교한 대안:
  - `Reservation`에 `status` 컬럼을 추가해 예약과 대기를 하나의 테이블/엔티티로 관리하는 방식
  - 부분 유니크 제약으로 예약과 대기의 중복 조건을 다르게 관리하는 방식

- 선택의 비교 기준:
  - 현재 요구사항에서의 구현 비용
  - 예약과 예약 대기의 유니크 제약 차이
  - 예약 대기 정책이 앞으로 독립적으로 확장될 가능성
  - 분리했을 때 따라오는 동시성/일관성 문제를 감당할 수 있는지

- 선택의 근거:
  - 처음에는 `status` 컬럼으로 통합하는 방식이 자연스럽다고 생각했다. 예약과 예약 대기는 같은 테마, 날짜, 시간을 기준으로 동작하기 때문이다.
  - 하지만 예약은 같은 슬롯에 하나만 존재해야 하고, 예약 대기는 같은 슬롯이어도 사용자 이름이 다르면 여러 명이 존재할 수 있다. 이 유니크 제약 차이 때문에 별도 엔티티를 유지했다.
  - 현재 요구사항만 보면 `status` 통합이 더 단순했을 수 있다고 생각한다.
  - 다만 이미 별도 도메인으로 구현되어 있었고, 앞으로 예약 대기만의 필드나 정책이 늘어나면 분리된 구조가 요구사항 변화에 더 유리할 수 있다고 봤다.
  - 특히 분리 구조에서는 동시성이나 일관성 문제가 따라오지만, 그 문제에 대한 대비가 갖춰진다면 장기적으로는 예약 대기를 독립된 도메인으로 다루는 편이 확장에는 유리할 수 있다고 생각했다.

- 이 선택의 한계 / 다음에 망가질 수 있는 지점:
  - 현재 요구사항 수준에서는 엔티티 분리가 과한 선택일 수 있다.
  - 예약과 예약 대기가 별도 데이터로 움직이므로, 빈 슬롯인데 대기만 남는 상태나 동시성 문제가 생길 수 있다.
  - 예약 취소, 대기 신청, 자동 승격이 동시에 일어나는 상황을 충분히 방어하지 못하면 일관성이 깨질 수 있다.

- 동료에게 묻고 싶은 것:
  - 현재 요구사항처럼 예약과 대기가 거의 같은 필드를 공유한다면 `status` 통합이 더 나은 선택인지, 아니면 유니크 제약 차이만으로도 별도 엔티티를 둘 근거가 충분한지 궁금하다.

### 결정 #3: 자동 승격 트랜잭션

- 선택한 것:
  - 예약 취소/수정 시 `ReservationService` 안에서 가장 오래된 예약 대기를 자동 승격한다.
  - 예약 삭제, 대기 조회, 예약 생성, 승격된 대기 삭제를 하나의 트랜잭션으로 처리했다.

- 비교한 대안:
  - 예약 취소와 대기 승격을 분리해서 처리하는 방식
  - 비동기 이벤트로 예약 취소 이후 대기 승격을 후속 처리하는 방식
  - 실패 시 보상 트랜잭션으로 복구하는 방식

- 선택의 비교 기준:
  - 예약 도메인의 데이터 정합성
  - 사용자 경험
  - 구현 복잡도
  - 실패 상황을 내가 설명하고 복구할 수 있는지

- 선택의 근거:
  - 자동 승격 실패 때문에 예약 취소가 실패하는 UX는 좋지 않다고 느꼈다.
  - 하지만 예약 취소와 대기 승격을 분리하면, 예약은 취소됐는데 대기는 승격되지 않거나, 예약과 대기 상태가 어긋나는 데이터 정합성 문제가 발생할 수 있다고 봤다.
  - 단순 조회수처럼 조금 틀려도 되는 데이터가 아니라, 실제 예약과 관련된 비즈니스 요구사항이므로 정합성이 더 중요하다고 판단했다.
  - 비동기 이벤트나 보상 트랜잭션 같은 키워드는 들어봤지만, 아직 적용 방법과 실패 대응 방식을 명확히 알지 못한다.
  - 그래서 이번 구현에서는 내가 설명할 수 있고, 가장 단순하게 정합성을 지킬 수 있는 하나의 트랜잭션 방식을 선택했다.

- 이 선택의 한계 / 다음에 망가질 수 있는 지점:
  - 자동 승격 실패가 예약 취소 실패로 전파될 수 있어 UX가 좋지 않을 수 있다.
  - 동시 요청 상황에서 같은 대기가 중복 승격되거나, 예약 취소와 대기 신청이 엇갈리는 문제를 아직 충분히 방어하지 못했다.
  - 알림이나 외부 서비스가 붙으면 하나의 트랜잭션 안에 묶기 어려워질 수 있다.

- 동료에게 묻고 싶은 것:
  - 예약 도메인처럼 정합성이 중요한 경우에도 사용자 요청을 먼저 성공시키고 후속 승격을 비동기로 처리하는 게 더 나은지, 그렇다면 중간 상태와 실패 복구를 어떻게 설계하는지 궁금하다.
