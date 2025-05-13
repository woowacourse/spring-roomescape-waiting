package roomescape.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserTest {

    @Test
    @DisplayName("유저 필드가 null이 될 수 없다")
    void validateNull() {
        // given
        final UserName name = UserName.from("홍길동");
        final Email email = Email.from("email@email.com");
        final Password password = Password.fromEncoded("password");
        final UserRole role = UserRole.NORMAL;

        // when
        // then
        assertAll(
                () -> assertThatThrownBy(() -> User.withId(null, name, email, password, role))
                        .isInstanceOf(NullPointerException.class),

                () -> assertThatThrownBy(() -> User.withId(UserId.unassigned(), name, email, password, role))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContainingAll("식별자가 할당되지 않았습니다."),

                () -> assertThatThrownBy(() -> User.withoutId(null, email, password, role))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: User.name"),

                () -> assertThatThrownBy(() -> User.withoutId(name, null, password, role))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: User.email"),

                () -> assertThatThrownBy(() -> User.withoutId(name, email, null, role))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: User.password"),

                () -> assertThatThrownBy(() -> User.withoutId(name, email, password, null))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: User.role")
        );
    }
}
