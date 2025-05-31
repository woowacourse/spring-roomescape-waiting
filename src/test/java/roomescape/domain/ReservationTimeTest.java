package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.slot.ReservationTime;

class ReservationTimeTest {

    @Test
    @DisplayName("예약 시간이 null 일 수 없다.")
    void reservationTimeTest() {
        assertThatThrownBy(() -> new ReservationTime(null))
                .isInstanceOf(NullPointerException.class);
    }
}
