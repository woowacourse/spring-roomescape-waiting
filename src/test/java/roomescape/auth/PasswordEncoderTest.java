package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new PasswordEncoder();

    @DisplayName("입력에 대해 인코딩된 값을 반환한다.")
    @Test
    void encode() {
        // given
        final String password = "password";

        // when
        final String actual = passwordEncoder.encode(password);

        // then
        assertThat(actual).isNotEqualTo(password);
    }

    @DisplayName("인코딩된 값과 기존 값을 비교하면 true를 반환한다.")
    @Test
    void matches() {
        // given
        final String password = "password";
        final String encodedPassword = passwordEncoder.encode(password);

        // when
        final boolean actual = passwordEncoder.matches(password, encodedPassword);

        // then
        assertThat(actual).isTrue();
    }

    @DisplayName("인코딩된 값과 다른 password 값을 비교하면 false를 반환한다.")
    @Test
    void matches1() {
        // given
        final String password = "password";
        final String notMatchesPassword = "notMatchesPassword";
        final String encodedPassword = passwordEncoder.encode(password);

        // when
        final boolean actual = passwordEncoder.matches(notMatchesPassword, encodedPassword);

        // then
        assertThat(actual).isFalse();
    }
}
