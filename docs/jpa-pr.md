## 선택미션 JPA PR 본문

### 0단계 - 기본 코드 준비하기

- 시작 브랜치: `origin/everypine`
- 작업 브랜치: `origin/jpa`
- 이번 미션에서 건드릴 범위: 모든 도메인 패키지의 `repository` 및 `entity(domain)`

### 1단계 - JPA 전환

#### 관찰 과제 1: 영속성 컨텍스트 신호 캡처
- **Dirty Checking**
    - 확인 코드: `@Transactional` 메서드에서 엔티티 필드 수정 후 `save()`를 호출하지 않는다.
    - 관찰 내용: commit 시점에 `UPDATE`가 자동 발행되는지 확인한다.

```
1. 시도한 코드  
Reservation reservation = saveReservation("before");  
Long reservationId = reservation.getId();  
entityManager.flush();  

ReflectionTestUtils.setField(reservation, "name", "after");  
entityManager.flush();  
entityManager.clear();  

Reservation actual = reservationRepository.findById(reservationId).get();  
assertThat(actual.getName()).isEqualTo("after");

2. 예측한 SQL/동작 
name 필드가 before로 유지될 것이다. 
3. 실제 SQL/동작 
name 필드가 after로 업데이트되었다.
4. 왜 다른가
JPA가 변경 감지를 수행하여 자동으로 update가 되기 때문이다.
```

- **1차 캐시**
    - 확인 코드: 같은 트랜잭션 안에서 `findById()`를 두 번 호출한다.
    - 관찰 내용: 두 번째 `SELECT`가 생략되는지 확인한다.

```
1. 시도한 코드   
Reservation reservation = saveReservation("reservation");  
Long reservationId = reservation.getId();  
entityManager.flush();  
entityManager.clear();  

Reservation first = reservationRepository.findById(reservationId).get();  
Reservation second = reservationRepository.findById(reservationId).get();  

assertThat(first).isSameAs(second);
2. 예측한 SQL/동작 
hibernate sql 로그에 select문이 2번 출력될 것이다.
3. 실제 SQL/동작 
1번만 출력되었다.
4. 왜 다른가
jpa는 1차 캐시를 사용하는데, 트랜잭션 내에서 최초 조회된 엔티티가 해당 캐시에 저장된다. 이후 동일 트랜잭션에서 재조회를 수행하면 db에서 가져오지 않고 1차 캐시에 저장된 엔티티를 가져온다.
```
- **쓰기 지연**
    - 확인 코드: `save()` 호출 후 `flush()` 전·후의 DB 상태를 비교한다.
    - 관찰 내용: `INSERT`가 `flush` 또는 commit 시점에 발행되는지 확인한다.

```
1. 시도한 코드   
Reservation reservation = saveReservation("before");  
entityManager.flush();  

ReflectionTestUtils.setField(reservation, "name", "write-behind");  

assertThat(countReservationByName("write-behind")).isZero();  

entityManager.flush();  

assertThat(countReservationByName("write-behind")).isOne();

private int countReservationByName(String name) {  
    return jdbcTemplate.queryForObject(  
        "SELECT COUNT(*) FROM reservation WHERE name = ?",  
        Integer.class,  
        name  
    );  
}

2. 예측한 SQL/동작
필드가 변경되면 바로 save가 호출되어 db에 반영이 될 것이다.
3. 실제 SQL/동작 
flush가 일어난 이후에 db에 반영된다.
4. 왜 다른가
JPA가 엔티티 필드 변경 순간에 즉시 SQL을 실행하지 않고, 영속성 컨텍스트 안에 변경 사항을 모아두었다가 flush 시점에 DB로 내보내기 때문이다.
```
- **Flush 시점**
    - 확인 코드: 명시적 `flush()` 호출, 트랜잭션 종료, JPQL 실행 직전을 비교한다.
    - 관찰 내용: 영속성 컨텍스트의 변경 사항이 DB와 동기화되는 시점을 확인한다.

