package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.controller.dto.ThemeRequest;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.reservation.service.ThemeService;
import roomescape.util.ControllerTest;

@DisplayName("테마 API 통합 테스트")
class ThemeControllerTest extends ControllerTest {
    @Autowired
    ThemeService themeService;

    @DisplayName("테마 생성 시, 201을 반환한다.")
    @Test
    void create() {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("name", "siso");
        params.put("description", "Hi, I am Siso");
        params.put("thumbnail", "thumbnail");

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("테마 조회 시, 200을 반환한다.")
    @Test
    void findAll() {
        //given
        themeService.create(new ThemeRequest("name", "description", "thumbnail"));

        //when & then
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("테마 삭제 시, 204를 반환한다.")
    @Test
    void delete() {
        //given
        ThemeResponse themeResponse = themeService.create(new ThemeRequest("name", "description", "thumbnail"));

        //when & then
        RestAssured.given().log().all()
                .when().delete("/themes/" + themeResponse.id())
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("테마 생성 시, 잘못된 형식에 대해 400을 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", "      "})
    void createBadRequest(String name) {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", "Hi, I am Siso");
        params.put("thumbnail", "thumbnail");

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("적절하지 않은 limit에 대해 400을 반환한다.")
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 100})
    void invalidLimit(int limit) {
        //given
        String startDate = "2024-05-03";
        String endDate = "2024-05-06";

        //when & then
        RestAssured.given().log().all()
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .queryParam("limit", limit)
                .contentType(ContentType.JSON)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(400);
    }
}
