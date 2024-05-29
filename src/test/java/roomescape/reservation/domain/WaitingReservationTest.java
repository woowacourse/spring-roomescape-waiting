package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.USER_MIA;
import static roomescape.TestFixture.WOOTECO_THEME;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

class WaitingReservationTest {

    @Test
    @DisplayName("예약 대기 순서를 계산한다.")
    void calculateOrder() {
        // given
        WaitingReservation waitingReservation = new WaitingReservation(
                MIA_RESERVATION(new ReservationTime(MIA_RESERVATION_TIME), WOOTECO_THEME(), USER_MIA(), WAITING), 1);

        // when
        long order = waitingReservation.calculateOrder();

        // then
        assertThat(order).isEqualTo(2);
    }

}
