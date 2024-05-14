package roomescape.theme.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.model.IntegrationTest;
import roomescape.theme.dto.ThemeRequest;

public class ThemeIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("요청한 테마를 정상적으로 등록, 확인, 삭제한다.")
    void themePageWork() {
        ThemeRequest themeRequest = new ThemeRequest("포레스트", "공포 테마", "thumbnail");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));

        RestAssured.given().log().all()
                .when().delete("/themes/3")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }
}
