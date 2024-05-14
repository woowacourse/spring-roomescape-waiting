package roomescape.theme;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.testutil.IntegrationTest;

@IntegrationTest
class ThemeIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    @Test
    @DisplayName("방탈출 테마 생성 성공 시, 생성된 테마의 정보를 반환한다.")
    void createTheme() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마이름");
        params.put("description", "설명");
        params.put("thumbnail", "썸네일");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()

                .statusCode(201)
                .body("id", equalTo(1))
                .body("name", equalTo("테마이름"))
                .body("description", equalTo("설명"))
                .body("thumbnail", equalTo("썸네일"));
    }

    @Test
    @DisplayName("방탈출 테마 생성 시, 테마의 이름이 공백인 경우 예외를 반환한다.")
    void createTheme_WhenThemeNameIsBlank() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("description", "설명");
        params.put("thumbnail", "썸네일");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("테마 명은 공백 문자가 불가능합니다."));
    }

    @Test
    @DisplayName("방탈출 테마 생성 시, 테마의 이름이 255글자 초과 경우 예외를 반환한다.")
    void createTheme_WhenThemeNameOverLength() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "a".repeat(256));
        params.put("description", "설명");
        params.put("thumbnail", "썸네일");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("테마 명은 최대 255자까지 입력이 가능합니다."));
    }

    @Test
    @DisplayName("방탈출 테마 생성 시, 테마의 설명이 공백인 경우 예외를 반환한다.")
    void createTheme_WhenThemeDescriptionIsBlank() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마이름");
        params.put("description", "");
        params.put("thumbnail", "썸네일");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("테마 설명은 공백 문자가 불가능합니다."));
    }

    @Test
    @DisplayName("방탈출 테마 생성 시, 테마의 설명이 255글자 초과 경우 예외를 반환한다.")
    void createTheme_WhenThemeDescriptionOverLength() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마이름");
        params.put("description", "a".repeat(256));
        params.put("thumbnail", "썸네일");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("테마 설명은 최대 255자까지 입력이 가능합니다."));
    }

    @Test
    @DisplayName("방탈출 테마 생성 시, 테마의 썸네일이 공백인 경우 예외를 반환한다.")
    void createTheme_WhenThemeThumbnailIsBlank() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마이름");
        params.put("description", "테마설명");
        params.put("thumbnail", "");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("테마 썸네일은 공백 문자가 불가능합니다."));
    }

    @Test
    @DisplayName("방탈출 테마 생성 시, 테마의 썸네일이 255글자 초과 경우 예외를 반환한다.")
    void createTheme_WhenThemeThumbnailOverLength() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마이름");
        params.put("description", "테마설명");
        params.put("thumbnail", "a".repeat(256));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("테마 썸네일은 최대 255자까지 입력이 가능합니다."));
    }

    @Test
    @DisplayName("방탈출 테마 목록을 조회한다.")
    void getThemes() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()

                .statusCode(200)
                .body("id", hasItems(1))
                .body("name", hasItems("테마이름"))
                .body("description", hasItems("설명"))
                .body("thumbnail", hasItems("썸네일"));
    }

    @Test
    @DisplayName("인기 방탈출 테마 목록을 조회한다.")
    void getPopularThemes() {
        IntStream.rangeClosed(1, 20)
                        .forEach(index -> jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('" + index + "이름', '설명', '썸네일')"));
        jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");
        jdbcTemplate.update("insert into member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");
        IntStream.rangeClosed(10, 20)
                .forEach(index -> jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-04-23', 1, " + index + ");"));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes/popular")
                .then().log().all()

                .statusCode(200)
                .body("size()", is(10))
                .body("name",hasItems("20이름"));
    }

    @Test
    @DisplayName("방탈출 테마를 삭제한다.")
    void deleteTheme() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/1")
                .then().log().all()

                .statusCode(204);
    }

    @Test
    @DisplayName("방탈출 테마 삭제 시, 해당 테마가 존재하지 않는다면 예외를 반환한다.")
    void deleteTheme_WhenAlreadyNotExist() {
        // jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/1")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 테마가 존재하지 않습니다. 삭제가 불가능합니다."));
    }

    @Test
    @DisplayName("방탈출 테마 삭제 시, 해당 테마가 사용 중이라면 예외를 반환한다.")
    void deleteTheme_WhenThemeInUsage() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");
        jdbcTemplate.update("insert into member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-04-23', 1, 1 )");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/1")
                .then().log().all()

                .statusCode(409)
                .body("detail", equalTo("식별자 1인 테마를 사용 중인 예약이 존재합니다. 삭제가 불가능합니다."));
    }
}
