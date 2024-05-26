package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.IllegalRequestException;
import roomescape.time.fixture.DateTimeFixture;

class ReservationTimeTest {

    @DisplayName("예약 시각이 정각 단위가 아니면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 58, 59})
    void should_reservation_time_is_hourly_unit(int min) {
        assertThatThrownBy(() -> new ReservationTime(null, LocalTime.of(1, min)))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("예약 시각이 null이면 예외가 발생한다")
    @Test
    void should_throw_exception_when_startAt_is_null() {
        assertThatThrownBy(() -> new ReservationTime(null, null))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("올바른 생성 요청 시 예외가 발생하지 않는다")
    @Test
    void should_not_throw_exception_when_valid_creation_request_arrived() {
        assertThatCode(() -> new ReservationTime(null, DateTimeFixture.TIME_10_00))
                .doesNotThrowAnyException();
    }
}
