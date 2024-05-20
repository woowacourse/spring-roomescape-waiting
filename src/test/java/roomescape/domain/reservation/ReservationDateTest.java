package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReservationDateTest {

    @Test
    @DisplayName("LocalDate 을 통해 도메인을 생성한다.")
    void create_domain_with_localDate() {
        assertThatCode(() -> new ReservationDate(LocalDate.parse("2019-01-01")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("문자열 을 통해 도메인을 생성한다.")
    void create_domain_with_string() {
        assertThatCode(() -> ReservationDate.from("2019-01-01"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "2019.03.01", "9999-99-99", "1000-00-00"})
    @DisplayName("유효하지 않은 값을 입력하면 예외를 발생한다.")
    void throw_exception_when_string_is_invalid(final String invalidDate) {
        assertThatThrownBy(() -> ReservationDate.from(invalidDate))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
