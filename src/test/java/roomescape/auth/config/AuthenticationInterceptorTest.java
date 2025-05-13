package roomescape.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;
import roomescape.auth.AuthRequired;
import roomescape.auth.AuthToken;
import roomescape.auth.LoginInfo;
import roomescape.auth.config.AuthenticationInterceptor;
import roomescape.auth.jwt.JwtUtil;
import roomescape.exception.auth.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AuthenticationInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationInterceptor sut;

    @Test
    void 핸들러_메서드가_아니면_true를_반환한다() {
        // given
        Object handler = new Object();

        // when
        boolean result = sut.preHandle(request, response, handler);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 인증이_필요하지_않은_핸들러_메서드는_true를_반환한다() {
        // given
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        given(handlerMethod.getMethodAnnotation(AuthRequired.class)).willReturn(null);

        // when
        boolean result = sut.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 유효한_토큰으로_인증에_성공하면_true를_반환한다() {
        // given
        String token = "valid.jwt.value";
        Cookie cookie = new Cookie("authToken", token);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        LoginInfo loginInfo = mock(LoginInfo.class);

        given(handlerMethod.getMethodAnnotation(AuthRequired.class)).willReturn(mock(AuthRequired.class));
        given(request.getCookies()).willReturn(new Cookie[]{cookie});
        given(jwtUtil.validateAndResolveToken(new AuthToken(token))).willReturn(loginInfo);

        // when
        boolean result = sut.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        verify(request).setAttribute("authorization", loginInfo);
    }

    @Test
    void 쿠키가_없으면_예외를_던진다() {
        // given
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        given(handlerMethod.getMethodAnnotation(AuthRequired.class)).willReturn(mock(AuthRequired.class));
        given(request.getCookies()).willReturn(null);

        // when, then
        assertThatThrownBy(() -> sut.preHandle(request, response, handlerMethod))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 유효하지_않은_토큰이면_예외를_던진다() {
        // given
        String token = "invalid.jwt.value";
        Cookie cookie = new Cookie("authToken", token);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);

        given(handlerMethod.getMethodAnnotation(AuthRequired.class)).willReturn(mock(AuthRequired.class));
        given(request.getCookies()).willReturn(new Cookie[]{cookie});
        given(jwtUtil.validateAndResolveToken(new AuthToken(token))).willThrow(AuthenticationException.class);

        // when, then
        assertThatThrownBy(() -> sut.preHandle(request, response, handlerMethod))
                .isInstanceOf(AuthenticationException.class);
    }
}
