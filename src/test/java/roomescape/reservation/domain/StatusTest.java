package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StatusTest {

    @DisplayName("올바른 문자열을 Status로 변환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"RESERVATION", " RESERVATION  "})
    void convertFromValidString(final String name) {
        Optional<Status> result = Status.from(name);

        assertThat(result.get()).isEqualTo(Status.RESERVATION);
    }

    @DisplayName("null을 Status로 변환한다.")
    @Test
    void convertFromNull() {
        Optional<Status> result = Status.from(null);

        assertThat(result).isEmpty();
    }

    @DisplayName("올바르지 않은 문자열을 Status로 변환하면 예외가 발생한다.")
    @Test
    void convertFromInvalidString() {
        assertThatThrownBy(() -> Status.from("RESERVED"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
