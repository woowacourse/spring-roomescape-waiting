package roomescape.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.assertion.ThemeAssertions;
import roomescape.acceptance.fixture.ReservationFixture;
import roomescape.acceptance.fixture.ReservationTimeFixture;
import roomescape.acceptance.fixture.ThemeFixture;

public class ThemeAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("테마를 생성하고 삭제할 수 있다")
    void createAndDeleteTheme() {
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");
        ThemeAssertions.checkAllThemeSize(1);

        ThemeFixture.deleteTheme(1L);
        ThemeAssertions.checkAllThemeSize(0);
    }

    @Test
    @DisplayName("예약 수를 기반으로 테마 랭킹을 조회할 수 있다")
    void themeApiSuccessTest() {
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ReservationFixture.createReservation("예약자", NOW_DATE, 1L, 1L);

        ThemeAssertions.checkThemeRanking("2026-04-20", "2026-05-02", 1);
    }
}
