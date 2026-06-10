package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.repository.ReservationWaitingUpdateDao;
import roomescape.service.ReservationService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserReservationTest {

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean
    private ReservationWaitingUpdateDao reservationWaitingUpdateDao;

    @Test
    void 사용자_예약__API() {
        Map<String, String> themes = new HashMap<>();
        themes.put("name", "무서운 이야기");
        themes.put("description", "공포");
        themes.put("url", "http://example.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themes)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, String> times = new HashMap<>();
        times.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(times)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, String> reservations = new HashMap<>();
        reservations.put("name", "브라운");
        reservations.put("date", "2026-08-04");
        reservations.put("timeId", "1");
        reservations.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 예약_정상_흐름_테스트() {
        Map<String, String> themes = new HashMap<>();
        themes.put("name", "무서운 이야기");
        themes.put("description", "공포");
        themes.put("url", "http://example.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themes)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, String> times1 = new HashMap<>();
        times1.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(times1)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, String> times2 = new HashMap<>();
        times2.put("startAt", "11:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(times2)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times?date=2026-06-04&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));

        Map<String, String> reservations = new HashMap<>();
        reservations.put("name", "브라운");
        reservations.put("date", "2026-07-04");
        reservations.put("timeId", "1");
        reservations.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times?date=2026-07-04&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].isAvailable", is(false))
                .body("[1].isAvailable", is(true));
    }

    @Test
    void 예약_날짜_시간_변경() {
        Map<String, String> theme = new HashMap<>();
        theme.put("name", "무서운 이야기");
        theme.put("description", "공포");
        theme.put("url", "http://example.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, String> time1 = new HashMap<>();
        time1.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time1)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, String> time2 = new HashMap<>();
        time2.put("startAt", "11:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time2)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> update = new HashMap<>();
        update.put("name", "브라운");
        update.put("date", "2026-08-06");
        update.put("timeId", 2);
        update.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(update)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 중복_예약시_409를_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("errorCode", is("DUPLICATE_RESERVATION"))
                .body("message", is("이미 예약된 시간입니다."));
    }

    @Test
    void 이미_예약된_시간으로_변경시_409를_반환한다() {
        createTheme();
        createTime("10:00");
        createTime("11:00");

        Map<String, Object> reservation1 = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation1)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> reservation2 = createReservationBody("네오", "2026-08-05", 2, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation2)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> update = createReservationBody("브라운", "2026-08-05", 2, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(update)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("errorCode", is("DUPLICATE_RESERVATION"))
                .body("message", is("이미 예약된 시간입니다."));
    }

    @Test
    void 존재하지_않는_예약_변경시_404를_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> update = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(update)
                .when().patch("/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", is("RESERVATION_NOT_FOUND"))
                .body("message", is("999번 예약이 존재하지 않습니다."));
    }

    @Test
    void 존재하지_않는_시간으로_예약시_404를_반환한다() {
        createTheme();

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 999, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", is("TIME_NOT_FOUND"))
                .body("message", is("999번 예약 시간을 찾을 수 없습니다."));
    }

    @Test
    void 존재하지_않는_테마로_예약시_404를_반환한다() {
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 999);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", is("THEME_NOT_FOUND"))
                .body("message", is("999번 테마를 찾을 수 없습니다."));
    }

    @Test
    void 과거_날짜로_예약시_400을_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2020-01-01", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("INVALID_DATE_OR_TIME"))
                .body("message", is("이미 지난 날짜이거나 시간입니다."));
    }

    @Test
    void 과거_날짜로_변경시_400을_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> update = createReservationBody("브라운", "2020-01-01", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(update)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("INVALID_DATE_OR_TIME"))
                .body("message", is("이미 지난 날짜이거나 시간입니다."));
    }

    @Test
    void 빈_이름으로_예약시_400을_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("", "2026-08-05", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("INVALID_INPUT"))
                .body("message", is("[name] 필드가 비어있습니다."));
    }

    @Test
    void 예약_취소_후_대기자가_예약자로_승격된다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> waiting = createReservationBody("네오", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(waiting)
                .when().post("/reservations/waitings").then().statusCode(201);

        RestAssured.given()
                .when().delete("/reservations/1").then().statusCode(204);

        RestAssured.given()
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("네오"));
    }

    @Test
    void 대기_있는_예약_취소시_예약삭제_예약생성_대기삭제가_함께_진행된다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> waiting = createReservationBody("네오", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(waiting)
                .when().post("/reservations/waitings").then().statusCode(201);

        RestAssured.given()
                .when().delete("/reservations/1").then().statusCode(204);

        RestAssured.given()
                .when().get("/reservations/1")
                .then().statusCode(404);

        RestAssured.given()
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("네오"));

        RestAssured.given()
                .when().get("reservations/waitings")
                .then().statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 존재하지_않는_예약_취소시_데이터가_그대로_유지된다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> waiting = createReservationBody("네오", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(waiting)
                .when().post("/reservations/waitings").then().statusCode(201);

        RestAssured.given()
                .when().delete("/reservations/999")
                .then().statusCode(404);

        RestAssured.given()
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("브라운"));

        RestAssured.given()
                .when().get("/reservations/waitings")
                .then().statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 대기_삭제_중_예외가_발생하면_예약_삭제와_대기_승격이_모두_롤백된다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> waiting = createReservationBody("네오", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(waiting)
                .when().post("/reservations/waitings").then().statusCode(201);

        doThrow(new IllegalStateException("예외 발생!!!")).when(reservationWaitingUpdateDao).delete(1L);

        assertThatThrownBy(() -> reservationService.delete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("예외 발생!!!");

        RestAssured.given()
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("브라운"));

        RestAssured.given()
                .when().get("/reservations/waitings")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("네오"));
    }

    @Test
    void 본인이_아닌_사람이_대기_취소하면_데이터가_그대로_유지된다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> waiting = createReservationBody("네오", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(waiting)
                .when().post("/reservations/waitings").then().statusCode(201);

        RestAssured.given()
                .when().delete("/reservations/waitings/1?name=다른사람")
                .then().statusCode(400);

        RestAssured.given()
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("브라운"));

        RestAssured.given()
                .when().get("/reservations/waitings")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("네오"));
    }

    private void createTheme() {
        Map<String, String> theme = new HashMap<>();
        theme.put("name", "무서운 이야기");
        theme.put("description", "공포");
        theme.put("url", "http://example.com");
        RestAssured.given().contentType(ContentType.JSON).body(theme)
                .when().post("/admin/themes").then().statusCode(201);
    }

    private void createTime(String startAt) {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", startAt);
        RestAssured.given().contentType(ContentType.JSON).body(time)
                .when().post("/admin/times").then().statusCode(201);
    }

    private Map<String, Object> createReservationBody(String name, String date, int timeId, int themeId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("date", date);
        body.put("timeId", timeId);
        body.put("themeId", themeId);
        return body;
    }
}
