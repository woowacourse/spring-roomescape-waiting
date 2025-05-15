package roomescape.member.login.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_ROLE = "USER";
    private static final String TEST_SECRET_KEY = "testSecretKey123456789012345678901234567890";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        setField(jwtTokenProvider, "secretKey", TEST_SECRET_KEY);
    }

    @DisplayName("JWT 토큰을 생성하고 페이로드에서 이메일을 추출할 수 있다.")
    @Test
    void create_token_and_extract_email() {
        String token = jwtTokenProvider.createToken(TEST_EMAIL, TEST_ROLE);

        String extractedEmail = jwtTokenProvider.getPayloadEmail(token);

        assertThat(extractedEmail).isEqualTo(TEST_EMAIL);
    }

    @DisplayName("JWT 토큰을 생성하고 페이로드에서 역할을 추출할 수 있다.")
    @Test
    void create_token_and_extract_role() {
        String token = jwtTokenProvider.createToken(TEST_EMAIL, TEST_ROLE);

        String extractedRole = jwtTokenProvider.getPayloadRole(token);

        assertThat(extractedRole).isEqualTo(TEST_ROLE);
    }
} 
