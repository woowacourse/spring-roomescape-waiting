package roomescape.theme.presentation;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerE2ETest {

    private static String token;

    @LocalServerPort
    int serverPort;

    @BeforeEach
    public void beforeEach() {
        RestAssured.port = serverPort;
        Map<String, String> loginParams = Map.of("email", "admin@test.com", "password", "123");
        token = RestAssured.given().log().all()
                .when().body(loginParams)
                .contentType(ContentType.JSON).post("/login")
                .then().log().all()
                .extract().cookie("token");
    }

    @DisplayName("테마 목록 조회 API 작동을 확인한다")
    @Test
    void checkThemes() {
        RestAssured.given().log().all()
                .when().cookie("token", token).get("themes")
                .then().log().all()
                .statusCode(200).body("size()", is(3));
    }

    @DisplayName("테마 추가와 삭제의 작동을 확인한다")
    @TestFactory
    Stream<DynamicTest> checkThemeCreateAndDelete() {
        Map<String, String> themeParams = Map.of(
                "id", "4",
                "name", "테마명",
                "description", "테마 설명",
                "thumbnail", "테마 이미지"
        );

        return Stream.of(
                dynamicTest("현재 테마 개수를 확인한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).get("/themes")
                            .then().log().all()
                            .statusCode(200).body("size()", is(3));
                }),

                dynamicTest("테마를 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(themeParams)
                            .when().cookie("token", token).post("/themes")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("테마가 정상적으로 추가되었는지 확인한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).get("/themes")
                            .then().log().all()
                            .statusCode(200).body("size()", is(4));
                }),

                dynamicTest("id가 4인 테마를 삭제한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).delete("/themes/4")
                            .then().log().all()
                            .statusCode(204);
                }),

                dynamicTest("테마가 정상적으로 삭제되었는지 확인한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).get("/themes")
                            .then().log().all()
                            .statusCode(200).body("size()", is(3));
                })
        );
    }

    @DisplayName("인기 테마 목록 조회 API 작동을 확인한다")
    @Test
    void checkPopularThemes() {
        RestAssured.given().log().all()
                .when().get("themes/popular")
                .then().log().all()
                .statusCode(200);
    }
}
