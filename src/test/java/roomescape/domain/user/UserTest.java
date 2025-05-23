package roomescape.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.InvalidInputException;

class UserTest {

    @Test
    @DisplayName("이름이 6글자 이상이면 예외가 발생한다")
    void nameLengthException() {
        assertThatThrownBy(() -> User.ofExisting(
                1L,
                "여섯글자이름",
                UserRole.USER,
                "email@email.com",
                "password")
        ).isInstanceOf(BusinessRuleViolationException.class);
    }

    @ParameterizedTest
    @DisplayName("이메일 형식이 맞지 않으면 예외가 발생한다")
    @CsvSource({"abc@email", "abc@email.", "abc@.com", "abc@.", "@email.com", "@email"})
    void emailFormatException(final String invalidEmail) {
        assertThatThrownBy(() -> User.ofExisting(
                1L,
                "이름",
                UserRole.USER,
                invalidEmail,
                "password")
        ).isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("비밀번호가 30글자 이상이면 예외가 발생한다")
    void passwordLengthException() {
        assertThatThrownBy(() -> User.ofExisting(
                1L,
                "이름",
                UserRole.USER,
                "email@email.com",
                "가".repeat(31))
        ).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("비밀번호 일치 여부를 알 수 있다.")
    void matchesPassword() {
        var user = User.ofExisting(1L, "포포", UserRole.USER, "popo@email.com", "password");
        assertAll(
                () -> assertThat(user.matchesPassword("password")).isTrue(),
                () -> assertThat(user.matchesPassword("PASSWORD")).isFalse()
        );
    }
}
