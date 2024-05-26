package roomescape.domain.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.member.domain.Password.PASSWORD_EMPTY_ERROR_MESSAGE;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ValueNullOrEmptyException;

class PasswordTest {

    @DisplayName("비밀번호가 정상적인 값으로 생성할 수 있다.")
    @Test
    void should_create_password() {
        assertThatCode(() -> new Password("123123"))
                .doesNotThrowAnyException();
    }


    @DisplayName("비밀번호가 비어있으면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_password_is_empty() {
        assertThatThrownBy(() -> new Password(""))
                .isInstanceOf(ValueNullOrEmptyException.class)
                .hasMessage(PASSWORD_EMPTY_ERROR_MESSAGE);
    }

    @DisplayName("비밀번호가 null이면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_password_is_null() {
        assertThatThrownBy(() -> new Password(null))
                .isInstanceOf(ValueNullOrEmptyException.class)
                .hasMessage(PASSWORD_EMPTY_ERROR_MESSAGE);
    }
}
