package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.TokenRequest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationControllerTest {

    private static final String EMAIL = "testDB@email.com";
    private static final String PASSWORD = "1234";

    @LocalServerPort
    private int port;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        accessToken = RestAssured
                .given().log().all()
                .body(new TokenRequest(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @DisplayName("로그인 정보와 예약 요청 정보를 이용하여 예약한다.")
    @Test
    void given_when_create_reservationByClient_then_statusCodeIsOk() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ReservationRequest request = new ReservationRequest(
                tomorrow, 1L, 1L
        );

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약 내역을 조회하면 200 OK를 반환한다.")
    @Test
    void readReservations() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(22));
    }

    /* 예약 현황
    testdb@email.com 3개
    testdb2@email.com 4개
    */
    @DisplayName("로그인 된 유저의 예약 내역을 조회하면 200을 응답한다.")
    @Test
    void given_when_find_my_reservations_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(12));
    }

    @DisplayName("예약 삭제에 성공하면 204 No-Content를 반환한다.")
    @Test
    void deleteReservation() {
        RestAssured.given().log().all()
                .when().delete("/reservations/{id}", 1)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예약 대기에 성공하면 201 Created를 반환한다.")
    @Test
    void createPendingReservation() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ReservationRequest request = new ReservationRequest(
                tomorrow, 1L, 1L
        );

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/pending")
                .then().statusCode(201);
    }
}
