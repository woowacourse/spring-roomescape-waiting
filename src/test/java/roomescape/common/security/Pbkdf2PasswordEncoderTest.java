package roomescape.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PBKDF2 비밀번호 인코더")
class Pbkdf2PasswordEncoderTest {

    private final Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder();

    @Test
    @DisplayName("암호화한 비밀번호는 같은 원문과 매칭된다")
    void matches() {
        // given
        String encoded = encoder.encode("password");

        // when & then
        assertThat(encoder.matches("password", encoded)).isTrue();
        assertThat(encoder.matches("wrong", encoded)).isFalse();
    }
}
