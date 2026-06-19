package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeControllerTest extends ControllerTest {

    @Test
    @DisplayName("API - 전체 테마 조회")
    void API_전체_테마_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("API - Top 10 테마 조회")
    void API_인기_테마_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("size", 10)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("API - 특정 id의 테마 시간 조회")
    void API_특정_id의_테마_시간_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("id", 0)
                .queryParam("date", LocalDate.now().plusDays(1).toString())
                .when().get("/themes/{id}/times")
                .then().log().all()
                .statusCode(200);
    }
}
