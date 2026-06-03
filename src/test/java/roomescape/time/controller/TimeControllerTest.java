package roomescape.time.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TimeControllerTest {

    @Test
    void 전제시간_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/times/reservation-time")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(11));
    }

    @Test
    void 예약가능시간_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/times/available-time?themeId=1&date=" + LocalDate.now().plusDays(1))
                .then().log().all()
                .statusCode(200)
                .body("size()", is(11));
    }
}
