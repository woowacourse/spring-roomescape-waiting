# 🚀 1단계: 웹 요청-응답
## 요구사항
- 방탈출 카페 관리자가 전화·현장 예약을 직접 등록·관리하는 상황에 필요한 예약 관리 API를 만든다.
- 별도의 데이터베이스 없이 메모리(List + AtomicLong)로 예약 상태를 관리한다.
- 서버를 재시작하면 데이터는 모두 사라진다.
- ⚠️ 화면 없음. 브라우저 화면은 만들지 않는다. API 동작 확인 방법(테스트, HTTP 클라이언트 등)은 스스로 찾는다.

## 예약 CRUD API
| 기능 | 메서드 / URL | 요청 본문 | 응답 |
| :--- | :--- | :--- | :--- |
| **예약 조회** | `GET /reservations` | — | `[{id, name, date, time}, ...]` |
| **예약 추가** | `POST /reservations` | `{name, date, time}` | `{id, name, date, time}` |
| **예약 삭제** | `DELETE /reservations/{id}` | — | `200 OK` |

- 예약 추가 요청·응답 예시
```java
POST /reservations HTTP/1.1
Content-Type: application/json

{
    "name": "브라운",
    "date": "2023-08-05",
    "time": "15:40"
}
```

```java
HTTP/1.1 200
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2023-08-05",
    "time": "15:40"
}
```

## 요구사항 테스트
아래 테스트가 모두 통과하면 단계 1 완료.

```java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Test
    void 예약_조회() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0)); // 아직 생성 요청이 없으니 0개
    }

    @Test
    void 예약_추가_및_삭제() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2023-08-05");
        params.put("time", "15:40");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("id", is(1));

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }
}
```

## 힌트
막힐 때 참고. 처음부터 모두 읽을 필요는 없다.

### 어디서 시작할지 모르겠다면
- 현재 프로젝트는 웹 관련 gradle 의존성이 추가되어있지 않다. 필요한 의존성을 찾아 추가한다.
- 학습 테스트에서 각 어노테이션이 어떤 역할을 하는지 먼저 익힌다.

### 요청·응답 처리
- @ResponseBody, controller method return type, DTO
- @RequestBody, @PathVariable

### 메모리 저장소·식별자 발급 예시
```java
public class ReservationController {
    private List<Reservation> reservations = new ArrayList<>();
    private AtomicLong index = new AtomicLong(1);

    // index.incrementAndGet() 활용 예시
    reservations.add(new Reservation(index.incrementAndGet(), "브라운", "2023-01-01", "10:00"));
}
```
