package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ThemeControllerTest extends ControllerTestSupport {

    @Test
    void 테마를_추가한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "방탈출1", "description", "다함께 탈출해요 방탈출", "thumbnail", "https://asdfsdf.sdfs"))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .header("Location", "/admin/themes/1");
    }

    @ParameterizedTest
    @MethodSource("invalidThemeRequests")
    void 테마_추가_요청_값이_잘못되면_400을_반환한다(String name, String description, String thumbnail) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "description", description, "thumbnail", thumbnail))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400);
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
        createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "방탈출1", "description", "설명2", "thumbnail", "https://asdfsdf2.sdfs"))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 테마를_삭제한다() {
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");

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
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        int timeId = createTime("10:00");
        createReservation(createMember("브라운"), LocalDate.now().plusDays(1).toString(), timeId, themeId).statusCode(201);

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
}
