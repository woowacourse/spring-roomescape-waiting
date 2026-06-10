package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.DatabaseInitializer;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationWaitingControllerTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 예약을_추가한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        createReservation("로지", reservationDate.toString(), timeId, themeId).statusCode(201);

        createReservationWaiting("브라운", reservationDate, timeId, themeId)
                .statusCode(201)
                .body("id", notNullValue())
                .header("Location", "/waitings/1");
    }

    @Test
    void 예약을_삭제한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출11", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        createReservation("로지", reservationDate.toString(), timeId, themeId).statusCode(201);
        int reservationWaitingId = createReservationWaiting("브라운", reservationDate, timeId, themeId)
                .statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/waitings/" + reservationWaitingId)
                .then().log().all()
                .statusCode(204);
    }

    private int createTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private int createTheme(String name, String description, String thumbnail) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "description", description, "thumbnail", thumbnail))
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private ValidatableResponse createReservationWaiting(String name, LocalDate reservationDate, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "reservationDate", reservationDate, "timeId", timeId, "themeId", themeId))
                .when().post("/waitings")
                .then().log().all();
    }

    private ValidatableResponse createReservation(String name, String date, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().log().all();
    }
}
