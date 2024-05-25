package roomescape.dto.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDateConverterTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "22:00:00", "abc"})
    @DisplayName("예약 날짜 입력 값이 유효하지 않으면 예외가 발생한다.")
    void throwExceptionWhenCannotConvertToLocalDate(final String invalidDate) {
        assertThatThrownBy(() -> LocalDateConverter.toLocalDate(invalidDate))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
