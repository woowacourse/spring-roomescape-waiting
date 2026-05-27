package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql("/cleanup.sql")
@SqlMergeMode(MergeMode.MERGE)
public class ThemeControllerTest {

    @Test
    void 테마_조회시_성공하면_200을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @Sql("/popular-themes.sql")
    void 인기테마_조회시_성공하면_200을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/themes/popular?from=2026-05-01&to=2026-05-07")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));
    }

    @Test
    @Sql("/popular-themes.sql")
    void 인기테마_조회시_from이_누락되면_400을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/themes/popular?to=2026-05-07")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("COMMON400_003"));
    }

    @Test
    @Sql("/popular-themes.sql")
    void 인기테마_조회시_to가_누락되면_400을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/themes/popular?from=2026-05-01")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("COMMON400_003"));
    }

    @Test
    @Sql("/popular-themes.sql")
    void 인기테마_조회시_from_형식이_잘못되면_400을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/themes/popular?from=2026/05/01&to=2026-05-07")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("COMMON400_005"));
    }

    @Test
    @Sql("/popular-themes.sql")
    void 인기테마_조회시_to_형식이_잘못되면_400을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/themes/popular?from=2026-05-01&to=2026/05/07")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("COMMON400_005"));
    }
}
