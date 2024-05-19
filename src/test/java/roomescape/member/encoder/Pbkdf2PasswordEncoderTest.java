package roomescape.member.encoder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Pbkdf2PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = setPasswordEncoder();

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

    private PasswordEncoder setPasswordEncoder() {
        return new Pbkdf2PasswordEncoder(
                new byte[]{-45, -5, -13, 125, 108, -103, -122, -41, 107, -10, -60, 1, 73, -94, -118, 85},
                65536,
                256
        );
    }
}
