package roomescape.infrastructure.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

class JwtTokenProviderTest {
    private static final String TEST_SECRET_KEY = "i-appreciate-your-kindness-her0807";

    @DisplayName("이메일로 토큰을 생성한다.")
    @Test
    void createTokenTest() {
        JwtTokenProvider jwtTokenProvider = createJwtTokenProvider(1000);
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        String payload = String.valueOf(1L);

        String token = jwtTokenProvider.createToken(payload);

        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        assertThat(payload).isEqualTo(subject);
    }

    @DisplayName("만료된 토큰의 payload를 추출하면 예외가 발생한다.")
    @Test
    void getPayloadExceptionTest() {
        JwtTokenProvider jwtTokenProvider = createJwtTokenProvider(-1);
        String payload = String.valueOf(1L);
        String expiredToken = jwtTokenProvider.createToken(payload);

        assertThatCode(() -> jwtTokenProvider.getPayload(expiredToken))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.TOKEN_EXPIRED);
    }

    private JwtTokenProvider createJwtTokenProvider(int validityInMilliseconds) {
        JwtTokenProperties jwtTokenProperties = new JwtTokenProperties();
        jwtTokenProperties.setSecretKey(TEST_SECRET_KEY);
        jwtTokenProperties.setExpireMilliseconds(validityInMilliseconds);
        return new JwtTokenProvider(jwtTokenProperties);
    }
}
