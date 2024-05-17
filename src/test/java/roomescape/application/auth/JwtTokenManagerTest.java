package roomescape.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Clock;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import roomescape.application.ServiceTest;
import roomescape.application.auth.dto.TokenPayload;
import roomescape.exception.ExpiredTokenException;
import roomescape.exception.InvalidTokenException;

@ServiceTest
class JwtTokenManagerTest {

    @Autowired
    private JwtTokenManager tokenManager;

    @Autowired
    private Clock clock;

    @Value("${jwt.secret}")
    private String TEST_TOKEN_SECRET;

    @Value("${jwt.expire-in-millis}")
    private long millis;

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("토큰이 비어있는 경우, 예외를 발생한다.")
    void invalidTokenOnBlankOrNull(String token) {
        assertThatCode(() -> tokenManager.extract(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("토큰에서 유저 아이디를 가져온다.")
    void extractFromCookiesTest() {
        String token = JWT.create()
                .withSubject("1")
                .withClaim("name", "test")
                .withClaim("role", "member")
                .withExpiresAt(Date.from(clock.instant().plusMillis(millis)))
                .sign(Algorithm.HMAC512(TEST_TOKEN_SECRET));
        TokenPayload payload = tokenManager.extract(token);
        assertThat(payload.memberId()).isEqualTo(1);
    }

    @Test
    @DisplayName("만료된 토큰을 가져오는 경우, 예외를 발생한다.")
    void expiredTokenTest() {
        Date expireDate = Date.from(clock.instant().minusMillis(millis + 1));
        String token = JWT.create()
                .withSubject("1")
                .withClaim("name", "test")
                .withClaim("role", "member")
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC512(TEST_TOKEN_SECRET));
        assertThatCode(() -> tokenManager.extract(token))
                .isInstanceOf(ExpiredTokenException.class);
    }
}
