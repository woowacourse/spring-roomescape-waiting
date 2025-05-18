package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {

    @DisplayName("올바른 비밀번호 값을 통해 비밀번호를 생성할 수 있다.")
    @ValueSource(strings = {"12345678", "aaaaaaaaaaaaaa16"})
    @ParameterizedTest
    void createPasswordByValidValue(String value) {
        assertThatCode(() -> new Password(value))
                .doesNotThrowAnyException();
    }

    @DisplayName("비밀번호 값이 공백이면 비밀번호를 생성할 수 없다.")
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    @ParameterizedTest
    void createPasswordByBlankValue(String value) {
        assertThatThrownBy(() -> new Password(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("비밀번호 값이 유효 길이 범위를 벗어나면 비밀번호를 생성할 수 없다.")
    @ValueSource(strings = {"1234567", "aaaaaaaaaaaaaaa17"})
    @ParameterizedTest
    void createPasswordByInvalidLength(String value) {
        assertThatThrownBy(() -> new Password(value))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
