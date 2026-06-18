package roomescape.acceptance;

import org.junit.jupiter.api.Test;
import roomescape.acceptance.step.ReservationSteps;
import roomescape.acceptance.step.ReservationTimeSteps;
import roomescape.acceptance.step.ThemeSteps;
import roomescape.domain.ReservationStatus;

public class ReservationAcceptanceTest extends AcceptanceTest {

    @Test
    void reservationApiSuccessTest() {
        // 1. 시간 추가
        ReservationTimeSteps.createReservationTime(FUTURE_TIME);

        // 2. 테마 추가
        ThemeSteps.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        // 3. 예약 추가
        ReservationSteps.saveReservation(1L, NOW_DATE, 1L, 1L);

        // 4. 대기 추가
        ReservationSteps.saveReservation(2L, NOW_DATE, 1L, 1L);
        ReservationSteps.saveReservation(3L, NOW_DATE, 1L, 1L);

        // 5. 이름 조회로 대기 추가 확인
        ReservationSteps.findByName("예약자2", 1, ReservationStatus.WAITING);

        // 6. 전체 조회 사이즈로 예약, 대기 추가 확인
        ReservationSteps.checkAllReservationSize(1, 2);

        // 7. 예약 삭제
        ReservationSteps.deleteReservation(1L);

        // 8. 대기가 예약으로 변경되었는지 이름 조회로 확인
        ReservationSteps.findByName("예약자2", 1, ReservationStatus.CONFIRMED);

        // 9. 대기 삭제
        ReservationSteps.deleteWait(2L);

        // 10. 전체 조회 사이즈로 예약, 대기 삭제 확인
        ReservationSteps.checkAllReservationSize(1, 0);
    }
}
