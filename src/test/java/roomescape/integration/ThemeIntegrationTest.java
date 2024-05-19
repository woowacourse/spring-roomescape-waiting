package roomescape.integration;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.request.ThemeRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/init-data.sql", "/controller-test-data.sql"})
class ThemeIntegrationTest {

    @DisplayName("전체 테마를 조회한다.")
    @Test
    void should_get_themes() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .and()
                .body("size()", equalTo(11));
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
                .assertThat()
                .statusCode(201)
                .and()
                .header("Location", response -> equalTo("/themes/" + response.path("id")));
    }

    @DisplayName("테마를 삭제한다")
    @Test
    void should_remove_theme() {
        RestAssured.given().log().all()
                .pathParam("id", 5)
                .when().delete("/themes/{id}")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("인기 테마를 조회한다.")
    @Test
    void should_find_popular_theme() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes/top10")
                .then().log().all()
                .and()
                .statusCode(200)
                .body("name", contains("에버", "배키", "네오"));
    }
}
