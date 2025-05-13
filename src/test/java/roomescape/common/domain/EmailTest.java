package roomescape.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class EmailTest {

    @Test
    @DisplayName("이메일이 null이면 예외가 발생한다")
    void validateNullEmail() {
        // when
        // then
        assertThatThrownBy(() -> Email.from(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking blank]: Email.value");
    }

    @Test
    @DisplayName("이메일이 빈 문자열이면 예외가 발생한다")
    void validateBlankEmail() {
        // when
        // then
        assertAll(() -> {
            assertThatThrownBy(() -> Email.from(""))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: Email.value");

            assertThatThrownBy(() -> Email.from(" "))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: Email.value");
        });
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 예외가 발생한다")
    void validateInvalidEmailFormat() {
        // when
        // then
        assertAll(() -> {
            assertThatThrownBy(() -> Email.from("invalid-email"))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking email]: Email.value");

            assertThatThrownBy(() -> Email.from("invalid@"))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking email]: Email.value");

            assertThatThrownBy(() -> Email.from("@invalid.com"))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking email]: Email.value");
        });
    }

    @Test
    @DisplayName("이메일 형식이 올바르면 Email 객체가 생성된다")
    void createValidEmail() {
        // when
        final Email email = Email.from("test@example.com");

        // then
        assertAll(() -> {
            assertThat(email).isNotNull();
            assertThat(email.getValue()).isEqualTo("test@example.com");
        });
    }
}
