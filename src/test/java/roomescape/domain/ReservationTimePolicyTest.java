package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.ReservationTimeException;

class ReservationTimePolicyTest {

    private final ReservationTimePolicy policy = new ReservationTimePolicy();

    @ParameterizedTest(name = "{0}시 {1}분은 예약 가능하다")
    @DisplayName("예약 가능 시간(12:00~22:00)에는 예약할 수 있다")
    @CsvSource({
            "12, 0",
            "12, 1",
            "21, 59",
            "22, 0"
    })
    void validateAvailableTime(int hour, int minute) {
        // given
        LocalTime time = LocalTime.of(hour, minute);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> policy.validate(time));
    }

    @ParameterizedTest(name = "{0}시 {1}분은 예약 불가능하다")
    @DisplayName("예약 불가능 시간에는 예약할 수 없다")
    @CsvSource({
            "11, 59",
            "22, 1"
    })
    void validateUnavailableTime(int hour, int minute) {
        // given
        LocalTime time = LocalTime.of(hour, minute);

        // when & then
        assertThatThrownBy(() -> policy.validate(time))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage("해당 시간은 예약 가능 시간이 아닙니다.");
    }
}
