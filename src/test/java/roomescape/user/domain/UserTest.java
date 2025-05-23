package roomescape.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    @DisplayName("유저 ID가 null이면 예외가 발생한다")
    void throwExceptionWhenUserIdIsNull() {
        // given
        final UserName name = UserName.from("몽이");
        final Email email = Email.from("email@email.com");
        final Password password = Password.fromEncoded("password");
        final UserRole role = UserRole.NORMAL;

        // when & then
        assertThatThrownBy(() -> User.withId(null, name, email, password, role))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: User.id");
    }

    @Test
    @DisplayName("유저 이름이 null이면 예외가 발생한다")
    void throwExceptionWhenUserNameIsNull() {
        // given
        final Email email = Email.from("email@email.com");
        final Password password = Password.fromEncoded("password");
        final UserRole role = UserRole.NORMAL;

        // when & then
        assertThatThrownBy(() -> User.withoutId(null, email, password, role))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: User.name");
    }

    @Test
    @DisplayName("유저 이메일이 null이면 예외가 발생한다")
    void throwExceptionWhenUserEmailIsNull() {
        // given
        final UserName name = UserName.from("홍길동");
        final Password password = Password.fromEncoded("password");
        final UserRole role = UserRole.NORMAL;

        // when & then
        assertThatThrownBy(() -> User.withoutId(name, null, password, role))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: User.email");
    }

    @Test
    @DisplayName("유저 패스워드가 null이면 예외가 발생한다")
    void throwExceptionWhenUserPasswordIsNull() {
        // given
        final UserName name = UserName.from("홍길동");
        final Email email = Email.from("email@email.com");
        final UserRole role = UserRole.NORMAL;

        // when & then
        assertThatThrownBy(() -> User.withoutId(name, email, null, role))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: User.password");
    }

    @Test
    @DisplayName("유저 역할이 null이면 예외가 발생한다")
    void throwExceptionWhenUserRoleIsNull() {
        // given
        final UserName name = UserName.from("홍길동");
        final Email email = Email.from("email@email.com");
        final Password password = Password.fromEncoded("password");

        // when & then
        assertThatThrownBy(() -> User.withoutId(name, email, password, null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: User.role");
    }
}
