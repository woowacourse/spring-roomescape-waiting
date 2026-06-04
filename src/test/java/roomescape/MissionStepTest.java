package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.controller.ReservationController;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Autowired
    private ReservationController reservationController;

    @Test
    @DisplayName("시간 관리 API")
    void timeManagementApi() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        int timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("times.find { it.id == " + timeId + " }.startAt", is("10:00"));

        RestAssured.given().log().all()
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("중복된 예약 시간을 추가하면 conflict를 반환한다.")
    void createDuplicatedReservationTime_returnsConflict() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("예약과 시간을 연결한다.")
    void connectReservationWithTime() {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", "10:00");

        Map<String, String> theme = new HashMap<>();
        theme.put("name", "이름");
        theme.put("description", "내용");
        theme.put("thumbnail", "https://example.com/theme.png");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        int timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        int themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("time.id", is(timeId))
                .body("theme.id", is(themeId));

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.find { it.time.id == " + timeId + " }.theme.id", is(themeId));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 후 예약하면 해당 시간이 예약 불가가 된다.")
    void reserveAfterFindingAvailableTime_makesTimeUnavailable() {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", "23:00");

        Map<String, String> theme = new HashMap<>();
        theme.put("name", "예약 가능 시간 테스트");
        theme.put("description", "예약 가능 시간 테스트용 테마");
        theme.put("thumbnail", "https://example.com/availability-theme.png");
        String date = LocalDate.now().plusDays(1).toString();

        int timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        int themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .when().get("/times/availability?date=" + date + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("availableTimes.find { it.id == " + timeId + " }.isAvailable", is(true));

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", date);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times/availability?date=" + date + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("availableTimes.find { it.id == " + timeId + " }.isAvailable", is(false));
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 다른 사용자가 예약하면 내 예약 조회에 대기 순번이 포함된다.")
    void createWaitingReservationAndFindMyReservations() {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", "22:30");

        Map<String, String> theme = new HashMap<>();
        theme.put("name", "대기 신청 테스트");
        theme.put("description", "대기 신청 테스트용 테마");
        theme.put("thumbnail", "https://example.com/waiting-theme.png");

        String date = LocalDate.now().plusDays(1).toString();

        int timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        int themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", date);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", nullValue())
                .body("waitingRank", nullValue());

        reservation.put("name", "레아");

        int waitingReservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", nullValue())
                .body("waitingRank", nullValue())
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .when().get("/reservations?name=레아")
                .then().log().all()
                .statusCode(200)
                .body("reservations[0].status", is("WAITING"))
                .body("reservations[0].waitingRank", is(1));

        RestAssured.given().log().all()
                .when().delete("/reservations/" + waitingReservationId + "?name=레아")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations?name=레아")
                .then().log().all()
                .statusCode(200)
                .body("reservations[0].status", is("CANCELED"))
                .body("reservations[0].waitingRank", nullValue());
    }

    @Test
    @DisplayName("컨트롤러는 JdbcTemplate을 직접 의존하지 않는다.")
    void controllerDoesNotDependOnJdbcTemplate() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
