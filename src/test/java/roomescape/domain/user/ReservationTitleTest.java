package roomescape.domain.user;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.reservation.ReservationTitle;

class ReservationTitleTest {
    @Test
    @DisplayName("String 을 통해 도메인을 생성한다.")
    void create_with_string() {
        assertThatCode(() -> new ReservationTitle("조이썬"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("유효하지 않은 값을 입력하면 예외를 발생한다.")
    void throw_exception_when_string_is_invalid(final String invalidName) {
        assertThatThrownBy(() -> new ReservationTitle(invalidName))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
