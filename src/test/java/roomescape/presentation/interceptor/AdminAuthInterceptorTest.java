package roomescape.presentation.interceptor;


import jakarta.servlet.http.Cookie;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.dto.response.MemberResponseDto;
import roomescape.model.Role;
import roomescape.presentation.support.CookieUtils;
import roomescape.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AdminAuthInterceptorTest {
    @Mock
    private AuthService authService;

    @Mock
    private CookieUtils cookieUtils;

    AdminAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AdminAuthInterceptor(authService, cookieUtils);
    }

    @Test
    @DisplayName("요청에 쿠키가 존재하지 않는 경우 false 를 반환한다")
    void test1() throws Exception {
        // given
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // when
        boolean result = interceptor.preHandle(servletRequest, servletResponse, mock(HandlerInterceptor.class));

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("요청에 토큰을 담는 키값이 존재하지 않는 경우 false 를 반환한다")
    void test2() throws Exception {
        // given
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(new Cookie("invalidValue", "temp"));
        NativeWebRequest webRequest = new ServletWebRequest(servletRequest);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // when
        boolean result = interceptor.preHandle(servletRequest, servletResponse, mock(HandlerInterceptor.class));

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("요청에 ADMIN 이 아닌 사용자의 정보를 담은 쿠키가 존재하는 경우 false 를 반환한다")
    void test3() throws Exception {
        // given
        String token = "tempToken";

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(new Cookie("token", token));
        NativeWebRequest webRequest = new ServletWebRequest(servletRequest);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        given(cookieUtils.containsCookieForToken(any())).willReturn(true);
        given(cookieUtils.getToken(any())).willReturn(token);
        given(authService.getMemberByToken(token))
                .willReturn(new MemberResponseDto(1L, "히로", "email@gmail.com", Role.USER));

        // when
        boolean result = interceptor.preHandle(servletRequest, servletResponse, mock(HandlerInterceptor.class));

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("요청에 ADMIN 사용자의 정보를 담은 쿠키가 존재하는 경우 true 를 반환한다")
    void test4() throws Exception {
        // given
        String token = "tempToken";

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(new Cookie("token", token));
        NativeWebRequest webRequest = new ServletWebRequest(servletRequest);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        given(cookieUtils.containsCookieForToken(any())).willReturn(true);
        given(cookieUtils.getToken(any())).willReturn(token);
        given(authService.getMemberByToken(token))
                .willReturn(new MemberResponseDto(1L, "히로", "email@gmail.com", Role.ADMIN));

        // when
        boolean result = interceptor.preHandle(servletRequest, servletResponse, mock(HandlerInterceptor.class));

        // then
        assertThat(result).isTrue();
    }
}
