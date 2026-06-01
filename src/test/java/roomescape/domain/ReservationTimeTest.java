package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.code.ReservationTimeErrorCode;
import roomescape.exception.domain.ReservationTimeException;

class ReservationTimeTest {

    @ParameterizedTest
    @ValueSource(strings = {"09:00", "09:59", "22:01", "23:00"})
    void 영업시간_외의_시간을_추가하면_예외가_발생한다(String time) {
        // given
        LocalTime startAt = LocalTime.parse(time);

        // when & then
        assertThatThrownBy(() -> new ReservationTime(startAt))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(ReservationTimeErrorCode.INVALID_RESERVATION_TIME_RANGE.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"10:01", "10:30", "21:59"})
    void 예약_시간이_1시간_단위가_아니면_예외가_발생한다(String time) {
        // given
        LocalTime startAt = LocalTime.parse(time);

        // when & then
        assertThatThrownBy(() -> new ReservationTime(startAt))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(ReservationTimeErrorCode.INVALID_RESERVATION_TIME_UNIT.getMessage());
    }
}
