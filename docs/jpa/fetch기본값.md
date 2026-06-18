## 관찰: fetch 기본값

### 시도한 코드

테스트용 Entity를 따로 만들어서 fetch 옵션을 명시하지 않음.

FetchDefaultReservation은 FetchDefaultTheme을 `@ManyToOne`으로 참조하게 만들고,
FetchDefaultTheme은 FetchDefaultReservation 목록을 `@OneToMany`로 가지게 만듦.

```java
@ManyToOne
private FetchDefaultTheme theme;
```

```java
@OneToMany(mappedBy = "theme")
private List<FetchDefaultReservation> reservations = new ArrayList<>();
```

그 뒤 각각을 조회하면서 연관 객체가 바로 초기화되는지 확인함.

### 예측한 SQL/동작

`@ManyToOne`은 fetch 옵션을 생략하면 기본값이 EAGER일 것이다.
그래서 FetchDefaultReservation을 조회할 때 FetchDefaultTheme도 같이 조회될 것이라 예상함.

반대로 `@OneToMany`는 fetch 옵션을 생략하면 기본값이 LAZY일 것이다.
그래서 FetchDefaultTheme을 조회할 때 reservations 목록은 바로 조회되지 않고,
실제로 목록에 접근하는 순간 SELECT SQL이 나갈 것이라 예상함.

### 실제 SQL/동작

`@ManyToOne`을 생략했을 때

```sql
select fdr1_0.id,
       fdr1_0.name,
       t1_0.id,
       t1_0.name
from fetch_default_reservation fdr1_0
left join fetch_default_theme t1_0
       on t1_0.id = fdr1_0.theme_id
where fdr1_0.id = ?
```

FetchDefaultReservation을 조회하는 순간 FetchDefaultTheme도 함께 조회되었다.
`Hibernate.isInitialized(found.getTheme())` 값도 true였다.

`@OneToMany`를 생략했을 때

Theme을 조회한 직후에는 reservations 목록이 초기화되지 않았다.
`Hibernate.isInitialized(found.getReservations())` 값이 false였다.

그 뒤 `found.getReservations().size()`처럼 목록에 접근하자 다음 SQL이 나갔다.

```sql
select r1_0.theme_id,
       r1_0.id,
       r1_0.name
from fetch_default_reservation r1_0
where r1_0.theme_id = ?
```

### 왜 그런가

JPA에서 `@ManyToOne`의 기본 fetch 전략은 EAGER이다.
그래서 다대일 관계에서는 연관된 단일 객체를 기본적으로 함께 가져오려고 한다.

이번 테스트에서는 FetchDefaultReservation을 조회할 때 FetchDefaultTheme까지 left join으로 함께 조회되었다.
즉, Reservation 입장에서는 Theme이 이미 초기화된 상태였다.

반대로 `@OneToMany`의 기본 fetch 전략은 LAZY이다.
일대다 관계에서는 연관된 컬렉션의 크기가 커질 수 있기 때문이다.
Theme 하나를 조회했는데 그 Theme에 연결된 Reservation 목록까지 항상 가져오면 필요 없는 조회가 쉽게 많아질 수 있다.

그래서 FetchDefaultTheme을 조회한 직후에는 reservations 목록이 프록시처럼 남아 있었고,
실제로 목록의 size를 확인하는 순간에야 Reservation 목록을 조회하는 SELECT SQL이 실행되었다.

정리하면 `@ManyToOne`은 기본값이 EAGER이고, `@OneToMany`는 기본값이 LAZY이다.
하지만 실무 코드에서는 기본값에 기대기보다 fetch 전략을 명시해서 의도를 드러내는 것이 더 안전하다고 느껴진다.
