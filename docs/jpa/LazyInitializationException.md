## 관찰: LazyInitializationException

### 시도한 코드

Slot은 ReservationTime과 Theme을 `@ManyToOne(fetch = FetchType.LAZY)`로 가지고 있음.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "time_id")
private ReservationTime time;
```

Slot을 저장한 뒤 EntityManager로 Slot만 조회함.
이때 ReservationTime은 아직 초기화하지 않음.

그 뒤 테스트 트랜잭션을 종료하고, 트랜잭션 밖에서 `found.getTime().getStartAt()`을 호출함.

```java
Slot found = entityManager.find(Slot.class, slot.getId());

TestTransaction.end();

found.getTime().getStartAt();
```

### 예측한 SQL/동작

Slot을 조회할 때는 Slot 테이블만 조회될 것이다.
왜냐하면 Slot의 time 필드는 LAZY이기 때문이다.

따라서 ReservationTime은 실제 객체가 아니라 프록시 상태로 남아 있을 것이라 예상함.

그리고 트랜잭션이 끝난 뒤에는 영속성 컨텍스트가 닫히기 때문에,
그 상태에서 프록시를 초기화하려고 하면 LazyInitializationException이 발생할 것이라 예상함.

### 실제 SQL/동작

Slot을 조회할 때 실행된 SQL

```sql
select s1_0.id,
       s1_0.date,
       s1_0.theme_id,
       s1_0.time_id
from slot s1_0
where s1_0.id = ?
```

ReservationTime 테이블은 join되지 않았다.
`Hibernate.isInitialized(found.getTime())` 값도 false였다.

그 뒤 트랜잭션을 종료하고 `found.getTime().getStartAt()`을 호출하자
`LazyInitializationException`이 발생했다.

### 왜 그런가

LAZY는 연관 객체를 지금 바로 가져오지 않고, 실제로 사용할 때 가져오겠다는 의미이다.

Slot을 조회할 때 ReservationTime까지 같이 조회하지 않았기 때문에,
Slot의 time 필드에는 진짜 ReservationTime 객체가 아니라 나중에 조회할 수 있는 프록시가 들어있다.

트랜잭션 안에서는 이 프록시를 건드리는 순간 Hibernate가 DB에 추가 SELECT를 보내서 실제 ReservationTime을 가져올 수 있다.
왜냐하면 아직 영속성 컨텍스트가 살아있고, DB 조회를 할 수 있는 Session이 연결되어 있기 때문이다.

하지만 트랜잭션이 끝나면 영속성 컨텍스트도 닫힌다.
이후에는 프록시가 실제 데이터를 가져오려고 해도 사용할 수 있는 Session이 없다.

그래서 트랜잭션 밖에서 아직 초기화되지 않은 LAZY 필드에 접근하면 LazyInitializationException이 발생한다.

정리하면 LazyInitializationException은 LAZY 자체가 문제라기보다,
프록시를 초기화해야 하는 순간에 영속성 컨텍스트가 이미 닫혀 있어서 발생하는 예외이다.
