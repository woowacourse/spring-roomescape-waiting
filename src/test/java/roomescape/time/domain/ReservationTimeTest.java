package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.ReservationTimeNotHourlyUnitException;

class ReservationTimeTest {

    @DisplayName("예약 시각이 정각 단위가 아니면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 58, 59})
    void should_reservation_time_is_hourly_unit(int min) {
        assertThatThrownBy(() -> new ReservationTime(null, LocalTime.of(1, min)))
                .isInstanceOf(ReservationTimeNotHourlyUnitException.class);
    }
}
