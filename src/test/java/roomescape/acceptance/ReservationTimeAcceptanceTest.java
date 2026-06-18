package roomescape.acceptance;

import org.junit.jupiter.api.Test;
import roomescape.acceptance.step.ReservationSteps;
import roomescape.acceptance.step.ReservationTimeSteps;
import roomescape.acceptance.step.ThemeSteps;
import roomescape.domain.ReservationAvailability;

public class ReservationTimeAcceptanceTest extends AcceptanceTest {

    @Test
    void reservationTimeApiSuccessTest() {
        // 1. 시간 추가
        ReservationTimeSteps.createReservationTime(FUTURE_TIME);

        // 2. 전체 시간 조회 사이즈로 시간 추가 확인
        ReservationTimeSteps.checkAllReservationTimeSize(1);

        // 3. 테마 추가
        ThemeSteps.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        // 4. 특정 날짜, 테마의 예약 가능한 시간 조회
        ReservationTimeSteps.checkAvailableReservation(NOW_DATE, 1L, ReservationAvailability.RESERVATION_AVAILABLE);

        // 5. 예약 추가
        ReservationSteps.saveReservation(1L, NOW_DATE, 1L, 1L);

        // 6. 예약 추가 후 해당 날짜, 테마의 시간 예약 가능 상태 변경 조회
        ReservationTimeSteps.checkAvailableReservation(NOW_DATE, 1L, ReservationAvailability.WAITING_AVAILABLE);

        // 7. 대기 추가
        ReservationSteps.saveReservation(2L, NOW_DATE, 1L, 1L);
        ReservationSteps.saveReservation(3L, NOW_DATE, 1L, 1L);
        ReservationSteps.saveReservation(4L, NOW_DATE, 1L, 1L);

        // 8. 대기 추가 후, 해당 날짜, 테마의 시간 예약 가능 상태 변경 조회
        ReservationTimeSteps.checkAvailableReservation(NOW_DATE, 1L, ReservationAvailability.NOTHING_AVAILABLE);

        // 7. 예약 삭제
        ReservationSteps.deleteWait(1L);
        ReservationSteps.deleteWait(2L);
        ReservationSteps.deleteWait(3L);
        ReservationSteps.deleteReservation(1L);

        // 8. 시간 삭제
        ReservationTimeSteps.deleteReservationTime(1L);

        // 9. 전체 시간 조회 사이즈로 시간 삭제 확인
        ReservationTimeSteps.checkAllReservationTimeSize(0);
    }
}
