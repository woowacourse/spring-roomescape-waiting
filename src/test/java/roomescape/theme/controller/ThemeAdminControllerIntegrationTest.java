package roomescape.theme.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.theme.controller.dto.ThemeRequest;

@SpringWebTest
public class ThemeAdminControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("관리자가 테마를 생성한다.")
    void createTheme_success() {
        ThemeRequest request = new ThemeRequest("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("name", is("우아한 테마"));
    }

    @Test
    @DisplayName("관리자가 테마를 삭제한다.")
    void deleteTheme_success() {
        ThemeRequest request = new ThemeRequest("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");

        Long id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/themes/" + id)
                .then().log().all()
                .statusCode(204);
    }
}
