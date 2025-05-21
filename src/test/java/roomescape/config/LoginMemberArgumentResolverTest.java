package roomescape.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.business.service.AuthService;
import roomescape.exception.UnauthorizedException;
import roomescape.presentation.dto.LoginMember;

@WebMvcTest(LoginMemberArgumentResolver.class)
class LoginMemberArgumentResolverTest {

    @Autowired
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MethodParameter methodParameter;

    @MockitoBean
    private ModelAndViewContainer modelAndViewContainer;

    @MockitoBean
    private WebDataBinderFactory webDataBinderFactory;

    @DisplayName("쿠키에 토큰 정보가 담겼을 때 로그인 사용자의 정보를 반환한다")
    @Test
    void resolveArgument() {
        //given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String accessToken = "valid-access-token";
        final Cookie cookie = new Cookie("token", accessToken);
        request.setCookies(cookie);

        final NativeWebRequest webRequest = new ServletWebRequest(request);
        final LoginMember expectedLoginMember = new LoginMember(1L, "name", "email", "role");

        when(authService.getLoginMemberByAccessToken(accessToken)).thenReturn(expectedLoginMember);

        //when
        final LoginMember result = (LoginMember) loginMemberArgumentResolver.resolveArgument(methodParameter,
                modelAndViewContainer, webRequest, webDataBinderFactory);

        //then
        assertThat(result).isEqualTo(expectedLoginMember);
    }

    @DisplayName("쿠키가 null일 때 예외가 발생한다")
    @Test
    void resolveArgumentWhenNotExistsCookie() {
        //given
        final MockHttpServletRequest request = new MockHttpServletRequest();

        final NativeWebRequest webRequest = new ServletWebRequest(request);
        final LoginMember expectedLoginMember = new LoginMember(1L, "name", "email", "role");

        when(authService.getLoginMemberByAccessToken(any(String.class))).thenReturn(expectedLoginMember);

        //when & then
        assertThatThrownBy(() -> loginMemberArgumentResolver.resolveArgument(methodParameter,
                modelAndViewContainer, webRequest, webDataBinderFactory))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인 정보가 없습니다.");
    }

    @DisplayName("쿠키에 토큰 정보가 안담겼을 때 예외가 발생한다")
    @Test
    void resolveArgumentWhenNotExistsAccessToken() {
        //given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String notTokenCookie = "not-token-cookie";
        final Cookie cookie = new Cookie("not-token", notTokenCookie);
        request.setCookies(cookie);

        final NativeWebRequest webRequest = new ServletWebRequest(request);
        final LoginMember expectedLoginMember = new LoginMember(1L, "name", "email", "role");

        when(authService.getLoginMemberByAccessToken(any(String.class))).thenReturn(expectedLoginMember);

        //when & then
        assertThatThrownBy(() -> loginMemberArgumentResolver.resolveArgument(methodParameter,
                modelAndViewContainer, webRequest, webDataBinderFactory))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인 정보가 없습니다.");
    }
}
