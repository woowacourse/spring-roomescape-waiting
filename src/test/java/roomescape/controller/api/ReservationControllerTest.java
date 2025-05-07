package roomescape.controller.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("/reservations 요청 시 예약 정보 조회")
    void readReservations() {
        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(3));
    }

    @Test
    @DisplayName("예약 관리 페이지 내에서 예약 추가")
    void createReservation() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithReservation())
            .when().post("/reservations/admin")
            .then().log().all()
            .statusCode(201)
            .body("name", is("사나"));

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(4));
    }

    @Test
    @DisplayName("예약 관리 페이지 내에서 예약 삭제")
    void deleteReservation() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithReservation())
            .when().post("/reservations/admin")
            .then().log().all()
            .statusCode(201)
            .body("id", is(4));

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(4));

        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(3));
    }

    private Map<String, Object> getTestParamsWithReservation() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('15:40')");
        jdbcTemplate.update(
            "INSERT INTO theme(name, description, thumbnail) VALUES('1단계', '탈출하기', 'http://~')");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "사나");
        params.put("date", "2024-04-26");
        params.put("timeId", 1);
        params.put("themeId", 1);
        return params;
    }
}
