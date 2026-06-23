package roomescape.reservationwaiting.controller;

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
public class ReservationWaitingControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long futureReservationId;
    private Integer waiting1Id;
    private Integer waiting2Id;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        futureReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        // 순번 테스트를 위해 대기 2개 미리 생성 (user1=1번, user3=2번)
        waiting1Id = RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "user2", "reservationId", futureReservationId))
                .post("/waitings").then().extract().path("id");

        waiting2Id = RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "user3", "reservationId", futureReservationId))
                .post("/waitings").then().extract().path("id");
    }

    @Test
    @DisplayName("예약 대기 생성 성공")
    void 예약_대기_생성_성공() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "reservationId", futureReservationId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("현미밥"))
                .body("date", equalTo("2099-12-01"));
    }

    @Test
    @DisplayName("예약 대기 삭제 성공")
    void 예약_대기_삭제_성공() {
        RestAssured.given().log().all()
                .when().delete("/waitings/" + waiting2Id)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("이름으로 예약 대기 조회")
    void 예약_대기_조회_성공() {
        RestAssured.given().log().all()
                .queryParam("name", "user3")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(waiting2Id))
                .body("[0].turn", equalTo(2));
    }
}
