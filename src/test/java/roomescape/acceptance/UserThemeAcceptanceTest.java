package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import roomescape.support.AcceptanceTest;
import roomescape.support.FixedPopularPolicyConfig;

/**
 * 사용자 테마 조회 인수 테스트 (RestAssured E2E).
 *
 * <p>시선: "사용자가 테마를 둘러보고 예약 가능 시간을 확인하고 인기 테마를 보는 흐름이 성립하는가".
 *
 * <p>예약 가능 시간 필터링·인기 집계의 세부 경계는 ReservationTimeServiceTest/ThemeServiceTest와
 * Repository 슬라이스가 이미 검증했다. 여기서는 그 결과가 사용자에게 HTTP로 도달하는 대표 경로만 본다.
 *
 * <p>인기 테마는 "오늘" 기준이 흔들리면 안 되므로 @Import(FixedPopularPolicyConfig)로 today를 고정한다.
 */
@Import(FixedPopularPolicyConfig.class)
class UserThemeAcceptanceTest extends AcceptanceTest {

    private static final LocalDate TODAY = FixedPopularPolicyConfig.TODAY;

    @Nested
    @DisplayName("예약 가능 시간 조회")
    class AvailableTimes {

        @Test
        @DisplayName("이미 예약된 시간은 가능 목록에서 빠진 채로 사용자에게 보인다")
        void 예약된_시간_제외() {
            Long time10 = fixture.insertTime(LocalTime.of(10, 0));
            fixture.insertTime(LocalTime.of(11, 0));
            Long themeId = fixture.insertTheme("테마A");
            LocalDate date = TODAY.plusDays(10);
            fixture.insertReservation("브라운", date, time10, themeId);  // 10:00 예약됨

            RestAssured.given().log().all()
                    .when().get("/user/themes/" + themeId + "/available-times?date=" + date)
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1))            // 11:00만 남음
                    .body("[0].startAt", is("11:00"));
        }
    }

    @Nested
    @DisplayName("인기 테마 조회")
    class Popular {

        @Test
        @DisplayName("최근 7일 예약 건수가 많은 테마가 사용자에게 상위로 보인다")
        void 인기_순서() {
            Long time1 = fixture.insertTime(LocalTime.of(10, 0));
            Long time2 = fixture.insertTime(LocalTime.of(11, 0));
            Long popular = fixture.insertTheme("인기테마");
            Long less = fixture.insertTheme("덜인기");
            LocalDate within = TODAY.minusDays(1);

            fixture.insertReservation("a", within, time1, popular);
            fixture.insertReservation("b", within, time2, popular);
            fixture.insertReservation("c", within, time1, less);

            RestAssured.given().log().all()
                    .when().get("/user/themes/popular")
                    .then().log().all()
                    .statusCode(200)
                    .body("[0].name", is("인기테마"))
                    .body("[0].reservationCount", is(2));
        }
    }
}
