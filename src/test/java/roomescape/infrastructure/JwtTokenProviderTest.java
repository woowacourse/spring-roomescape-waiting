package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtTokenProviderTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("토큰 검증")
    void validateToken() {
        //given
        final String token = jwtTokenProvider.generateToken("redddy", "ADMIN");

        assertAll(
                () -> assertThat(jwtTokenProvider.validateToken(token)).isTrue(),
                () -> assertThat(jwtTokenProvider.validateToken("redddy")).isFalse(),
                () -> assertThat(jwtTokenProvider.validateToken(token + "i")).isFalse()
        );

    }
}
