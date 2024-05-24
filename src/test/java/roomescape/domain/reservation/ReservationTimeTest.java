package roomescape.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.START_AT_SIX;

class ReservationTimeTest {

    @Test
    @DisplayName("예약 시간이 생성된다.")
    void createReservationTime() {
        // given
        final String time = START_AT_SIX;

        // when & then
        assertThatCode(() -> new ReservationTime(time))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "13-00"})
    @DisplayName("예약 시간 입력 값이 유효하지 않으면 예외가 발생한다.")
    void throwExceptionWhenCannotConvertToLocalTime(final String invalidTime) {
        assertThatThrownBy(() -> new ReservationTime(invalidTime))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
