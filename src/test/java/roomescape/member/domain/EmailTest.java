package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.DomainValidationException;

class EmailTest {

    @DisplayName("이메일이 null인 경우 예외를 발생시킨다")
    @Test
    void should_throw_exception_when_email_is_null() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(DomainValidationException.class);
    }

    @DisplayName("이메일이 공백문자로만 이루어진 경우 예외를 발생시킨다")
    @ParameterizedTest
    @ValueSource(strings = {" ", "    ", "    "})
    void should_throw_exception_when_email_is_blank(String email) {
        assertThatThrownBy(() -> new Email(email))
                .isInstanceOf(DomainValidationException.class);
    }

    @DisplayName("이메일 형식이 아닌 경우 생성 시 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"test", "test@ddddd", "test.com"})
    void should_throw_exception_when_email_length_is_invalid(String email) {
        assertThatThrownBy(() -> new Email(email))
                .isInstanceOf(DomainValidationException.class);
    }

    @DisplayName("유효한 이메일은 생성 시 검증을 통과한다")
    @ParameterizedTest
    @ValueSource(strings = {"test@test.com", "test@test.co.kr"})
    void should_pass_validation_when_valid_name_creation(String email) {
        assertThatCode(() -> new Email(email))
                .doesNotThrowAnyException();
    }
}
