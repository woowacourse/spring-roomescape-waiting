package roomescape.reservationTime.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @DisplayName("과거 시간인지 검사한다")
    @Test
    void isBeforeNow() {
        // given
        ReservationTime past = new ReservationTime(LocalTime.now().minusMinutes(5));
        ReservationTime future = new ReservationTime(LocalTime.now().plusMinutes(5));

        // when & then
        assertThat(past.isBeforeNow()).isTrue();
        assertThat(future.isBeforeNow()).isFalse();
    }
}
