# 🚀 3단계: 시간 관리
## 요구사항
관리자가 매번 예약 시간을 텍스트로 직접 입력해 번거롭고 실수가 나는 상황이다. 정해진 시간 슬롯을 관리자가 선택해서 쓸 수 있도록 시간 관리 기능을 추가하고 예약과 시간을 연결한다.

⚠️ 새로운 테스트 도구나 기법(Spring Boot Test, Mock, RestAssured 추가 활용 등)을 도입하지 않고 레벨1에서 학습했던 JUnit만 활용한 단위 테스트에 집중한다. 요구사항에서 RestAssured가 주어진 경우 그대로 사용하되, 그 위에 새 테스트 기법을 쌓지 않는다.

## 시간 관리 기능 추가
테이블 추가:

```sql
CREATE TABLE reservation_time (
id       BIGINT       NOT NULL AUTO_INCREMENT,
start_at VARCHAR(255) NOT NULL,
PRIMARY KEY (id)
);
```

시간 API:

| 기능 | 메서드 / URL | 요청 본문 | 응답 |
| :--- | :--- | :--- | :--- |
| **시간 추가** | `POST /times` | `{startAt}` | `{id, startAt}` |
| **시간 조회** | `GET /times` | — | `[{id, startAt}, ...]` |
| **시간 삭제** | `DELETE /times/{id}` | — | `200 OK` |

이 소단계에서는 기존 예약 테이블의 time 컬럼을 그대로 유지한다. 예약과 예약 시간을 연동하는 것은 다음 소단계에서 진행한다.

## 예약과 시간 연결
- reservation 테이블의 time 컬럼을 time_id(FK → reservation_time.id)로 변경

```sql
CREATE TABLE reservation (
id      BIGINT       NOT NULL AUTO_INCREMENT,
name    VARCHAR(255) NOT NULL,
date    VARCHAR(255) NOT NULL,
time_id BIGINT,
PRIMARY KEY (id),
FOREIGN KEY (time_id) REFERENCES reservation_time (id)
);
```

- Reservation 클래스의 time 필드를 String → ReservationTime 객체로 변경
- 예약 추가 요청 본문: time → timeId
- 예약 조회 응답: time을 객체로 ({id, startAt})

변경된 예약 추가 요청/응답 예시:

```java
POST /reservations HTTP/1.1
Content-Type: application/json

{
"date": "2023-08-05",
"name": "브라운",
"timeId": 1
}
```

```java
HTTP/1.1 200
Content-Type: application/json

{
"id": 1,
"name": "브라운",
"date": "2023-08-05",
"time": {
"id": 1,
"startAt": "10:00"
}
}
```

## 요구사항 테스트
아래 테스트가 모두 통과하면 단계 3 완료.

```java
@Test
void 시간_관리_API() {
Map<String, String> params = new HashMap<>();
params.put("startAt", "10:00");

    RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/times")
            .then().log().all()
            .statusCode(200);

    RestAssured.given().log().all()
            .when().get("/times")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1));

    RestAssured.given().log().all()
            .when().delete("/times/1")
            .then().log().all()
            .statusCode(200);
}

@Test
void 예약과_시간_연결() {
Map<String, Object> reservation = new HashMap<>();
reservation.put("name", "브라운");
reservation.put("date", "2023-08-05");
reservation.put("timeId", 1);

    RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(reservation)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(200);

    RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1));
}
```

## 힌트
막힐 때 참고. 처음부터 모두 읽을 필요는 없다.

### 어디서 시작할지 모르겠다면
- 앞선 단계에서 완성한 예약 기능 코드를 참고해 진행한다.
- 시간 관리 기능은 API부터 데이터베이스까지 전 구간을 구현하면서 복습하는 목적으로 진행한다.

### 도메인 의존 관계 처리
- 도메인 간에 의존 관계가 형성될 때의 처리를 연습하는 단계다.
- 클래스 간의 의존 관계 및 테이블 간의 의존 관계를 학습한다.
- 의존관계가 추가되면서 수정되는 쿼리 및 로직을 확인하고 영향을 받는 부분을 파악한다.
- 여러 테이블에서 일치하는 데이터를 조회할 때 inner join 키워드를 활용한다. 스스로 작성할 수 있도록 연습한다.

### 조회 쿼리 예시 (inner join 활용)

```sql
SELECT
r.id as reservation_id,
r.name,
r.date,
t.id as time_id,
t.start_at as time_value
FROM reservation as r
INNER JOIN reservation_time as t
ON r.time_id = t.id
```

### 키워드
- inner join, 객체 의존, 외래키
