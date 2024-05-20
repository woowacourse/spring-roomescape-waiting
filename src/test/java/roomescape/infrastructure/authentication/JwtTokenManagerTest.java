package roomescape.infrastructure.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@ExtendWith(MockitoExtension.class)
class JwtTokenManagerTest {

    @InjectMocks
    private JwtTokenManager jwtTokenManager;

    @Mock
    private HttpServletResponse response;

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

    @DisplayName("쿠키가 null인 경우 예외가 발생한다.")
    @Test
    void extractTokenNullTest() {
        JwtTokenManager jwtTokenManager = new JwtTokenManager();
        Cookie[] cookies = null;

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

    @DisplayName("쿠키에 token 키로 accessToken 을 담는다.")
    @Test
    void setCookieTest() {
        jwtTokenManager.setToken(response, "accessToken");

        ArgumentCaptor<Cookie> cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieArgumentCaptor.capture());

        Cookie cookie = cookieArgumentCaptor.getValue();

        assertThat(cookie.getName()).isEqualTo("token");
        assertThat(cookie.getValue()).isEqualTo("accessToken");
    }
}
