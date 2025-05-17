package roomescape.domain.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessRuleViolationException;

class PasswordTest {

    @Test
    @DisplayName("비밀번호에 공백이 포함되면 예외가 발생한다")
    void passwordCannotContainsBlank() {
        var thirtyOneWords = "가".repeat(31);
        assertThatThrownBy(() -> new Password(thirtyOneWords))
            .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("비밀번호가 30글자 이상이면 예외가 발생한다")
    void passwordLengthCannotOverMax() {
        var thirtyOneWords = "가".repeat(31);
        assertThatThrownBy(() -> new Password(thirtyOneWords))
            .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("비밀번호가 일치하는 지 알 수 있다.")
    void matches() {
        var password = new Password("abcd");

        var matchPassword = new Password("abcd");
        var notMatchPassword = new Password("abcf");

        assertAll(
            () -> assertThat(password.matches(matchPassword)).isTrue(),
            () -> assertThat(password.matches(notMatchPassword)).isFalse()
        );
    }
}
