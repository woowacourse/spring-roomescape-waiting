package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReservationTimeTest {

    @Test
    @DisplayName("id와 LocalTime 을 통해 도메인을 생성한다.")
    void create_with_id_and_localTime() {
        assertThatCode(() -> new ReservationTime(null, LocalTime.parse("10:00")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("id 와 문자열 을 통해 도메인을 생성한다.")
    void create_with_id_and_string() {
        assertThatCode(() -> ReservationTime.from(null, "10:00"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "10.00", "24:00", "1:1"})
    @DisplayName("유효하지 않은 값을 입력하면 예외를 발생한다.")
    void throw_exception_when_string_is_invalid(final String invalidStartAt) {
        assertThatThrownBy(() -> ReservationTime.from(null, invalidStartAt))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
