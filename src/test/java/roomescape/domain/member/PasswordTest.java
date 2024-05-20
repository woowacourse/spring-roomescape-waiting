package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {

    @ParameterizedTest
    @ValueSource(ints = {7, 21})
    @DisplayName("비밀번호의 길이가 8자에서 20자 사이가 아닌 경우 예외를 발생한다.")
    void invalidLengthTest(int length) {
        String rawPassword = "a".repeat(length);
        assertThatCode(() -> new Password(rawPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 8자 이상 20자 이하여야 합니다.");
    }

    @Test
    @DisplayName("비밀번호가 올바른지 확인한다.")
    void validPasswordTest() {
        String rawPassword = "12345678";
        Password password = new Password(rawPassword);
        boolean matches = password.matches(rawPassword);
        assertThat(matches).isTrue();
    }
}
