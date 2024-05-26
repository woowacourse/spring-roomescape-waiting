package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.IllegalRequestException;

class PasswordTest {

    @DisplayName("패스워드가 null인 경우 예외를 발생시킨다")
    @Test
    void should_throw_exception_when_password_is_null() {
        assertThatThrownBy(() -> new Password(null))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("패스워드가 공백문자로만 이루어진 경우 생성 시 예외를 발생시킨다")
    @ParameterizedTest
    @ValueSource(strings = {" ", "    ", "    "})
    void should_throw_exception_when_password_is_blank(String password) {
        assertThatThrownBy(() -> new Password(password))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("유효한 패스워드는 생성 시 검증을 통과한다")
    @ParameterizedTest
    @ValueSource(strings = {"test@test.com", "test@test.co.kr"})
    void should_pass_validation_when_valid_password_creation(String password) {
        assertThatCode(() -> new Password(password))
                .doesNotThrowAnyException();
    }
}
