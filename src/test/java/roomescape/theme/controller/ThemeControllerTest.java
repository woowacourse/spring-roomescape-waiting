package roomescape.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;

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
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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

    @DisplayName("테마 목록을 읽을 수 있다.")
    @Test
    void findReservations() {
        int size = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        assertThat(size).isEqualTo(COUNT_OF_THEME);
    }

    @DisplayName("인기 테마 목록을 읽을 수 있다.")
    @Test
    void findPopularReservations() {
        List<ThemeResponse> expected = List.of(
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

    @DisplayName("테마를 DB에 추가할 수 있다.")
    @Test
    void createTime() {
        ThemeCreateRequest params = new ThemeCreateRequest(
                "오리와 호랑이",
                "오리들과 호랑이들 사이에서 살아남기",
                "https://image.jpg");
        long expectedId = COUNT_OF_THEME + 1;

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/themes/" + expectedId);
    }

    @DisplayName("삭제할 id를 받아서 DB에서 해당 테마를 삭제 할 수 있다.")
    @Test
    void deleteTime() {
        RestAssured.given().log().all()
                .when().delete("/themes/3")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from theme", Integer.class);
        assertThat(countAfterDelete).isEqualTo(COUNT_OF_THEME - 1);
    }
}
