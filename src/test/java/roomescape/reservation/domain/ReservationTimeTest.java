package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ViolationException;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTimeTest {

    @Test
    @DisplayName("예약 시간은 10분 단위이다.")
    void validateTimeUnit() {
        // given
        LocalTime invalidTime = LocalTime.of(1, 23);

        // when & then
        assertThatThrownBy(() -> new ReservationTime(invalidTime))
                .isInstanceOf(ViolationException.class);
    }
}
