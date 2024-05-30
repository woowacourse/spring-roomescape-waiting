package roomescape.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.ThemeFixture.THEME_1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.fixture.ThemeFixture;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeControllerTest {
    private static final int COUNT_OF_THEME = 3;

    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("테마를 조회, 추가, 삭제 할 수 있다.")
    @Test
    void findCreateDeleteReservations() {
        ThemeCreateRequest params = ThemeFixture.toThemeCreateRequest(THEME_1);

        // 테마 추가
        ThemeResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201).extract()
                .jsonPath().getObject("", ThemeResponse.class);

        // 테마 조회
        List<ThemeResponse> themeResponses = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList("", ThemeResponse.class);

        assertThat(themeResponses).containsExactlyInAnyOrder(response);

        // 테마 삭제
        RestAssured.given().log().all()
                .when().delete("/themes/" + response.id())
                .then().log().all()
                .statusCode(204);

        // 테마 조회
        themeResponses = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList("", ThemeResponse.class);

        assertThat(themeResponses).isEmpty();
    }

    @DisplayName("인기 테마 목록을 읽을 수 있다.")
    @Test
    @Sql(scripts = {"/init.sql", "/ranking-data.sql"})
    void findPopularReservations() {
        List<ThemeResponse> expected = List.of(
                new ThemeResponse(3L, "레벨4 탈출", "우테코 레벨4 탈출기!", "https://img.jpg"),
                new ThemeResponse(1L, "레벨2 탈출", "우테코 레벨2 탈출기!", "https://img.jpg"),
                new ThemeResponse(2L, "레벨3 탈출", "우테코 레벨3 탈출기!", "https://img.jpg")
        );

        List<ThemeResponse> response = RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ThemeResponse.class);

        assertThat(response).isEqualTo(expected);
    }
}
