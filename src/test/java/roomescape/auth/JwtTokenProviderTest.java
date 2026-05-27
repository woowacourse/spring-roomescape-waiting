package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.exception.auth.ExpiredTokenException;
import roomescape.exception.auth.InvalidTokenException;

public class JwtTokenProviderTest {

    private static final String TEST_SECRET = "L0JXeHZWdC9lTXNUVlNxK1JJOWtSQ3JaS0szOEtOR0lXaTdpbHhDZzlTbz0=";
    private final JwtTokenProvider provider = new JwtTokenProvider(TEST_SECRET, 3_600_000L);

    @Test
    void 토큰을_발급하고_memberId를_추출한다() {
        String token = provider.createToken(1L);
        assertThat(provider.getMemberId(token)).isEqualTo(1L);
    }

    @Test
    void 발급한_토큰은_검증에_통과한다() {
        String token = provider.createToken(1L);
        assertThatCode(() -> provider.validateToken(token))
                .doesNotThrowAnyException();
    }

    @Test
    void 잘못된_시그니처는_InvalidTokenException을_던진다() {
        String token = provider.createToken(1L);
        String tampered = token + "X";
        assertThatThrownBy(() -> provider.validateToken(tampered))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void 만료된_토큰은_ExpiredTokenException을_던진다() throws InterruptedException {
        JwtTokenProvider shortLived = new JwtTokenProvider(TEST_SECRET, 1L);
        String token = shortLived.createToken(1L);
        Thread.sleep(10);
        assertThatThrownBy(() -> shortLived.validateToken(token))
                .isInstanceOf(ExpiredTokenException.class);
    }

    @Test
    void 형식이_잘못된_토큰은_InvalidTokenException을_던진다() {
        assertThatThrownBy(() -> provider.validateToken("not-a-jwt-at-all"))
                .isInstanceOf(InvalidTokenException.class);
    }
}
