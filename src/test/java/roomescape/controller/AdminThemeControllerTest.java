package roomescape.controller.controller;

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
import org.springframework.test.annotation.DirtiesContext;
import roomescape.DatabaseInitializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminThemeControllerTest {

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
                Arguments.of("방탈출1", "설명", "올바르지않은URL"),
                Arguments.of("공포의 저택에서 살아남기: 어둠 속의 비밀을 파헤쳐라. 과연 저택에 숨겨진 이야기는?", "설명", "https://asdfsdf.sdfs"),
                Arguments.of("방탈출1",
                        "오래된 저택의 깊은 곳에는 아무도 모르는 비밀이 숨겨져 있습니다. 당신은 과연 이 저택의 모든 수수께끼를 풀고 살아서 탈출할 수 있을까요? 시간이 얼마 남지 않았습니다. 지금 바로 방탈출에 참여하세요.",
                        "https://asdfsdf.sdfs"),
                Arguments.of("방탈출1", "설명",
                        "https://example.com/themes/horror-mansion-escape-room-thumbnail-image-very-very-very-long-filename-version.jpg")
        );
    }

    @Test
    void 이미_존재하는_테마를_추가하면_409를_반환한다() {
        createTheme("방탈출1", "설명", "https://asdfsdf.sdfs").statusCode(201);
        createTheme("방탈출1", "설명2", "https://asdfsdf2.sdfs").statusCode(409);
    }

    @Test
    void 테마를_삭제한다() {
        int themeId = createTheme("방탈출11", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs")
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

    private ValidatableResponse createTheme(String name, String description, String thumbnail) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "description", description, "thumbnail", thumbnail))
                .when().post("/admin/themes")
                .then().log().all();
    }
}
