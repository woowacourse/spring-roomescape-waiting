package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.application.dto.result.PopularThemeResult;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.support.FixedPopularPolicyConfig;
import roomescape.support.ServiceIntegrationTest;

/**
 * ThemeService 통합 테스트.
 *
 * <p>검증 대상은 두 가지로, 둘 다 "시스템 상태(이미 저장된 예약)에 의존하는 규칙"이라 실제 H2로 검증한다:
 * <ul>
 *   <li>삭제 거부: 예약이 존재하는 테마는 삭제할 수 없다 (existsByThemeId 상태에 의존)</li>
 *   <li>인기 테마 집계: 최근 7일 예약 건수로 정렬 (집계 쿼리 + 날짜 경계)</li>
 * </ul>
 *
 * <p>인기 테마는 "오늘"의 의미가 흔들리면 집계 범위가 달라지므로 @Import(FixedPopularPolicyConfig)로
 * today를 2026-05-09로 고정한다. 집계 SQL "자체"의 정확성은 Repository 슬라이스가 따로 책임지고,
 * 여기서는 "서비스가 정책의 기간으로 집계를 호출해 결과를 만든다"는 흐름을 본다.
 */
@Import(FixedPopularPolicyConfig.class)
class ThemeServiceTest extends ServiceIntegrationTest {

    private static final LocalDate TODAY = FixedPopularPolicyConfig.TODAY;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationService reservationService;

    @Nested
    @DisplayName("테마 삭제 거부 정책")
    class DeletePolicy {

        @Test
        @DisplayName("[상태 의존] 예약이 존재하는 테마는 삭제할 수 없다")
        void 예약_있는_테마_삭제_거부() {
            Long timeId = fixture.insertTime(LocalTime.of(10, 0));
            Long themeId = fixture.insertTheme("테마A");
            fixture.insertReservation("브라운", TODAY.plusDays(1), timeId, themeId);

            assertThatThrownBy(() -> themeService.delete(themeId))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }

        @Test
        @DisplayName("예약이 없는 테마는 삭제할 수 있다")
        void 예약_없는_테마_삭제_허용() {
            Long themeId = fixture.insertTheme("테마A");

            assertThatCode(() -> themeService.delete(themeId))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("예약을 삭제한 뒤에는 그 테마를 삭제할 수 있다")
        void 예약_삭제_후_테마_삭제_허용() {
            Long timeId = fixture.insertTime(LocalTime.of(10, 0));
            Long themeId = fixture.insertTheme("테마A");
            Long reservationId = fixture.insertReservation("브라운", TODAY.plusDays(1), timeId, themeId);

            // 예약을 실제로 제거하면(관리자 삭제) 테마 삭제 제약이 풀려야 한다
            reservationService.delete(reservationId);

            assertThatCode(() -> themeService.delete(themeId))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("인기 테마 조회")
    class Popular {

        @Test
        @DisplayName("최근 7일 예약 건수가 많은 순으로 정렬된다")
        void 예약_많은_순_정렬() {
            Long time1 = fixture.insertTime(LocalTime.of(10, 0));
            Long time2 = fixture.insertTime(LocalTime.of(11, 0));
            Long time3 = fixture.insertTime(LocalTime.of(12, 0));
            Long popular = fixture.insertTheme("인기테마");   // 예약 2건
            Long lessPopular = fixture.insertTheme("덜인기");  // 예약 1건

            LocalDate within = TODAY.minusDays(1);  // 집계 범위(최근 7일) 안
            fixture.insertReservation("a", within, time1, popular);
            fixture.insertReservation("b", within, time2, popular);
            fixture.insertReservation("c", within, time3, lessPopular);

            List<PopularThemeResult> result = themeService.findPopular();

            assertThat(result).extracting(r -> r.getTheme().getName())
                    .containsExactly("인기테마", "덜인기");  // 건수 내림차순
        }

        @Test
        @DisplayName("집계 기간(7일)을 벗어난 예약은 포함되지 않는다 (날짜 경계)")
        void 기간_밖_예약_제외() {
            Long time1 = fixture.insertTime(LocalTime.of(10, 0));
            Long oldTheme = fixture.insertTheme("오래된테마");

            LocalDate tooOld = TODAY.minusDays(8);  // 7일 범위 밖
            fixture.insertReservation("a", tooOld, time1, oldTheme);

            List<PopularThemeResult> result = themeService.findPopular();

            assertThat(result).extracting(r -> r.getTheme().getName())
                    .doesNotContain("오래된테마");
        }

        @Test
        @DisplayName("예약이 하나도 없으면 빈 목록을 반환한다")
        void 예약_없으면_빈_목록() {
            fixture.insertTheme("아무도없는테마");

            assertThat(themeService.findPopular()).isEmpty();
        }

        @Test
        @DisplayName("[경계/미정의] 건수가 동률이면 둘 다 포함되나, 그들 사이의 순서는 보장되지 않는다")
        void 동률_순서_미보장() {
            Long time1 = fixture.insertTime(LocalTime.of(10, 0));
            Long time2 = fixture.insertTime(LocalTime.of(11, 0));
            Long themeA = fixture.insertTheme("테마A");
            Long themeB = fixture.insertTheme("테마B");
            LocalDate within = TODAY.minusDays(1);
            // 둘 다 1건씩 — 동률
            fixture.insertReservation("a", within, time1, themeA);
            fixture.insertReservation("b", within, time2, themeB);

            List<PopularThemeResult> result = themeService.findPopular();

            // 순서(containsExactly)를 단언하지 않는다. 동률일 때 ORDER BY count DESC만으로는
            // A·B 사이 순서가 비결정적이기 때문이다. 이 테스트는 그 미정의 동작을 "순서를 믿지 않는"
            // 방식으로 노출한다 → 프로덕션에 2차 정렬 기준(예: 이름)이 필요한지 결정할 재료.
            assertThat(result).extracting(r -> r.getTheme().getName())
                    .containsExactlyInAnyOrder("테마A", "테마B");
            assertThat(result).allMatch(r -> r.getReservationCount() == 1L);
        }
    }
}
