package roomescape.auth.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.exception.AuthorizationException;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.MIA_EMAIL;

class JwtTokenProviderTest {
    private static final String TEST_SECRET_KEY = "TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST";
    private static final long TEST_VALIDITY = 1000000;

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(TEST_SECRET_KEY, TEST_VALIDITY);

    @Test
    @DisplayName("토큰을 생성한다.")
    void createToken() {
        // given & when
        String token = jwtTokenProvider.createToken(MIA_EMAIL);

        // then
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("토큰에서 페이로드를 얻는다.")
    void getPayload() {
        // given
        String token = jwtTokenProvider.createToken(MIA_EMAIL);

        // when
        String payload = jwtTokenProvider.getPayload(token);

        // then
        assertThat(payload).isEqualTo(MIA_EMAIL);
    }

    @Test
    @DisplayName("토큰이 만료되었을 경우 예외가 발생한다.")
    void validateExpiredToken() {
        // given
        Date now = new Date();
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET_KEY));
        String expiredToken = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(now)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("토큰의 형식이 잘못되었을 경우 예외가 발생한다.")
    void validateInvalidFormatToken() {
        // given
        String invalidFormatToken = "invalid token";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(invalidFormatToken))
                .isInstanceOf(AuthorizationException.class);
    }
}
