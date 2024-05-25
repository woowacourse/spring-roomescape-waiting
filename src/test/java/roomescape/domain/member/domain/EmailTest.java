package roomescape.domain.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.member.domain.Email.EMAIL_EMPTY_ERROR_MESSAGE;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ValueNullOrEmptyException;

class EmailTest {

    @DisplayName("이메일이 정상값인 값으로 생성할 수 있다.")
    @Test
    void should_creat_email() {
        assertThatCode(() -> new Email("dodo@gmail.com"))
                .doesNotThrowAnyException();
        ;
    }

    @DisplayName("이메일이 Null이면 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_email_is_null() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(ValueNullOrEmptyException.class)
                .hasMessage(EMAIL_EMPTY_ERROR_MESSAGE);
    }

    @DisplayName("이메일이 비어있으면 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_email_is_blank() {
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(ValueNullOrEmptyException.class)
                .hasMessage(EMAIL_EMPTY_ERROR_MESSAGE);
    }
}
