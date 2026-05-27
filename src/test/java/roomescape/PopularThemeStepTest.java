package roomescape;

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


/*
 * 미션2 사이클1 - 인기 테마 조회 API 요구사항 테스트.
 * IntegrationTest 상속으로 매 테스트 빈 DB 보장.
 * 시간 의존성을 풀기 위해 @TestConfiguration으로 고정 Clock을 주입.
 * 각 테스트가 @BeforeEach에서 자기 데이터(테마 + 예약)를 직접 준비.
 *
 * 비즈니스 규칙:
 *  1) 7일 이내 예약만 집계 (8일 이전은 제외)
 *  2) 오늘 예약은 제외
 *  3) 예약 건수 내림차순 정렬
 *  4) 최대 10개
 */
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
        // 시간 슬롯 8개
        Long time1 = helper.insertTime(LocalTime.of(10, 0));
        Long time2 = helper.insertTime(LocalTime.of(11, 0));
        Long time3 = helper.insertTime(LocalTime.of(12, 0));
        Long time4 = helper.insertTime(LocalTime.of(13, 0));
        Long time5 = helper.insertTime(LocalTime.of(14, 0));
        Long time6 = helper.insertTime(LocalTime.of(15, 0));
        Long time7 = helper.insertTime(LocalTime.of(16, 0));
        Long time8 = helper.insertTime(LocalTime.of(17, 0));

        //핵심테마 3개
        islandThemeId = helper.insertTheme("무인도 탈출", "...", "https://example.com/island.jpg");
        cityThemeId = helper.insertTheme("도시 탈출", "...", "https://example.com/city.jpg");
        balloonThemeId = helper.insertTheme("열기구 탈출", "...", "https://example.com/balloon.jpg");

        LocalDate yesterday = TODAY.minusDays(1);
        LocalDate fiveDaysAgo = TODAY.minusDays(5);
        LocalDate eightDaysAgo = TODAY.minusDays(8);

        // 무인도: 어제 3건 + 5일 전 2건 = 5건 (1등)
        helper.insertReservation("user1", yesterday, time1, islandThemeId);
        helper.insertReservation("user2", yesterday, time2, islandThemeId);
        helper.insertReservation("user3", yesterday, time3, islandThemeId);
        helper.insertReservation("user4", fiveDaysAgo, time1, islandThemeId);
        helper.insertReservation("user5", fiveDaysAgo, time2, islandThemeId);

        // 도시: 5일 전 4건 + 8일 전 2건 = 집계상 4건 (2등)
        helper.insertReservation("user6", fiveDaysAgo, time3, cityThemeId);
        helper.insertReservation("user7", fiveDaysAgo, time4, cityThemeId);
        helper.insertReservation("user8", fiveDaysAgo, time5, cityThemeId);
        helper.insertReservation("user9", fiveDaysAgo, time6, cityThemeId);
        helper.insertReservation("user10", eightDaysAgo, time1, cityThemeId);
        helper.insertReservation("user11", eightDaysAgo, time2, cityThemeId);

        // 열기구: 어제 1건
        helper.insertReservation("user12", yesterday, time4, balloonThemeId);

        // 무인도 오늘 5건 (오늘이라 집계 제외 검증용)
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

        // 1등은 무인도(5건), 2등은 도시(4건)
        assert names.get(0).equals("무인도 탈출") : "1등은 무인도여야 함, 실제: " + names.get(0);
        assert counts.get(0) == 5 : "1등 건수는 5여야 함, 실제: " + counts.get(0);
        assert names.get(1).equals("도시 탈출") : "2등은 도시여야 함, 실제: " + names.get(1);
        assert counts.get(1) == 4 : "2등 건수는 4여야 함, 실제: " + counts.get(1);
    }

    @Test
    @DisplayName("8일 전 예약은 집계에서 제외된다")
    void 기간_밖_예약_제외() {
        // 도시 테마는 5일전 4건 + 8일전 2건 = 총 6건 예약이지만,
        // 8일전이 제외되면 4건이 집계되어야 함
        ExtractableResponse<Response> response = RestAssured.given()
                .when().get("/user/themes/popular")
                .then().statusCode(200).extract();

        List<String> names = response.jsonPath().getList("name");
        List<Integer> counts = response.jsonPath().getList("reservationCount");

        int cityIndex = names.indexOf("도시 탈출");
        assert cityIndex >= 0 : "도시 테마가 응답에 있어야 함";
        assert counts.get(cityIndex) == 4
                : "도시 테마 건수는 4여야 함 (8일전 2건 제외), 실제: " + counts.get(cityIndex);
    }

    @Test
    @DisplayName("오늘 예약은 집계에서 제외된다")
    void 오늘_예약_제외() {
        // 무인도 테마는 어제 3 + 5일전 2 + 오늘 5 = 총 10건이지만,
        // 오늘이 제외되면 5건만 집계되어야 함
        ExtractableResponse<Response> response = RestAssured.given()
                .when().get("/user/themes/popular")
                .then().statusCode(200).extract();

        List<String> names = response.jsonPath().getList("name");
        List<Integer> counts = response.jsonPath().getList("reservationCount");

        int islandIndex = names.indexOf("무인도 탈출");
        assert islandIndex >= 0 : "무인도 테마가 응답에 있어야 함";
        assert counts.get(islandIndex) == 5
                : "무인도 테마 건수는 5여야 함 (오늘 5건 제외), 실제: " + counts.get(islandIndex);
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
