package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.request.ThemeRequest;
import roomescape.model.Theme;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/controller-test-data.sql")
class ThemeControllerTest {

    @DisplayName("전체 테마를 조회한다.")
    @Test
    void should_get_themes() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", Theme.class);
    }

    @DisplayName("테마를 추가한다.")
    @Test
    void should_add_theme() {
        ThemeRequest request = new ThemeRequest("에버", "공포", "공포.jpg");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/themes/12");
    }

    @DisplayName("테마를 삭제한다")
    @Test
    void should_remove_theme() {
        RestAssured.given().log().all()
                .when().delete("/themes/11")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("인기 테마를 조회한다.")
    @Test
    void should_find_popular_theme() {

        List<Theme> popularThemes = RestAssured.given().log().all()
                .when().get("/themes/top10")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath().getList(".", Theme.class);
    }
}
