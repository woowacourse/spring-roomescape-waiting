package roomescape.reservation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 전체예약_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(15));
    }

    @Test
    void 이름으로_전체예약_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/reservations/list?name=로치")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(13));
    }

    @Test
    void 이름으로_예약과_예약_대기_조회_성공() {
        String name = "초록";
        String date = LocalDate.now().plusDays(1).toString();
        Integer reservationId = createReservation(name, 1L, date, 1L);
        createReservation("브라운", 2L, date, 2L);
        Integer waitingId = createWaiting(name, 2L, date, 2L);

        RestAssured.given().log().all()
                .when().get("/reservations/list?name=" + name)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].id", is(reservationId))
                .body("[0].name", is(name))
                .body("[0].themeName", is("은하수"))
                .body("[0].date", is(date))
                .body("[0].startAt", is("10:00"))
                .body("[0].waitingNumber", nullValue())
                .body("[1].id", is(waitingId))
                .body("[1].name", is(name))
                .body("[1].themeName", is("지구"))
                .body("[1].date", is(date))
                .body("[1].startAt", is("11:00"))
                .body("[1].waitingNumber", is(1));
    }

    @Test
    void 시간존재예약_추가_성공() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "초록");
        params.put("themeId", 2L);
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 7L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 시간없음예약_추가_실패() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "초록");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 15L);
        params.put("themeId", 2L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("RESERVATION_TIME_NOT_FOUND"))
                .body("message", equalTo("예약 시간을 찾을 수 없습니다."));
    }

    @Test
    void 예약삭제_성공() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 이름_빈칸으로_예약추가_예외발생() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", " ");
        params.put("themeId", 2L);
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 7L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"))
                .body("message", equalTo("요청 값이 올바르지 않습니다."));
    }

    @Test
    void 날짜_형식_오류로_예약추가_예외발생() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "워넬");
        params.put("themeId", 2L);
        params.put("date", "2026/05/12");
        params.put("timeId", 7L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_DATE_FORMAT"))
                .body("message", equalTo("날짜 형식이 잘못되었습니다. (yyyy-MM-dd)"));
    }

    @Test
    void 본인_예약_변경_성공() {
        String originDate = LocalDate.now().plusDays(1).toString();
        String changedDate = LocalDate.now().plusDays(2).toString();
        Integer reservationId = createReservation("로치", 1L, originDate, 1L);
        Map<String, Object> params = Map.of(
                "name", "로치",
                "themeId", 1L,
                "date", changedDate,
                "timeId", 2L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200)
                .body("id", is(reservationId))
                .body("name", is("로치"))
                .body("date", is(changedDate))
                .body("time.id", is(2));
    }

    @Test
    void 다른_사람의_예약은_변경할_수_없다() {
        Map<String, Object> params = Map.of(
                "name", "브라운",
                "themeId", 1L,
                "date", LocalDate.now().plusDays(1).toString(),
                "timeId", 2L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(403)
                .body("code", is("CANNOT_MODIFY_OTHER_RESERVATION"));
    }

    @Test
    void 이미_예약된_시간으로는_변경할_수_없다() {
        String originDate = LocalDate.now().plusDays(1).toString();
        String duplicatedDate = LocalDate.now().plusDays(2).toString();
        Integer reservationId = createReservation("로치", 1L, originDate, 1L);
        createReservation("브라운", 1L, duplicatedDate, 3L);
        Map<String, Object> params = Map.of(
                "name", "로치",
                "themeId", 1L,
                "date", duplicatedDate,
                "timeId", 3L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(409)
                .body("code", is("RESERVATION_ALREADY_EXISTS"));
    }

    @Test
    void 본인_예약_삭제_성공() {
        Integer reservationId = createReservation("로치", 1L, LocalDate.now().plusDays(1).toString(), 1L);

        RestAssured.given().log().all()
                .when().delete("/reservations/my/" + reservationId + "?name=로치")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 본인이_아닌_예약_삭제_실패() {
        RestAssured.given().log().all()
                .when().delete("/reservations/my/1?name=브라운")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void 없는_예약_삭제_실패() {
        RestAssured.given().log().all()
                .when().delete("/reservations/my/17?name=로치")
                .then().log().all()
                .statusCode(404);
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

    private Integer createWaiting(String name, Long themeId, String date, Long timeId) {
        Map<String, Object> params = Map.of(
                "name", name,
                "themeId", themeId,
                "date", date,
                "timeId", timeId
        );

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservation-waitings")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
