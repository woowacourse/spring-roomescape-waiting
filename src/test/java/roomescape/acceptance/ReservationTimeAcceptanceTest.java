package roomescape.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.assertion.ReservationTimeAssertions;
import roomescape.acceptance.fixture.ReservationFixture;
import roomescape.acceptance.fixture.ReservationTimeFixture;
import roomescape.acceptance.fixture.ThemeFixture;

public class ReservationTimeAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("예약 시간을 생성하고 삭제할 수 있다")
    void createAndDeleteReservationTime() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ReservationTimeAssertions.checkAllReservationTimeSize(1);

        ReservationTimeFixture.deleteReservationTime(1L);
        ReservationTimeAssertions.checkAllReservationTimeSize(0);
    }

    @Test
    @DisplayName("예약 추가/삭제에 따라 해당 시간의 예약 가능 상태가 변경된다")
    void reservationTimeApiSuccessTest() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        ReservationTimeAssertions.checkAvailableReservation(NOW_DATE, 1L, true);

        ReservationFixture.createReservation("예약자", NOW_DATE, 1L, 1L);
        ReservationTimeAssertions.checkAvailableReservation(NOW_DATE, 1L, false);

        ReservationFixture.deleteReservation(1L);
        ReservationTimeAssertions.checkAvailableReservation(NOW_DATE, 1L, true);
    }
}
