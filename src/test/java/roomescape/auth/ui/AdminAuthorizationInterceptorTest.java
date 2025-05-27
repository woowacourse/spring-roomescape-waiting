package roomescape.auth.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.auth.application.TokenProvider;
import roomescape.auth.exception.AccessForbiddenException;

class AdminAuthorizationInterceptorTest {

    TokenProvider tokenProvider = mock(TokenProvider.class);
    CookieProvider cookieProvider = mock(CookieProvider.class);
    AdminAuthorizationInterceptor interceptor = new AdminAuthorizationInterceptor(tokenProvider, cookieProvider);

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    @DisplayName("관리자는 접근을 허용한다")
    @Test
    void preHandle_adminAccess() {
        // given
        String token = "validToken";
        request.setCookies(new Cookie("token", token));

        when(cookieProvider.extractTokenFromCookie(any())).thenReturn(token);
        when(tokenProvider.getRoleName(token)).thenReturn("admin");

        // when & then
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @DisplayName("관리자가 아님녀 접근 불가하다")
    @Test
    void preHandle_notAdminAccess() {
        // given
        String token = "userToken";
        request.setCookies(new Cookie("token", token));

        when(cookieProvider.extractTokenFromCookie(any())).thenReturn(token);
        when(tokenProvider.getRoleName(token)).thenReturn("user");

        // when & then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(AccessForbiddenException.class);
    }
}
