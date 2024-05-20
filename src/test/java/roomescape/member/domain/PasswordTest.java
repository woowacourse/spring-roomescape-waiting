package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exceptions.ValidationException;

class PasswordTest {

    @Test
    @DisplayName("비밀번호가 6자 미만일 경우 예외가 발생한다.")
    void shortPasswordThrowException() {
        assertThatThrownBy(() -> new Password("12345")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("비밀번호가 100자 초과일 경우 예외가 발생한다.")
    void tooLongPasswordThrowException() {
        String password = "1".repeat(101);
        assertThatThrownBy(() -> new Password(password)).isInstanceOf(ValidationException.class);
    }
}
