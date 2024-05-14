package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import roomescape.IntegrationTestSupport;

class ThemeControllerTest extends IntegrationTestSupport {

    String createdId;
    int themeSize;

    @DisplayName("테마 생성 조회")
    @TestFactory
    Stream<DynamicTest> dynamicUserTestsFromCollection() {
        return Stream.of(
                dynamicTest("테마 목록을 조회한다.", () -> {
                    themeSize = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().get("/themes")
                            .then().log().all()
                            .statusCode(200).extract()
                            .response().jsonPath().getList("$").size();
                }),
                dynamicTest("테마을 추가한다.", () -> {
                    Map<String, String> param = Map.of("name", "테마_테스트",
                            "description", "설명_테스트",
                            "thumbnail", "섬네일");

                    createdId = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .body(param)
                            .when().post("/admin/themes")
                            .then().log().all()
                            .statusCode(201).extract().header("location").split("/")[2];
                }),
                dynamicTest("테마 목록 개수가 1증가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().get("/themes")
                            .then().log().all()
                            .statusCode(200).body("size()", is(themeSize + 1));
                }),
                dynamicTest("테마이름이 비어있을 수 없다.", () -> {
                    Map<String, String> param = Map.of("name", "  ",
                            "description", "설명_테스트",
                            "thumbnail", "섬네일");

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .body(param)
                            .when().post("/admin/themes")
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("테마를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().delete("/admin/themes/" + createdId)
                            .then().log().all()
                            .statusCode(204);
                }),
                dynamicTest("테마 목록 개수가 1감소한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().get("/themes")
                            .then().log().all()
                            .statusCode(200).body("size()", is(themeSize));
                })
        );
    }

    @DisplayName("인기 테마 조회")
    @Test
    void themeNameBlank() {
        Map<String, String> params = Map.of("startDate", "2024-05-04",
                "endDate", "2024-05-09",
                "limit", "2");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams(params)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200);
    }
}
