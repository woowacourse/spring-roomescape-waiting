package roomescape.acceptance;

import org.junit.jupiter.api.Test;
import roomescape.acceptance.step.ReservationSteps;
import roomescape.acceptance.step.ReservationTimeSteps;
import roomescape.acceptance.step.ThemeSteps;

public class ThemeAcceptanceTest extends AcceptanceTest {

    @Test
    void reservationTimeApiSuccessTest() {
        // 1. 테마 추가
        ThemeSteps.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        // 2. 전체 테마 조회 사이즈로 테마 추가 확인
        ThemeSteps.checkAllThemeSize(1);

        // 3. 시간 추가
        ReservationTimeSteps.createReservationTime(FUTURE_TIME);

        // 4. 예약 추가
        ReservationSteps.saveReservation(1L, NOW_DATE, 1L, 1L);

        // 5. 특정 기간 내의 테마 랭킹 조회
        ThemeSteps.checkThemeRanking("2026-04-20", "2026-05-01", 1);

        // 5. 예약 삭제
        ReservationSteps.deleteReservation(1L);

        // 6. 테마 삭제
        ThemeSteps.deleteTheme(1L);

        // 7. 전체 테마 조회 사이즈로 테마 삭제 확인
        ThemeSteps.checkAllThemeSize(0);
    }
}
