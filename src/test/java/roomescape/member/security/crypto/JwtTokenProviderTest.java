package roomescape.member.security.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.member.domain.Member;

@SpringBootTest
class JwtTokenProviderTest {

    private long validityInMilliseconds = 360000L;

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider("test-secret-key", validityInMilliseconds);

    private static final String USER_NAME = "도비";
    private static final String USER_TEST_COM = "user@test.com";
    private static final String USER_PASSWORD = "pass";
    private static final Member TEST_MEMBER = new Member(1L, USER_NAME, USER_TEST_COM, USER_PASSWORD);

    @Test
    @DisplayName("토큰을 생성하고, 토큰의 유효성을 검증하며, 페이로드를 반환한다")
    void testTokenCreationValidationAndPayloadExtraction() {
        // 토큰 발행
        Date now = new Date();
        String token = jwtTokenProvider.createToken(TEST_MEMBER, now);
        assertNotNull(token);

        // 토큰 검증
        boolean isValid = jwtTokenProvider.validateToken(token);
        assertTrue(isValid);

        // Payload 추출
        Map<String, String> payload = jwtTokenProvider.getPayload(token);
        assertNotNull(payload);
        assertEquals(USER_TEST_COM, payload.get("email"));
        assertEquals(USER_NAME, payload.get("name"));
        assertEquals("1", payload.get("id"));
    }

    @Test
    @DisplayName("만료된 토큰은 유효하지 않다고 반환한다")
    void testExpiredTokenShouldBeInvalid() {
        // 과거 토큰 발행
        Date now = new Date();
        Date past = new Date(now.getTime() - validityInMilliseconds - 1);
        String expiredToken = jwtTokenProvider.createToken(TEST_MEMBER, past);

        // 만료된 토큰 검증
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);
        assertFalse(isValid);
    }

}

