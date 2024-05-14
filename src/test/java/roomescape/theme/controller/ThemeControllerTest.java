package roomescape.theme.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("테마 컨트롤러")
class ThemeControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("테마 컨트롤러는 테마 추가 요청이 들어오면 저장 후 201을 반환한다.")
    @Test
    void createTheme() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "리얼공포");
        params.put("description", "완전 무서움");
        params.put("thumbnail", "http://something");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then()
                .statusCode(200)
                .body("size()", is(3));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then()
                .statusCode(200)
                .body("size()", is(4));
    }

    @DisplayName("테마 컨트롤러는 중복된 이름으로 생성 요청이 들어올 경우 400을 응답한다.")
    @Test
    void createThemeWithDuplicatedName() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "리얼공포");
        params.put("description", "완전 무서움");
        params.put("thumbnail", "http://something");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);


        // when
        String detailMessage = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        // then
        assertThat(detailMessage).isEqualTo("중복된 테마 이름입니다.");
    }


    @DisplayName("테마 컨트롤러는 테마 조회 요청이 들어오면 200을 반환한다.")
    @Test
    void readThemes() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @DisplayName("테마 컨트롤러는 최근 일주일간 인기 테마 조회 요청이 들어오면 200을 반환한다.")
    @Test
    void readPopularThemes() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("테마 컨트롤러는 테마 삭제 요청이 들어오면 204를 반환한다.")
    @Test
    void deleteTheme() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/3")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("테마 컨트롤러는 존재하지 않는 테마 삭제 요청이 들어오면 400을 응답한다.")
    @Test
    void deleteThemeWithNonExists() {
        // given
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().delete("/themes/3")
                .then()
                .statusCode(204);

        // when
        String detailMessage = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/3")
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        // then
        assertThat(detailMessage).isEqualTo("존재하지 않는 테마입니다.");
    }
}
