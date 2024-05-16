package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.member.domain.Email.INVALID_EMAIL_FORMAT;
import static roomescape.member.domain.Email.INVALID_EMAIL_LENGTH;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exceptions.ValidationException;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"invalid@.com", "invalidEmail", "example.com", "example@", "invalid.email@com"})
    @DisplayName("이메일 주소 형식이 올바르지 않을 경우 예외가 발생한다.")
    void throwExceptionWithoutAtSymbol(String email) {
        assertThatThrownBy(() -> new Email(email)).isInstanceOf(ValidationException.class)
                .hasMessageContaining(INVALID_EMAIL_FORMAT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcd@email.com", "toolongemailwhichover30lengthhh@email.com"})
    @DisplayName("localPart의 길이가 5자 미만 또는 30자 초과일 경우 예외가 발생한다.")
    void throwExceptionInvalidLocalPartLength(String email) {
        assertThatThrownBy(() -> new Email(email)).isInstanceOf(ValidationException.class)
                .hasMessageContaining(INVALID_EMAIL_LENGTH);
    }

    @ParameterizedTest
    @ValueSource(strings = {"example@example.com", "user123@example.com"})
    @DisplayName("유효한 이메일 주소는 예외가 발생하지 않는다.")
    void validEmailDoesNotThrowException(String email) {
        assertThatCode(() -> new Email(email)).doesNotThrowAnyException();
    }
}
