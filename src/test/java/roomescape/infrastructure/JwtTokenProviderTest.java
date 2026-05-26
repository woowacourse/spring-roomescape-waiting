package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.Role;

class JwtTokenProviderTest {

    private static final String SECRET = "/BWxvVt/eMsTVSq+RI9kRCrZKK38KNGIWi7ilxCg9So=";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 3600000L);

    @Test
    void 토큰을_발급하고_userId와_username을_복원한다() {
        String token = provider.createToken(1L, "brown@test.com", Role.MEMBER);

        assertThat(provider.getUserId(token)).isEqualTo(1L);
        assertThat(provider.getUsername(token)).isEqualTo("brown@test.com");
    }

    @Test
    void 위조된_토큰이면_예외를_던진다() {
        assertThatThrownBy(() -> provider.getUsername("not-a-jwt"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void 만료된_토큰이면_예외를_던진다() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1000L);
        String expiredToken = expiredProvider.createToken(1L, "brown@test.com", Role.MEMBER);

        assertThatThrownBy(() -> provider.getUsername(expiredToken))
                .isInstanceOf(RuntimeException.class);
    }
}