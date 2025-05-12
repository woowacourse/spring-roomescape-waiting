package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.infrastructure.JwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String normalAccessToken;

    @BeforeEach
    void initialize() {
        String normalMemberEmail = "normal1@normal.com";
        normalAccessToken = jwtTokenProvider.createToken(normalMemberEmail);
    }

    @Test
    @DisplayName("/reservations 요청 시 예약 정보 조회")
    void readReservations() {
        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(21));
    }

    @Test
    @DisplayName("예약 관리 페이지 내에서 예약 추가")
    void createReservation() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", normalAccessToken)
            .body(getTestParamsWithReservation())
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("member.name", is("일반1"));

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(22));
    }


    @Test
    @DisplayName("예약 관리 페이지 내에서 예약 삭제")
    void deleteReservation() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", normalAccessToken)
            .body(getTestParamsWithReservation())
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("id", is(22));

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(22));

        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(21));
    }

    private Map<String, Object> getTestParamsWithReservation() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('15:40')");
        jdbcTemplate.update(
            "INSERT INTO theme(name, description, thumbnail) VALUES('1단계', '탈출하기', 'http://~')");

        Map<String, Object> params = new HashMap<>();
        params.put("memberId", 1);
        params.put("date", "2030-04-26");
        params.put("timeId", 1);
        params.put("themeId", 1);
        return params;
    }
}
