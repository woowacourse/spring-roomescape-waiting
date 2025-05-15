package roomescape.member.login.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.AuthorizationException;

class TokenAuthorizationHandlerTest {

    private final TokenAuthorizationHandler handler = new TokenAuthorizationHandler();

    @DisplayName("Authorization 쿠키가 존재하면 해당 값을 반환한다")
    @Test
    void extractToken_returnsToken_whenAuthorizationCookieExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie("token", "valid-token")};
        when(request.getCookies()).thenReturn(cookies);

        String token = handler.extractToken(request);

        assertThat(token).isEqualTo("valid-token");
    }

    @DisplayName("쿠키가 null이면 예외를 던진다")
    @Test
    void extractToken_throwsException_whenCookiesAreNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> handler.extractToken(request))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("쿠키가 존재하지 않습니다");
    }

    @DisplayName("Authorization 쿠키가 없으면 예외를 던진다")
    @Test
    void extractToken_throwsException_whenAuthorizationCookieMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie("NotAuthorization", "other-token")};
        when(request.getCookies()).thenReturn(cookies);

        assertThatThrownBy(() -> handler.extractToken(request))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("토큰 값이 존재하지 않습니다");
    }

    @DisplayName("accessToken으로 쿠키를 생성해 응답에 추가한다")
    @Test
    void createCookie_addsHttpOnlyCookieToResponse() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        handler.createCookie("new-token", response);

        verify(response).addCookie(argThat(cookie ->
                cookie.getName().equals("token") &&
                        cookie.getValue().equals("new-token") &&
                        cookie.isHttpOnly() &&
                        cookie.getPath().equals("/")
        ));
    }

    @DisplayName("쿠키가 있으면 모든 쿠키를 삭제 처리한다")
    @Test
    void deleteCookie_setsCookiesToExpire() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Cookie[] cookies = {new Cookie("Authorization", "token-value")};
        when(request.getCookies()).thenReturn(cookies);

        handler.deleteCookie(request, response);

        verify(response).addCookie(argThat(cookie ->
                cookie.getName().equals("Authorization") &&
                        cookie.getValue().equals("") &&
                        cookie.getMaxAge() == 0 &&
                        cookie.getPath().equals("/")
        ));
    }

    @DisplayName("쿠키가 null이면 삭제 시도 중 예외가 발생한다")
    @Test
    void deleteCookie_throwsException_whenCookiesAreNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> handler.deleteCookie(request, response))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("쿠키가 존재하지 않습니다");
    }
}
