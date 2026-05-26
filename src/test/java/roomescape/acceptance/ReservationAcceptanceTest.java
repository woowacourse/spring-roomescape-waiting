package roomescape.acceptance;

import org.junit.jupiter.api.Test;
import roomescape.acceptance.step.ReservationSteps;
import roomescape.acceptance.step.ReservationTimeSteps;
import roomescape.acceptance.step.ThemeSteps;

public class ReservationAcceptanceTest extends AcceptanceTest {

    @Test
    void reservationTimeApiSuccessTest() {
        // 1. 시간 추가
        ReservationTimeSteps.createReservationTime(FUTURE_TIME);

        // 2. 테마 추가
        ThemeSteps.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        // 3. 예약 추가
        ReservationSteps.createReservation("예약자", NOW_DATE, 1L, 1L);

        // 4. 전체 예약 조회 사이즈로 예약 추가 확인
        ReservationSteps.checkAllReservationSize(1);

        // 5, 예약 업데이트
        ReservationSteps.updateReservation(1L, FUTURE_DATE, 1L);

        // 5. 예약 삭제
        ReservationSteps.deleteReservation(1L);

        // 6. 전체 예약 조회 사이즈로 예약 삭제 확인
        ReservationSteps.checkAllReservationSize(0);
    }

    @Test
    void reservationTimeApiFailTest() {
        // 1. 시간 추가
        ReservationTimeSteps.createReservationTime("10:00");

        // 2. 테마 추가
        ThemeSteps.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        // 3. 예약 추가
        ReservationSteps.createReservation("예약자", NOW_DATE, 1L, 1L);

        // 4. 날짜, 시간, 테마가 동일한 예약 추가 시 예외 발생
        ReservationSteps.createDuplicatedReservation("예약자", NOW_DATE, 1L, 1L);
    }
}
