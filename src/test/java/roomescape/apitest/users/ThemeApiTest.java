package roomescape.apitest.users;

import static org.hamcrest.Matchers.is;
import static roomescape.config.FixedClockConfig.TODAY;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeApiTest {

    @Test
    void 예약_가능한_시간_조회_API() {
        LocalDate now = LocalDate.parse(TODAY);

        RestAssured.given().log().all()
                .when().get("/themes/1/schedules?date=" + now)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(9));
    }

    @Test
    @DisplayName("최근 1주동안 예약이 많았던 테마를 조회하는 정상 테스트")
    void 테마_조회_API() {
        int expectedSize = 10;

        RestAssured.given().log().all()
                .when().get("/themes/popular?limit=10")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize));
    }
}
