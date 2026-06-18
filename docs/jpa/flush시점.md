## 관찰: flush 시점

### 시도한 코드

Theme 객체를 DB에 저장한 뒤 다시 조회해서 영속성 컨텍스트에 올려놓음.
그 뒤 Theme의 name 값을 바꾸고, 어떤 시점에 UPDATE SQL이 나가는지 확인함.

이번에는 세 가지 경우를 나눠서 봄.

```
1. entityManager.flush()를 직접 호출함
2. Theme을 변경한 뒤 JPQL을 실행함
3. Theme을 변경한 뒤 트랜잭션을 종료함
```

### 예측한 SQL/동작

Theme의 name 값을 바꾸는 순간 바로 UPDATE가 나가지는 않을 것이다.
왜냐하면 객체의 값이 바뀐 것이지, 아직 DB와 동기화된 것은 아니기 때문이다.

대신 flush가 발생하는 시점에 Hibernate가 변경 감지를 하고 UPDATE SQL을 날릴 것이다.

```sql
update theme
set description=?,
    name=?,
    thumbnail_image_url=?
where id = ?
```

JPQL을 실행하는 경우에는 JPQL SELECT를 하기 전에 먼저 UPDATE가 나갈 것이라 예상함.
왜냐하면 JPQL은 DB를 직접 조회하기 때문에, 조회 전에 영속성 컨텍스트의 변경사항을 DB에 맞춰야 하기 때문이다.

### 실제 SQL/동작

명시적으로 flush()를 호출했을 때

```sql
update theme
set description=?,
    name=?,
    thumbnail_image_url=?
where id = ?
```

JPQL을 실행했을 때

```sql
update theme
set description=?,
    name=?,
    thumbnail_image_url=?
where id = ?
```

```sql
select t1_0.id,
       t1_0.description,
       t1_0.name,
       t1_0.thumbnail_image_url
from theme t1_0
where t1_0.id = ?
```

트랜잭션을 종료했을 때

```sql
update theme
set description=?,
    name=?,
    thumbnail_image_url=?
where id = ?
```

### 왜 그런가

flush는 영속성 컨텍스트의 변경사항을 DB에 반영하는 동기화 시점이다.

Theme 객체의 name을 바꿨을 때 그 순간 바로 UPDATE SQL이 나가지는 않았다.
이때는 아직 영속성 컨텍스트 안의 객체 상태만 바뀐 상태이다.

그 뒤 entityManager.flush()를 직접 호출하니 Hibernate가 영속성 컨텍스트 안의 스냅샷과 현재 값을 비교했고,
name 값이 달라진 것을 확인해서 UPDATE SQL을 실행했다.

JPQL을 실행할 때도 UPDATE가 먼저 나갔다.
처음에는 SELECT만 나갈 수도 있다고 생각할 수 있지만, JPQL은 DB에서 데이터를 조회한다.
만약 변경사항을 DB에 반영하지 않고 SELECT를 먼저 실행하면, DB는 아직 예전 값을 가지고 있을 수 있다.
그래서 Hibernate는 JPQL 실행 직전에 flush를 먼저 해서 영속성 컨텍스트와 DB를 맞춘다.

트랜잭션이 끝날 때도 flush가 발생했다.
커밋 전에 영속성 컨텍스트의 변경사항을 DB에 반영해야 최종적으로 변경 내용이 저장될 수 있기 때문이다.

정리하면 flush는 commit 자체는 아니지만, commit 전에 필요한 DB 동기화 과정이다.
그리고 flush는 직접 호출할 때뿐만 아니라 JPQL 실행 직전, 트랜잭션 커밋 시점에도 발생할 수 있다.
