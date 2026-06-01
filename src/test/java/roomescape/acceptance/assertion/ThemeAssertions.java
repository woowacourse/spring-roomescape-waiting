package roomescape.acceptance.assertion;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;

public class ThemeAssertions {

    public static void checkAllThemeSize(int expectedSize) {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize));
    }

    public static void checkThemeRanking(String startDate, String endDate, int expectedRanking) {
        RestAssured.given().log().all()
                .param("startDate", startDate)
                .param("endDate", endDate)
                .when().get("/themes/ranking")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", is(expectedRanking));
    }
}
