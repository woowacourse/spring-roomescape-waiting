package roomescape.e2e;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class Roomescapee2eTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long reservationId;
    private Integer existingWaiting1Id;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        existingWaiting1Id = RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "user2", "reservationId", reservationId))
                .post("/waitings").then().extract().path("id");
    }

    @Test
    @DisplayName("대기 생성 -> 조회 -> 순번 확인")
    void 대기_순번_확인_테스트() {
        Integer waiting2Id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "reservationId", reservationId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("현미밥"))
                .extract().path("id");

        Integer waiting3Id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥3", "reservationId", reservationId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        // 현미밥3은 3번째로 등록 → turn=3
        RestAssured.given().log().all()
                .queryParam("name", "현미밥3")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(waiting3Id))
                .body("[0].turn", equalTo(3));

        RestAssured.given().log().all()
                .queryParam("name", "user2")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(existingWaiting1Id))
                .body("[0].turn", equalTo(1));
    }

    @Test
    @DisplayName("대기 취소 후 순번 재정렬")
    void 순번_정렬_테스트() {
        Integer waiting2Id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "reservationId", reservationId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        Integer waiting3Id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥3", "reservationId", reservationId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        // turn=1인 user1 취소
        RestAssured.given().log().all()
                .when().delete("/waitings/" + existingWaiting1Id)
                .then().log().all()
                .statusCode(204);

        // 현미밥3의 순번이 3→2로 당겨짐
        RestAssured.given().log().all()
                .queryParam("name", "현미밥3")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(waiting3Id))
                .body("[0].turn", equalTo(2));
    }
}
