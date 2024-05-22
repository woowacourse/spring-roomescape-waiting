package roomescape.member.encoder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Pbkdf2PasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("평문 암호를 인코딩한다.")
    @Test
    void encodeTest() {
        // Given
        final String plainPassword = "kellyPw1234";

        // When
        final String encodedPassword = passwordEncoder.encode(plainPassword);

        // Then
        assertThat(encodedPassword).isNotEqualTo(plainPassword);
    }

    @DisplayName("평문 암호와 인코딩된 암호를 비교한다.")
    @Test
    void matchesTest() {
        // Given
        final String plainPassword = "kellyPw1234";
        final String encodedPassword = passwordEncoder.encode(plainPassword);

        // When
        final boolean isMatch = passwordEncoder.matches(plainPassword, encodedPassword);

        // Then
        assertThat(isMatch).isTrue();
    }
}