```
1. 시도한 코드     
  
@Nested  
@DisplayName("Flush 시점")  
class FlushTimingTest {  
  
    @Test  
    @DisplayName("명시적 flush()를 호출하면 변경 사항이 DB에 동기화된다")  
    void explicitFlush() {  
        Reservation reservation = saveReservation("before");  
        entityManager.flush();  
  
        ReflectionTestUtils.setField(reservation, "name", "explicit-flush");  
        assertThat(countReservationByName("explicit-flush")).isZero();  
  
        entityManager.flush();  
  
        assertThat(countReservationByName("explicit-flush")).isOne();  
    }  
  
    @Test  
    @DisplayName("JPQL 실행 직전에 변경 사항이 flush된다")  
    void flushBeforeJpql() {  
        Reservation reservation = saveReservation("before");  
        entityManager.flush();  
  
        ReflectionTestUtils.setField(reservation, "name", "jpql-flush");  
        assertThat(countReservationByName("jpql-flush")).isZero();  
  
        List<Reservation> reservations = entityManager.getEntityManager()  
            .createQuery("SELECT r FROM reservation r", Reservation.class)  
            .getResultList();  
  
        assertThat(reservations).isNotEmpty();  
        assertThat(countReservationByName("jpql-flush")).isOne();  
    }  
  
    @Test  
    @Transactional(propagation = Propagation.NOT_SUPPORTED)  
    @Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)  
    @DisplayName("트랜잭션 종료 시 변경 사항이 commit 전에 flush된다")  
    void flushOnTransactionCommit() {  
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);  
        Long reservationId = transactionTemplate.execute(status -> {  
            Reservation reservation = saveReservation("before");  
            entityManager.flush();  
            ReflectionTestUtils.setField(reservation, "name", "commit-flush");  
            return reservation.getId();  
        });  
  
        assertThat(reservationId).isNotNull();  
        assertThat(countReservationByName("commit-flush")).isOne();  
    }  
}
2. 예측한 SQL/동작 
명시적 `flush()` 호출: db에 변경사항이 저장된다.
트랜잭션 종료: db에 변경사항이 저장된다.
JPQL 실행 직전: db에 변경사항이 저장된다.
3. 실제 SQL/동작
명시적 `flush()` 호출: db에 변경사항이 저장된다.
트랜잭션 종료: db에 변경사항이 저장된다.
JPQL 실행 직전: db에 변경사항이 저장된다.
4. 왜 다른가
해당없음
```
- **Fetch 기본값**
    - 확인 코드: `@ManyToOne`, `@OneToMany`에서 `fetch` 옵션을 명시하지 않는다.
    - 관찰 내용: `@ManyToOne`은 기본 `EAGER`, `@OneToMany`는 기본 `LAZY`로 동작하는지 확인한다.

```
1. 시도한 코드   
FetchType manyToOneDefault = (FetchType) ManyToOne.class.getMethod("fetch")  
	.getDefaultValue();  
FetchType oneToManyDefault = (FetchType) OneToMany.class.getMethod("fetch")  
	.getDefaultValue();  

assertThat(manyToOneDefault).isEqualTo(FetchType.EAGER);  
assertThat(oneToManyDefault).isEqualTo(FetchType.LAZY);
2. 예측한 SQL/동작 
@OneToMany만 LAZY가 기본값으로 예측했다.
3. 실제 SQL/동작 
@ManyToOne, @OneToMany는 각각 EAGER, LAZY를 기본값으로 동작한다.
4. 왜 다른가
@ManyToOne과 @OneToMany은 연관관계의 방향과 카디널리티가 다르며,  
JPA 애노테이션 정의 자체에서 fetch 옵션의 기본값이 다르게 선언되어 있기 때문이다. 그 값은 각각 EAGER, LAZY이다.
```
- **LazyInitializationException**
    - 확인 코드: 트랜잭션 밖에서 LAZY 연관 필드에 접근한다.
    - 관찰 내용: 영속성 컨텍스트가 닫힌 뒤 프록시 초기화 시 예외가 발생하는지 확인한다.

```
1. 시도한 코드 
TransactionTemplate transactionTemplate = 
	new TransactionTemplate(transactionManager);  
Long reservationId = transactionTemplate.execute(status 
	-> saveReservation("reservation").getId());  

Reservation found = transactionTemplate.execute(status -> {  
	Reservation reservation = reservationRepository.findById(reservationId).get();  
	assertThat(Hibernate.isInitialized(reservation.getTime())).isFalse();  
	return reservation;  
});  

assertThatThrownBy(() -> found.getTime().getStartAt())  
.isInstanceOf(LazyInitializationException.class);

2. 예측한 SQL/동작 
별다른 Exception이 발생하지 않고 정상적으로 start_at이 조회될 것이다.
3. 실제 SQL/동작 
LazyInitializationException이 발생한다.
4. 왜 다른가
LAZY 연관 객체는 실제 접근 시점에 DB 조회가 필요하다.  
그런데 실제 접근 시점에는 이미 트랜잭션과 영속성 컨텍스트가 종료되어 있다.  
그래서 Hibernate가 추가 조회를 할 수 없어 LazyInitializationException이 발생한다.
```