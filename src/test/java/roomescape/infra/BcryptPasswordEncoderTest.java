package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BcryptPasswordEncoderTest {

    private BcryptPasswordEncoder bcryptPasswordEncoder;

    @BeforeEach
    void setUp() {
        bcryptPasswordEncoder = new BcryptPasswordEncoder();
    }

    @Test
    @DisplayName("비밀번호를 암호화한다.")
    void encode() {
        String encodedPassword = bcryptPasswordEncoder.encode("password");

        assertThat(encodedPassword).isNotEqualTo("password");
    }

    @Test
    @DisplayName("비밀번호가 일치하는지 확인한다.")
    void matches() {
        String rawPassword = "password";
        String encodedPassword = bcryptPasswordEncoder.encode(rawPassword);

        boolean result = bcryptPasswordEncoder.matches(rawPassword, encodedPassword);

        assertThat(result).isTrue();
    }
}
