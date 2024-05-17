package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReservationTimeTest {

    @DisplayName("생성 테스트")
    @Test
    void create() {
        assertThatCode(() -> new ReservationTime(1L, LocalTime.now().toString()))
            .doesNotThrowAnyException();
        assertThatCode(() -> new ReservationTime(LocalTime.now().toString()))
            .doesNotThrowAnyException();

    }

    @DisplayName("생성 테스트")
    @ParameterizedTest
    @ValueSource(strings = {"24:00", "-10:00"})
    void createByIllegalDate(String input) {
        assertThatCode(() -> new ReservationTime(1L, input))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
