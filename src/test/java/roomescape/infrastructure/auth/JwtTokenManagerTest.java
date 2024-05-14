package roomescape.infrastructure.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public class JwtTokenManagerTest {

    @DisplayName("쿠키에서 토큰을 추출한다")
    @Test
    void extractTokenTest() {
        JwtTokenManager jwtTokenManager = new JwtTokenManager();
        Cookie cookie = new Cookie("token", "value");
        Cookie[] cookies = new Cookie[]{cookie};

        String accessToken = jwtTokenManager.extractToken(cookies);

        assertThat(accessToken).isEqualTo("value");
    }

    @DisplayName("쿠키가 비어있는 경우 예외가 발생한다.")
    @Test
    void extractEmptyTokenTest() {
        JwtTokenManager jwtTokenManager = new JwtTokenManager();
        Cookie[] cookies = new Cookie[1];

        assertThatCode(() -> jwtTokenManager.extractToken(cookies))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.UNAUTHORIZED);
    }

    @DisplayName("쿠키에 토큰이 담겨있지 않은 경우 예외가 발생한다.")
    @Test
    void extractNotFoundTokenTest() {
        JwtTokenManager jwtTokenManager = new JwtTokenManager();
        Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie("test", "value");

        assertThatCode(() -> jwtTokenManager.extractToken(cookies))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.UNAUTHORIZED);
    }
}
