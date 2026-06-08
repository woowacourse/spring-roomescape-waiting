package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.DatabaseInitializer;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class ThemeControllerTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 테마를_추가한다() {
        createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs")
                .statusCode(201)
                .body("id", notNullValue())
                .header("Location", "/admin/themes/1");
    }

    @ParameterizedTest
    @MethodSource("invalidThemeRequests")
    void 테마_추가_요청_값이_잘못되면_400을_반환한다(String name, String description, String thumbnail) {
        createTheme(name, description, thumbnail).statusCode(400);
    }

    private static Stream<Arguments> invalidThemeRequests() {
        return Stream.of(
                Arguments.of("", "설명", "https://asdfsdf.sdfs"),
                Arguments.of("방탈출1", "", "https://asdfsdf.sdfs"),
                Arguments.of("방탈출1", "설명", "올바르지않은URL")
        );
    }

    @Test
    void 이미_존재하는_테마를_추가하면_409를_반환한다() {
        createTheme("방탈출1", "설명", "https://asdfsdf.sdfs").statusCode(201);
        createTheme("방탈출1", "설명2", "https://asdfsdf2.sdfs").statusCode(409);
    }

    @Test
    void 테마를_삭제한다() {
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs")
                .statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 존재하지_않는_테마를_삭제하면_404를_반환한다() {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약에_존재하는_테마를_삭제하면_409를_반환한다() {
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs")
                .statusCode(201)
                .extract().path("id");
        int timeId = createTime("10:00");
        createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 전체_테마를_조회한다() {
        createTheme("방탈출1", "다함께 탈출해요 방탈출1", "https://example.com/theme1.jpg");
        createTheme("방탈출2", "다함께 탈출해요 방탈출2", "https://example.com/theme2.jpg");

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void 인기_테마를_조회한다() {
        RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    private ValidatableResponse createTheme(String name, String description, String thumbnail) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "description", description, "thumbnail", thumbnail))
                .when().post("/admin/themes")
                .then().log().all();
    }

    private int createTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private void createReservation(String name, String date, int timeId, int themeId) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().statusCode(201);
    }
}
