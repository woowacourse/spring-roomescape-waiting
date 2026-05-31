package roomescape.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.exception.MemberException;

import static roomescape.member.exception.MemberExceptionInformation.PASSWORD_NOT_MATCH;

class PasswordTest {

    @Test
    @DisplayName("암호화 전, 비밀번호와 암호화 후, 비밀번호는 다르다.")
    void from() {
        // given
        String rawPassword = "1234";

        // when
        Password password = Password.from(rawPassword);
        String encryptedPassword = password.getValue();

        // then
        Assertions.assertThat(rawPassword)
                .isNotEqualTo(encryptedPassword);
    }

    @Test
    @DisplayName("암호화 전 비밀번호가 같으면, 암호화 비밀번호는 같다.")
    void same_encrytedPassword() {
        // given
        String rawPassword = "1234";
        Password password = Password.from(rawPassword);
        Password samePassword = Password.from(rawPassword);

        // when & then
        Assertions.assertThat(password.getValue())
                .isEqualTo(samePassword.getValue());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다.")
    void validate_matches() {
        // given
        String rawPassword = "1234";
        String wrongPassword = "abcd";
        Password password = Password.from(rawPassword);

        // when & then
        Assertions.assertThatThrownBy(() -> password.validateMatches(wrongPassword))
                .isInstanceOf(MemberException.class)
                .hasMessage(PASSWORD_NOT_MATCH.getMessage());
    }

}
