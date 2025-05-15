package roomescape.presentation.interceptor;


import jakarta.servlet.http.Cookie;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.presentation.support.CookieUtils;
import roomescape.service.AuthService;

@ExtendWith(MockitoExtension.class)
class ReservationAuthInterceptorTest {
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
}
