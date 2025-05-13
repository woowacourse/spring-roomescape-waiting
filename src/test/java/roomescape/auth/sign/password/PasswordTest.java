package roomescape.auth.sign.password;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class PasswordTest {

    private final PasswordEncoder encoder = new TempPasswordEncoder();

    @Test
    @DisplayName("비밀번호가 null이면 예외가 발생한다")
    void validateNullPassword() {
        // when
        // then
        assertAll(() -> {
            assertThatThrownBy(() -> Password.fromRaw(null, encoder))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: Password.encodedValue");

            assertThatThrownBy(() -> Password.fromEncoded(null))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: Password.encodedValue");
        });
    }

    @Test
    @DisplayName("비밀번호가 빈 문자열이면 예외가 발생한다")
    void validateBlankPassword() {
        // when
        // then
        assertAll(() -> {
            assertThatThrownBy(() -> Password.fromRaw("", encoder))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: Password.encodedValue");

            assertThatThrownBy(() -> Password.fromRaw(" ", encoder))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: Password.encodedValue");

            assertThatThrownBy(() -> Password.fromEncoded(""))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: Password.encodedValue");

            assertThatThrownBy(() -> Password.fromEncoded(" "))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: Password.encodedValue");
        });
    }

    @Test
    @DisplayName("유효한 비밀번호로 Password 객체를 생성할 수 있다")
    void createValidPassword() {
        // when
        Password password1 = Password.fromRaw("password123", encoder);
        Password password2 = Password.fromEncoded("encodedPassword");

        // then
        assertAll(() -> {
            assertThat(password1).isNotNull();
            assertThat(password1.getEncodedValue()).isEqualTo("password123" + TempPasswordEncoder.secretKey);

            assertThat(password2).isNotNull();
            assertThat(password2.getEncodedValue()).isEqualTo("encodedPassword");
        });
    }

    @Test
    @DisplayName("비밀번호가 일치하는지 확인할 수 있다")
    void matchPassword() {
        // given
        String rawPassword = "password123";
        Password password = Password.fromRaw(rawPassword, encoder);

        // when
        // then
        assertAll(() -> {
            assertThat(password.matches(rawPassword, encoder)).isTrue();
            assertThat(password.matches("wrongPassword", encoder)).isFalse();
        });
    }
}
