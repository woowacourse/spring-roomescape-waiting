package roomescape.reservation.controller;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class WaitingControllerTest {

    @DisplayName("예약 대기를 추가한다.")
    @Test
    void createReservationWaiting() {
        int timeId = addReservationTime("10:00");
        int themeId = addTheme();
        String tokenValue = getAdminLoginTokenValue();
        Map<String, Object> waitingParams = Map.of(
                "date", getTomorrow(),
                "timeId", timeId,
                "themeId", themeId
        );

        RestAssured.given().log().all()
                .cookie("token", tokenValue)
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약대기를 삭제한다.")
    @Test
    void deleteReservationWaiting() {
        int timeId = addReservationTime("10:00");
        int themeId = addTheme();
        String tokenValue = getAdminLoginTokenValue();
        Map<String, Object> reservationParams = Map.of(
                "date", getTomorrow(),
                "timeId", timeId,
                "themeId", themeId
        );

        int waitingId = RestAssured.given()
                .cookie("token", tokenValue)
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations/waiting")
                .then().extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/reservations/waiting/" + waitingId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 예약대기를 삭제할 경우 NOT_FOUND 반환한다.")
    @Test
    void deleteNonExistsReservationWaiting() {
        RestAssured.given().log().all()
                .when().delete("/reservations/waiting/0")
                .then().log().all()
                .statusCode(404);
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
}
