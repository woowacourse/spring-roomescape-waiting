package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.DatabaseInitializer;

import static org.hamcrest.Matchers.hasItem;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminReservationControllerTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 전체_예약을_조회한다() {
        int timeId1 = createTime("10:00");
        int timeId2 = createTime("11:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId1, themeId).statusCode(201);
        createReservation("로지", LocalDate.now().plusDays(1).toString(), timeId2, themeId).statusCode(201);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("브라운"));
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

    private ValidatableResponse createReservation(String name, String date, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().log().all();
    }
}
