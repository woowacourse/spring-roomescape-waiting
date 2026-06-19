package roomescape.apitest.member;

import static org.hamcrest.Matchers.is;
import static roomescape.common.config.FixedClockConfig.TODAY;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeApiTest {

    @Test
    @DisplayName("사용자는 예약 가능한 시간을 조회할 수 있다.")
    void getThemeSchedules_Success() {
        LocalDate now = LocalDate.parse(TODAY);

        RestAssured.given().log().all()
                .when().get("/themes/1/schedules?date=" + now)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));
    }

    @Test
    @DisplayName("사용자는 최근 1주동안 예약이 많았던 테마를 조회할 수 있다.")
    void getPopularTheme_Success() {
        int expectedSize = 10;

        RestAssured.given().log().all()
                .when().get("/themes/popular?limit=10")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize));
    }
}
