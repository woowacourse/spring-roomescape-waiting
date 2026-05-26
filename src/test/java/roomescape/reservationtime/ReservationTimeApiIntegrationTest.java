package roomescape.reservationtime;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.support.ApiIntegrationTestHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationTimeApiIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiIntegrationTestHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new ApiIntegrationTestHelper(jdbcTemplate);
        testHelper.clearDatabase();
    }

    @DisplayName("특정 날짜/테마의 예약 가능 시간대 조회 API를 테스트합니다.")
    @Test
    void find_available_times() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservationTime(LocalTime.of(11, 0));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", "2028-05-04")
                .queryParam("themeId", themeId)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3))
                .body("startAt", containsInAnyOrder("09:00", "10:00", "11:00"))
                .body("available", everyItem(is(true)));
    }
}
