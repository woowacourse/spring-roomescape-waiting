## 관찰: DirtyChecking

### 시도한 코드

Theme객체를 DB에 저장한 뒤, Theme의 changeName() 이라는 메서드를 사용하여
객체 내부의 속성을 바꾸고 flush()를 호출함.

### 예측한 SQL/동작

Dirty Checking을 통해서 Hibernate가 영속성 컨텍스트에 올라온 Theme이라는 객체를
보고있다가 update 쿼리를 날려서 변경된 객체 내부 속성을 DB에 함께 반영함.

```sql
UPDATE theme
SET description,
    name,
    thumbnail_image_url
WHERE id = ?
```

### 실제 SQL/동작

```sql
update theme
set description=?,
    name=?,
    thumbnail_image_url=?
where id = ?
```

### 왜 그런가

Hibernate는 영속성 컨텍스트에 올라온 객체를 계속 보다가 변경사항이 일어날 때 이를 DB에 반여해줌

Theme 객체는 영속성 상태이기 때문에 Hibernate는 조회 시점의 스냅샷과 flush 시점의 값을 비교한다.
name 값이 달라졌기 때문에 dirty checking에 의해서 UPDATE SQL문이 실행된다.

단, name을 바꿨는데, description, thumbnail_image_url까지 UPDATE 문에 포함되었다.
이는 Hibernate가 기본적으로 변경된 컬럼만이 아니라 엔티티의 갱신 가능한 컬럼들을 함께
UPDATE 문에 포함하기 때문이다. 변경 컬럼만 UPDATE하고 싶다면 별도 설정이 필요하다.
