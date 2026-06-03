package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Role;

class JwtTokenProviderTest {

    private static final String SECRET = "/BWxvVt/eMsTVSq+RI9kRCrZKK38KNGIWi7ilxCg9So=";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 3600000L);

    @Test
    @DisplayName("토큰을 발급하고 userId와 username을 복원한다")
    void createsTokenAndRestoresUserIdAndUsername() {
        String token = provider.createToken(1L, "brown@test.com", Role.MEMBER);

        assertThat(provider.getUserId(token)).isEqualTo(1L);
        assertThat(provider.getUsername(token)).isEqualTo("brown@test.com");
    }

    @Test
    @DisplayName("위조된 토큰이면 예외를 던진다")
    void throwsExceptionWhenTokenIsForged() {
        assertThatThrownBy(() -> provider.getUsername("not-a-jwt"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("만료된 토큰이면 예외를 던진다")
    void throwsExceptionWhenTokenIsExpired() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1000L);
        String expiredToken = expiredProvider.createToken(1L, "brown@test.com", Role.MEMBER);

        assertThatThrownBy(() -> provider.getUsername(expiredToken))
                .isInstanceOf(RuntimeException.class);
    }
}
