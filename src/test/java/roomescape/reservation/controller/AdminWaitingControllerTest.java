package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class AdminWaitingControllerTest {

    @DisplayName("모든 예약대기 목록을 읽어온다.")
    @Test
    void readAllWaiting() {
        String tokenValue = getAdminLoginTokenValue();
        addReservationTime("10:00");
        addTheme();
        addReservation(tokenValue);

        RestAssured.given()
                .cookie("token", tokenValue)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", LocalDate.now().plusDays(1L),
                        "timeId", 1,
                        "themeId", 1
                )).when().post("/reservations/waiting")
                .then();

        RestAssured.given().log().all()
                .cookie("token", tokenValue)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("일반 사용자는 모든 예약대기 목록을 읽어올 수 없다.")
    @Test
    void readAllWaitingByMember() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "member@woowa.com",
                        "password", "12341234",
                        "name", "일반"
                )).when().post("/members")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        String tokenValue = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "member@woowa.com",
                        "password", "12341234"
                )).when().post("/login")
                .then()
                .extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", tokenValue)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("예약대기를 승인한다.")
    @Test
    void approveWaiting() {
        int timeId = addReservationTime("10:00");
        int themeId = addTheme();
        String tokenValue = getAdminLoginTokenValue();
        Map<String, Object> params = Map.of(
                "date", getTomorrow(),
                "timeId", timeId,
                "themeId", themeId
        );
        int waitingId = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", tokenValue)
                .body(params)
                .when().post("reservations/waiting")
                .then().extract().path("id");

        RestAssured.given().log().all()
                .cookie("token", tokenValue)
                .when().post("/admin/reservations/waiting/approve/" + waitingId)
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약대기를 거절한다.")
    @Test
    void denyWaiting() {
        int timeId = addReservationTime("10:00");
        int themeId = addTheme();
        String tokenValue = getAdminLoginTokenValue();
        Map<String, Object> params = Map.of(
                "date", getTomorrow(),
                "timeId", timeId,
                "themeId", themeId
        );
        int waitingId = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", tokenValue)
                .body(params)
                .when().post("reservations/waiting")
                .then().extract().path("id");

        RestAssured.given().log().all()
                .cookie("token", tokenValue)
                .when().delete("/admin/reservations/waiting/deny/" + waitingId)
                .then().log().all()
                .statusCode(204);
    }

    private String getAdminLoginTokenValue() {
        Map<String, String> adminLoginParams = Map.of("email", "admin@woowa.com", "password", "12341234");
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(adminLoginParams)
                .when().post("/login")
                .then()
                .extract().cookie("token");
    }

    private LocalDate getTomorrow() {
        return LocalDate.now().plusDays(1L);
    }

    private int addReservationTime(final String timeValue) {
        Map<String, String> timeParams = Map.of("startAt", timeValue);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/times")
                .then().extract().path("id");
    }

    private int addTheme() {
        Map<String, String> themeParams = Map.of(
                "name", "테마1", "description", "테마1", "thumbnail", "www.m.com"
        );
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/themes")
                .then().extract().path("id");
    }

    private void addReservation(final String tokenValue) {
        RestAssured.given()
                .cookie("token", tokenValue)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", LocalDate.now().plusDays(1L),
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/reservations")
                .then();
    }
}
