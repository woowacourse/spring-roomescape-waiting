package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.dto.ThemeResponse;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class ThemeApiTest {

    @DisplayName("인기테마 조회를 성공할 경우 200을 반환한다.")
    @Test
    @Sql({"/test-time-data.sql", "/test-member-data.sql", "/test-popular-theme-data.sql"})
    void testFindPopularThemes() {
        ThemeResponse[] responses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/popular-themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", Matchers.is(3))
                .extract()
                .as(ThemeResponse[].class);
        assertAll(
                () -> assertThat(responses[0].id()).isEqualTo(2L),
                () -> assertThat(responses[1].id()).isEqualTo(1L),
                () -> assertThat(responses[2].id()).isEqualTo(3L)
        );
    }

    @DisplayName("테마 목록 조회를 성공할 경우 200을 반환한다.")
    @Test
    @Sql("/test-theme-data.sql")
    void testFindALl() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", Matchers.is(3));
    }
}
