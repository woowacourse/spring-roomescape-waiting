package roomescape.waiting.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationWaitingControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 예약이_없는_시간에_대기_생성_예외발생() {
        Map<String, Object> params = Map.of(
                "name", "초록",
                "themeId", 1L,
                "date", LocalDate.now().plusDays(1).toString(),
                "timeId", 1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservation-waitings")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("RESERVATION_NOT_EXISTS"))
                .body("message", equalTo("예약이 존재하지 않습니다."));
    }

    @Test
    void 본인_예약에_대기_예외발생() {
        String name = "초록";
        String date = LocalDate.now().plusDays(1).toString();
        createReservation(name, 1L, date, 1L);
        Map<String, Object> params = Map.of(
                "name", name,
                "themeId", 1L,
                "date", date,
                "timeId", 1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservation-waitings")
                .then().log().all()
                .statusCode(409)
                .body("code", equalTo("DUPLICATED_RESERVATION"))
                .body("message", equalTo("본인 예약에 대기를 신청할 수 없습니다."));
    }

    @Test
    void 과거_예약에_대기_예외발생() {
        Map<String, Object> params = Map.of(
                "name", "초록",
                "themeId", 1L,
                "date", "2026-05-23",
                "timeId", 3L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservation-waitings")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("CANNOT_CANCEL_PAST_RESERVATION_WAITING"))
                .body("message", equalTo("이미 지난 시간의 예약 대기를 취소할 수 없습니다."));
    }

    @Test
    void 날짜가_누락된_대기_요청_예외발생() {
        Map<String, Object> params = Map.of(
                "name", "초록",
                "themeId", 1L,
                "timeId", 1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservation-waitings")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"))
                .body("message", equalTo("요청 값이 올바르지 않습니다."));
    }

    @Test
    void 잘못된_테마로_대기_요청_예외발생() {
        Map<String, Object> params = Map.of(
                "name", "초록",
                "themeId", 999L,
                "date", LocalDate.now().plusDays(1).toString(),
                "timeId", 1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservation-waitings")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("THEME_NOT_FOUND"))
                .body("message", equalTo("테마를 찾을 수 없습니다."));
    }


    @Test
    void 대기_생성_응답의_Location으로_생성된_대기_조회_성공() {
        String name = "초록";
        String date = LocalDate.now().plusDays(1).toString();
        createReservation("브라운", 1L, date, 1L);
        Map<String, Object> params = Map.of(
                "name", name,
                "themeId", 1L,
                "date", date,
                "timeId", 1L
        );

        String location = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservation-waitings")
                .then().log().all()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("name", is(name))
                .body("themeId", is(1))
                .body("date", is(date))
                .body("time.id", is(1))
                .body("waitingNumber", is(1))
                .extract()
                .header("Location");

        RestAssured.given().log().all()
                .when().get(location)
                .then().log().all()
                .statusCode(200)
                .body("name", is(name))
                .body("themeId", is(1))
                .body("date", is(date))
                .body("time.id", is(1))
                .body("waitingNumber", is(1));
    }

    private Integer createReservation(String name, Long themeId, String date, Long timeId) {
        Map<String, Object> params = Map.of(
                "name", name,
                "themeId", themeId,
                "date", date,
                "timeId", timeId
        );

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
