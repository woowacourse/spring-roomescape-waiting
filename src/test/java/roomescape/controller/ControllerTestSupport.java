package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.DatabaseInitializer;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public abstract class ControllerTestSupport {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    protected int createMember(String name) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name))
                .when().post("/members")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    protected int createTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    protected int createTheme(String name, String description, String thumbnail) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "description", description, "thumbnail", thumbnail))
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    protected ValidatableResponse createReservation(int memberId, String date, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("memberId", memberId, "date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().log().all();
    }

    protected ValidatableResponse createReservationWaiting(int memberId, LocalDate date, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("memberId", memberId, "reservationDate", date.toString(), "timeId", timeId, "themeId", themeId))
                .when().post("/waitings")
                .then().log().all();
    }
}
