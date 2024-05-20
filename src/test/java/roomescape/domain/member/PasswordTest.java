package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {
    @ParameterizedTest
    @DisplayName("비밀번호가 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validatePassword(String password) {
        assertThatThrownBy(() -> new Password(password))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호는 필수 값입니다.");
    }

    @Test
    @DisplayName("비밀번호가 255자를 넘으면 예외가 발생한다.")
    void validatePasswordLength() {
        String password = "a".repeat(256);

        assertThatThrownBy(() -> new Password(password))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호는 255자를 넘을 수 없습니다.");
    }

    @Test
    @DisplayName("비밀번호에 공백이 포함되어 있으면 예외가 발생한다")
    void validateContainsBlank() {
        String password = "123 45678";
        assertThatThrownBy(() -> new Password(password))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호에 공백이 포함되어 있습니다.");
    }
}
