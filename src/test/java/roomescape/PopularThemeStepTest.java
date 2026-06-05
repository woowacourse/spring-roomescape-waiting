package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.domain.policy.PopularThemePolicy;
import roomescape.support.ReservationTestHelper;
import roomescape.support.TestRecentWeekPopularPolicy;

public class PopularThemeStepTest extends IntegrationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 9);

    @TestConfiguration
    static class FixedPolicyConfig {
        @Bean
        @Primary
        public PopularThemePolicy fixedPopularThemePolicy() {
            Clock fixed = Clock.fixed(
                    TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
            return new TestRecentWeekPopularPolicy(fixed);
        }
    }

    @Autowired
    private ReservationTestHelper helper;

    private Long islandThemeId;
    private Long cityThemeId;
    private Long balloonThemeId;

    @BeforeEach
    void setUp() {
        Long time1 = helper.insertTime(LocalTime.of(10, 0));
        Long time2 = helper.insertTime(LocalTime.of(11, 0));
        Long time3 = helper.insertTime(LocalTime.of(12, 0));
        Long time4 = helper.insertTime(LocalTime.of(13, 0));
        Long time5 = helper.insertTime(LocalTime.of(14, 0));
        Long time6 = helper.insertTime(LocalTime.of(15, 0));
        Long time7 = helper.insertTime(LocalTime.of(16, 0));
        Long time8 = helper.insertTime(LocalTime.of(17, 0));

        islandThemeId = helper.insertTheme("무인도 탈출", "...", "https://example.com/island.jpg");
        cityThemeId = helper.insertTheme("도시 탈출", "...", "https://example.com/city.jpg");
        balloonThemeId = helper.insertTheme("열기구 탈출", "...", "https://example.com/balloon.jpg");

        LocalDate yesterday = TODAY.minusDays(1);
        LocalDate fiveDaysAgo = TODAY.minusDays(5);
        LocalDate eightDaysAgo = TODAY.minusDays(8);

        helper.insertReservation("user1", yesterday, time1, islandThemeId);
        helper.insertReservation("user2", yesterday, time2, islandThemeId);
        helper.insertReservation("user3", yesterday, time3, islandThemeId);
        helper.insertReservation("user4", fiveDaysAgo, time1, islandThemeId);
        helper.insertReservation("user5", fiveDaysAgo, time2, islandThemeId);

        helper.insertReservation("user6", fiveDaysAgo, time3, cityThemeId);
        helper.insertReservation("user7", fiveDaysAgo, time4, cityThemeId);
        helper.insertReservation("user8", fiveDaysAgo, time5, cityThemeId);
        helper.insertReservation("user9", fiveDaysAgo, time6, cityThemeId);
        helper.insertReservation("user10", eightDaysAgo, time1, cityThemeId);
        helper.insertReservation("user11", eightDaysAgo, time2, cityThemeId);

        helper.insertReservation("user12", yesterday, time4, balloonThemeId);

        helper.insertReservation("user13", TODAY, time1, islandThemeId);
        helper.insertReservation("user14", TODAY, time2, islandThemeId);
        helper.insertReservation("user15", TODAY, time3, islandThemeId);
        helper.insertReservation("user16", TODAY, time4, islandThemeId);
        helper.insertReservation("user17", TODAY, time5, islandThemeId);
    }


    @Test
    @DisplayName("예약 건수 내림차순으로 정렬된다")
    void 예약_건수_내림차순_정렬() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/user/themes/popular")
                .then().log().all()
                .statusCode(200)
                .extract();

        List<String> names = response.jsonPath().getList("name");
        List<Integer> counts = response.jsonPath().getList("reservationCount");

        assertThat(names.get(0)).isEqualTo("무인도 탈출");
        assertThat(counts.get(0)).isEqualTo(5);
        assertThat(names.get(1)).isEqualTo("도시 탈출");
        assertThat(counts.get(1)).isEqualTo(4);
    }

    @Test
    @DisplayName("8일 전 예약은 집계에서 제외된다")
    void 기간_밖_예약_제외() {
        ExtractableResponse<Response> response = RestAssured.given()
                .when().get("/user/themes/popular")
                .then().statusCode(200).extract();

        List<String> names = response.jsonPath().getList("name");
        List<Integer> counts = response.jsonPath().getList("reservationCount");

        int cityIndex = names.indexOf("도시 탈출");
        assertThat(cityIndex).isGreaterThanOrEqualTo(0);
        assertThat(counts.get(cityIndex)).isEqualTo(4);
    }

    @Test
    @DisplayName("오늘 예약은 집계에서 제외된다")
    void 오늘_예약_제외() {
        ExtractableResponse<Response> response = RestAssured.given()
                .when().get("/user/themes/popular")
                .then().statusCode(200).extract();

        List<String> names = response.jsonPath().getList("name");
        List<Integer> counts = response.jsonPath().getList("reservationCount");

        int islandIndex = names.indexOf("무인도 탈출");
        assertThat(islandIndex).isGreaterThanOrEqualTo(0);
        assertThat(counts.get(islandIndex)).isEqualTo(5);
    }

    @Test
    @DisplayName("최대 10개를 반환한다")
    void 최대_10개를_반환한다() {
        RestAssured.given().log().all()
                .when().get("/user/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", lessThanOrEqualTo(10));
    }

    @Test
    @DisplayName("응답 항목은 테마 정보와 예약 건수를 포함한다")
    void 응답_항목_형태() {
        RestAssured.given().log().all()
                .when().get("/user/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("[0].name", is("무인도 탈출"))
                .body("[0].reservationCount", is(5));
    }
}
