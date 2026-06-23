package roomescape.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
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
class ReservationCancelPromotionE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')"); // id=1
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)"); // id=1
    }

    @Test
    @DisplayName("예약자가 취소하면 1번 대기자가 예약으로 승격되고, 2번 대기자의 순번은 1이 된다")
    void 예약_취소_시_대기자_승격_시나리오() {
        Integer reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "user1", "date", "2099-12-01", "timeId", 1, "themeId", 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        Integer waiting1Id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "user2", "reservationId", reservationId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        Integer waiting2Id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "user3", "reservationId", reservationId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        List<Map<String, Object>> user1Reservations = RestAssured.given().log().all()
                .queryParam("name", "user1")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertThat(user1Reservations).isEmpty();

        List<Map<String, Object>> user2Reservations = RestAssured.given().log().all()
                .queryParam("name", "user2")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertThat(user2Reservations).hasSize(1);
        assertThat(user2Reservations.get(0).get("name")).isEqualTo("user2");

        List<Map<String, Object>> user2Waitings = RestAssured.given().log().all()
                .queryParam("name", "user2")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertThat(user2Waitings).isEmpty();

        List<Map<String, Object>> user3Waitings = RestAssured.given().log().all()
                .queryParam("name", "user3")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertThat(user3Waitings).hasSize(1);
        assertThat(user3Waitings.get(0).get("turn")).isEqualTo(1);
    }
}