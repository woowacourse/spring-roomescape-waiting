package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.support.ApiIntegrationTestHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WaitingApiIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiIntegrationTestHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new ApiIntegrationTestHelper(jdbcTemplate);
        testHelper.clearDatabase();
    }

    @DisplayName("이름으로 본인 대기 목록 조회 API를 테스트합니다.")
    @Test
    void find_my_waitings() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long firstTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long secondTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertWaiting("카야", LocalDate.of(2028, 5, 6), themeId, firstTimeId);
        testHelper.insertWaiting("스타크", LocalDate.of(2028, 5, 6), themeId, firstTimeId);
        testHelper.insertWaiting("스타크", LocalDate.of(2028, 5, 7), themeId, secondTimeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", equalTo("스타크"))
                .body("[0].date", equalTo("2028-05-06"))
                .body("[0].themeId", equalTo(themeId.intValue()))
                .body("[0].themeName", equalTo("theme name"))
                .body("[0].timeId", equalTo(firstTimeId.intValue()))
                .body("[0].startAt", equalTo("09:00"))
                .body("[0].order", equalTo(2))
                .body("[1].name", equalTo("스타크"))
                .body("[1].date", equalTo("2028-05-07"))
                .body("[1].timeId", equalTo(secondTimeId.intValue()))
                .body("[1].startAt", equalTo("10:00"))
                .body("[1].order", equalTo(1));
    }

    @DisplayName("본인 대기 취소 API를 테스트합니다.")
    @Test
    void cancel_my_waiting() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long waitingId = testHelper.insertWaiting("스타크", LocalDate.of(2028, 5, 6), themeId, timeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().delete("/waitings/{id}", waitingId)
                .then().log().all()
                .statusCode(204);

        Integer savedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE id = ?",
                Integer.class,
                waitingId
        );

        assertThat(savedCount).isZero();
    }

    @DisplayName("타인 대기 취소 시 403을 반환한다.")
    @Test
    void cancel_other_users_waiting() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long waitingId = testHelper.insertWaiting("타스", LocalDate.of(2028, 5, 6), themeId, timeId);

        RestAssured.given()
                .queryParam("name", "카야")
                .when().delete("/waitings/{id}", waitingId)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @DisplayName("존재하지 않는 대기 취소 시 404를 반환한다.")
    @Test
    void cancel_non_existing_waiting() {
        RestAssured.given()
                .queryParam("name", "타스")
                .when().delete("/waitings/{id}", 999)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
