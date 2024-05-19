package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.exception.ExceptionType.INVALID_PASSWORD;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.RoomescapeException;

class PasswordTest {

    @DisplayName("잘못된 값으로 Password 객체를 생성할 수 없다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1234567"})
    void wrongPasswordTest(String value) {
        assertThatThrownBy(() -> new Password(value))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(INVALID_PASSWORD.getMessage());
    }

    @DisplayName("정상적인 Password 객체를 생성할 수 있다.")
    void collectPasswordTest() {
        assertThatCode(() -> new Password("12345678"))
                .doesNotThrowAnyException();
    }
}
