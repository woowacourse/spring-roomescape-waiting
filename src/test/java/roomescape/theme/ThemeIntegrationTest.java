package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.common.exceptionHandler.dto.ExceptionResponse;
import roomescape.theme.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ThemeIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = this.port;
    }

    @DisplayName("테마를 조회할 수 있다.")
    @Test
    void theme_view() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("테마를 추가 및 삭제 할 수 있다.")
    @Test
    void reservation_time_post_to_add() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "테스트1");
        params.put("description", "테스트2");
        params.put("thumbnail", "테스트3");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .response();
        // then
        ThemeResponse expected = new ThemeResponse(1L, "테스트1", "테스트2", "테스트3");
        ThemeResponse actual = response.as(ThemeResponse.class);
        assertThat(actual).isEqualTo(expected);

        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("테마 이름이 null 또는 빈 상태로 생성 요청 시 400 응답을 준다.")
    @ParameterizedTest
    @NullAndEmptySource
    void when_given_null_and_empty_theme_name(final String name) {
        // given
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("description", "hi");
        params.put("thumbnail", "http");
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 테마 이름이 비어있을 수 없습니다.", "/themes");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("테마 설명이 null 또는 빈 상태로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_null_and_empty_theme_description() {
        // given
        Map<String, Object> params = new HashMap<>();
        params.put("name", "제목");
        params.put("description", null);
        params.put("thumbnail", "http");
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 테마 설명이 비어있을 수 없습니다.", "/themes");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("테마 썸네일이 null 또는 빈 상태로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_null_and_empty_theme_thumbnail() {
        // given
        Map<String, Object> params = new HashMap<>();
        params.put("name", "제목");
        params.put("description", "hi");
        params.put("thumbnail", null);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 테마 썸네일이 비어있을 수 없습니다.", "/themes");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }
}
