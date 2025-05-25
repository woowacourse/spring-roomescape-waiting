package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.infrastructure.BcryptPasswordEncoder;

class PasswordEncoderTest {

    PasswordEncoder passwordEncoder = new BcryptPasswordEncoder();

    @DisplayName("평문 비밀번호를 암호화한다")
    @Test
    void encode() {
        // given
        String rawPassword = "1234";

        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
    }

    @DisplayName("평문 비밀번호와 암호화된 비밀번호가 맞다면 true를 반환한다")
    @Test
    void match_same() {
        // given
        String rawPassword = "1234";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when & then
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @DisplayName("비밀번호가 다르다면 false를 반환한다")
    @Test
    void match_different() {
        // given
        String rawPassword = "1234";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        String wrongPassword = "1235";

        // when & then
        assertThat(passwordEncoder.matches(wrongPassword, encodedPassword));
    }
}
