package roomescape.acceptance;

import org.junit.jupiter.api.Test;
import roomescape.acceptance.assertion.ThemeAssertions;
import roomescape.acceptance.fixture.ReservationFixture;
import roomescape.acceptance.fixture.ReservationTimeFixture;
import roomescape.acceptance.fixture.ThemeFixture;

public class ThemeAcceptanceTest extends AcceptanceTest {

    @Test
    void 테마를_생성하고_삭제할_수_있다() {
        // given
        테마를_생성한다();
        전체_테마가_1건이다();

        // when
        테마를_삭제한다();

        // then
        전체_테마가_0건이다();
    }

    @Test
    void 예약_수를_기반으로_테마_랭킹을_조회할_수_있다() {
        // given
        테마를_생성한다();
        예약_시간을_생성한다();
        예약자가_예약한다();

        // then
        테마_랭킹이_1건이다();
    }

    private void 테마를_생성한다() {
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");
    }

    private void 예약_시간을_생성한다() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
    }

    private void 예약자가_예약한다() {
        ReservationFixture.createReservation("예약자", NOW_DATE, 1L, 1L);
    }

    private void 테마를_삭제한다() {
        ThemeFixture.deleteTheme(1L);
    }

    private void 전체_테마가_0건이다() {
        ThemeAssertions.checkAllThemeSize(0);
    }

    private void 전체_테마가_1건이다() {
        ThemeAssertions.checkAllThemeSize(1);
    }

    private void 테마_랭킹이_1건이다() {
        ThemeAssertions.checkThemeRanking("2026-04-20", "2026-05-02", 1);
    }
}