package roomescape.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.domain.user.UserRole;
import roomescape.infrastructure.JwtTokenHandler;
import roomescape.presentation.auth.AuthenticationTokenCookie;
import roomescape.presentation.auth.CheckAdminInterceptor;

class CheckAdminInterceptorTest {

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final AuthenticationTokenHandler tokenProvider = new JwtTokenHandler();
    private final HandlerInterceptor interceptor = new CheckAdminInterceptor(tokenProvider);

    @Test
    @DisplayName("쿠키에 유효한 토큰이 있고 토큰 내용의 role이 ADMIN인 경우 true를 반환한다.")
    void preHandle() throws Exception {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.ADMIN);
        var token = tokenProvider.createToken(authenticationInfo);
        var tokenCookie = AuthenticationTokenCookie.forResponse(token);

        // when
        request.setCookies(tokenCookie);
        boolean actual = interceptor.preHandle(request, response, new TestController());

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("쿠키에 유효한 토큰이 있고 토큰 내용의 role이 ADMIN이 아닌 경우 false를 반환한다.")
    void preHandleWhenUserIsNotAdmin() throws Exception {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.USER);
        var token = tokenProvider.createToken(authenticationInfo);
        var tokenCookie = AuthenticationTokenCookie.forResponse(token);

        // when
        request.setCookies(tokenCookie);
        boolean actual = interceptor.preHandle(request, response, new TestController());

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("쿠키에 토큰이 없다면 false를 반환한다.")
    void preHandleWhenNotExistsToken() throws Exception {
        boolean actual = interceptor.preHandle(request, response, new TestController());
        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("쿠키에 유효하지 않은 토큰이 있는 경우 false를 반환한다.")
    void preHandleWhenTokenIsInvalid() throws Exception {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.USER);
        var token = tokenProvider.createToken(authenticationInfo);

        var forgedToken = token.substring(0, token.length() - 1);
        var forgedTokenCookie = AuthenticationTokenCookie.forResponse(forgedToken);

        // when
        request.setCookies(forgedTokenCookie);
        boolean actual = interceptor.preHandle(request, response, new TestController());

        assertThat(actual).isFalse();
    }

    @Controller
    private static class TestController {

    }
}
