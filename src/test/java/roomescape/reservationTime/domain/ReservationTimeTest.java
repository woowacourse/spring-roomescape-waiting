package roomescape.reservationTime.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @DisplayName("시간 아이디와 시작 시간이 같은 경우 동일하다")
    @Test
    void isEqual() {
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.parse("10:00"));
        ReservationTime reservationTime2 = new ReservationTime(1L, LocalTime.parse("10:00"));

        assertThat(reservationTime1.equals(reservationTime2)).isTrue();
    }
}
