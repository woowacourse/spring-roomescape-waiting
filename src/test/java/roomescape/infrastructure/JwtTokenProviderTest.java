package roomescape.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.IntegrationTestSupport;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class JwtTokenProviderTest extends IntegrationTestSupport {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("토큰 검증")
    void validateToken() {
        //given
        final Map<String, Object> payload = Map.of(
                "sub", 1L,
                "name", "redddy",
                "role", "ADMIN"
        );

        final String token = jwtTokenProvider.generateToken(payload);

        assertAll(
                () -> assertThat(jwtTokenProvider.validateToken(token)).isTrue(),
                () -> assertThat(jwtTokenProvider.validateToken("redddy")).isFalse(),
                () -> assertThat(jwtTokenProvider.validateToken(token + "i")).isFalse()
        );
    }
}
