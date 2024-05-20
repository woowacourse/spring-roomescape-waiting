package roomescape.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.TokenRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminReservationTest {

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

    @DisplayName("reservation 페이지 조회 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetReservations_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(22));
    }

    @DisplayName("reservation 페이지에 새로운 예약 정보를 추가, 조회, 삭제할 수 있다.")
    @Test
    void given_when_saveAndDeleteReservations_then_statusCodeIsOkay() {
        ReservationRequest request =
                new ReservationRequest(1L, LocalDate.parse("2999-12-31"), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(23));

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("등록되지 않은 시간으로 예약하는 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveNotExistTimeId_then_statusCodeIsBadRequest() {
        ReservationRequest request =
                new ReservationRequest(1L, LocalDate.parse("2999-12-31"), 500L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("과거 날짜로 예약하는 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"2011-02-09", "1123-12-12"})
    void given_when_savePastDate_then_statusCodeIsBadRequest(String invalidDate) {
        ReservationRequest request =
                new ReservationRequest(1L, LocalDate.parse(invalidDate), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 지나간 날짜와 시간으로 예약할 수 없습니다"));
    }

    @DisplayName("날짜가 비어있는 채 예약한다면 400 오류를 반환한다.")
    @Test
    void given_when_saveNullDate_then_statusCodeIsBadRequest() {
        ReservationRequest request =
                new ReservationRequest(1L, null, 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 예약 날짜는 비워둘 수 없습니다."));
    }

    @DisplayName("시간이 비어있는 채 예약하는 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveInvalidTimeId_then_statusCodeIsBadRequest() {
        ReservationRequest request =
                new ReservationRequest(1L, LocalDate.parse("2100-05-05"), null, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 예약 시간은 비워둘 수 없습니다."));
    }

    @DisplayName("테마가 비어있는 채 예약하는 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveInvalidThemeId_then_statusCodeIsBadRequest() {
        ReservationRequest request =
                new ReservationRequest(1L, LocalDate.parse("2100-05-05"), 1L, null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 테마는 비워둘 수 없습니다."));
    }

    @DisplayName("필터링 된 예약 내역을 조회하면 200 OK와 응답을 반환한다.")
    @Test
    void findAllByMemberAndThemeAndPeriod() {
        LocalDate from = LocalDate.now().minusDays(3);
        LocalDate to = LocalDate.now();
        String uriPath = "/admin/reservations?memberId=1&themeId=5&dateFrom=" + from + "&dateTo=" + to;

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .when().get(uriPath)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }
}
