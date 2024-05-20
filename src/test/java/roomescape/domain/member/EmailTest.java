package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {
    @ParameterizedTest
    @DisplayName("이메일이 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validateEmail(String email) {
        assertThatThrownBy(() -> new Email(email))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일은 필수 값입니다.");
    }

    @Test
    @DisplayName("이메일이 255자를 넘으면 예외가 발생한다.")
    void validateEmailLength() {
        String email = "a".repeat(256);

        assertThatThrownBy(() -> new Email(email))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일은 255자를 넘을 수 없습니다.");
    }

    @Test
    @DisplayName("이메일이 형식에 맞지 않으면 예외가 발생한다.")
    void validateEmailPattern() {
        String email = "prin@com";

        assertThatThrownBy(() -> new Email(email))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 형식이 올바르지 않습니다.");
    }
}
