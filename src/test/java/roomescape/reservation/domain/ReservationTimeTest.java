package roomescape.reservation.domain;

import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.common.exception.ReservationException;

class ReservationTimeTest {

    @Test
    void 시간이_null이면_예외가_발생한다() {
        // given
        final LocalTime time = null;

        // when & then
        Assertions.assertThatThrownBy(() -> new ReservationTime(time))
                .isInstanceOf(ReservationException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"09:59", "22:01"})
    void 영업시간이_아니면_예외가_발생한다(final LocalTime time) {
        // when & then
        Assertions.assertThatThrownBy(() -> new ReservationTime(time))
                .isInstanceOf(ReservationException.class);
    }
}