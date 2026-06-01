package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidInputException;

class ReservationTimeTest {

    @Test
    void 시간_생성() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        assertThat(time.getId()).isEqualTo(1L);
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void startAt이_null이면_예외() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(InvalidInputException.class);
    }
}
