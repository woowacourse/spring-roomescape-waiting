package roomescape.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.business.service.AuthService;
import roomescape.exception.UnauthorizedException;
import roomescape.presentation.dto.LoginMember;

@WebMvcTest(CheckAdminInterceptor.class)
class CheckAdminInterceptorTest {

    @Autowired
    private CheckAdminInterceptor checkAdminInterceptor;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MockHttpServletResponse mockHttpServletResponse;

    @DisplayName("어드민 권한이 담긴 토큰일 때 true를 반환한다")
    @Test
    void preHandle() {
        //given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String accessToken = "valid-access-token";
        final Cookie cookie = new Cookie("token", accessToken);
        request.setCookies(cookie);

        final LoginMember adminMember = new LoginMember(1L, "name", "email", "ADMIN");
        when(authService.getLoginMemberByAccessToken(accessToken)).thenReturn(adminMember);

        //when
        final boolean result = checkAdminInterceptor.preHandle(request, mockHttpServletResponse, new Object());

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("사용자 권한이 담긴 토큰일 때 예외가 발생한다")
    @Test
    void preHandleWhenRoleIsUser() {
        //given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String accessToken = "valid-access-token";
        final Cookie cookie = new Cookie("token", accessToken);
        request.setCookies(cookie);

        final LoginMember userMember = new LoginMember(1L, "name", "email", "USER");
        when(authService.getLoginMemberByAccessToken(accessToken)).thenReturn(userMember);

        //when & then
        assertThatThrownBy(() -> checkAdminInterceptor.preHandle(request, mockHttpServletResponse, new Object()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("접근 권한이 부족합니다.");
    }

    @DisplayName("쿠키가 null일 때 예외가 발생한다")
    @Test
    void preHandleWhenNotExistsCookie() {
        //given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final LoginMember loginMember = new LoginMember(1L, "name", "email", "USER");

        when(authService.getLoginMemberByAccessToken(any(String.class))).thenReturn(loginMember);

        //when & then
        assertThatThrownBy(() -> checkAdminInterceptor.preHandle(request, mockHttpServletResponse, new Object()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인 정보가 없습니다.");
    }

    @DisplayName("쿠키에 토큰 정보가 안담겼을 때 예외가 발생한다")
    @Test
    void preHandleWhenNotExistsAccessToken() {
        //given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String notTokenCookie = "not-token-cookie";
        final Cookie cookie = new Cookie("not-token", notTokenCookie);
        request.setCookies(cookie);

        final LoginMember loginMember = new LoginMember(1L, "name", "email", "USER");

        when(authService.getLoginMemberByAccessToken(any(String.class))).thenReturn(loginMember);

        //when & then
        assertThatThrownBy(() -> checkAdminInterceptor.preHandle(request, mockHttpServletResponse, new Object()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인 정보가 없습니다.");
    }
}
