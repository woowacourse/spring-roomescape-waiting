package roomescape.config.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.config.annotation.RequiredAccessToken;
import roomescape.domain.Role;
import roomescape.dto.business.AccessTokenContent;
import roomescape.dto.request.LoginRequest;
import roomescape.exception.global.AuthorizationException;
import roomescape.exception.local.NotFoundCookieException;
import roomescape.utility.CookieUtility;
import roomescape.utility.JwtTokenProvider;

class LoginMemberArgumentResolverTest {

    private final CookieUtility cookieUtility = new CookieUtility();
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test_secret_key_test_secret_key_test_secret_key_", 60000);
    private final LoginMemberArgumentResolver argumentResolver =
            new LoginMemberArgumentResolver(cookieUtility, jwtTokenProvider);

    @Nested
    @DisplayName("현재 인자 리졸버가 지원하는 파라리터인지 체크할 수 있다.")
    public class supportsParameter {

        @DisplayName("현재 인자 리졸바가 지원하는 파라미터의 경우 지원한다.")
        @Test
        void canSupportsParameter() {
            // given
            MethodParameter methodParameter = mock(MethodParameter.class);
            when(methodParameter.hasParameterAnnotation(RequiredAccessToken.class)).thenReturn(true);
            doReturn(AccessTokenContent.class).when(methodParameter).getParameterType();

            // when
            boolean isSupported = argumentResolver.supportsParameter(methodParameter);

            // then
            assertThat(isSupported).isTrue();
        }

        @DisplayName("애노테이션이 적용되지 않은 파라미터의 경우 지원하지 않는다.")
        @Test
        void cannotSupportsParameterByNoneAnnotation() {
            // given
            MethodParameter methodParameter = mock(MethodParameter.class);
            when(methodParameter.hasParameterAnnotation(RequiredAccessToken.class)).thenReturn(false);
            doReturn(AccessTokenContent.class).when(methodParameter).getParameterType();

            // when
            boolean isSupported = argumentResolver.supportsParameter(methodParameter);

            // then
            assertThat(isSupported).isFalse();
        }

        @DisplayName("타입이 다른 파라미터의 경우 지원하지 않느다.")
        @Test
        void cannotSupportsParameterByType() {
            // given
            MethodParameter methodParameter = mock(MethodParameter.class);
            when(methodParameter.hasParameterAnnotation(RequiredAccessToken.class)).thenReturn(true);
            doReturn(LoginRequest.class).when(methodParameter).getParameterType();

            // when
            boolean isSupported = argumentResolver.supportsParameter(methodParameter);

            // then
            assertThat(isSupported).isFalse();
        }
    }

    @Nested
    @DisplayName("쿠키에 담긴 엑세스 토큰을 파라미터에 담을 수 있다.")
    public class resolveArgument {

        @DisplayName("쿠키에 담긴 엑세스 토큰을 파라미터에 담을 수 있다.")
        @Test
        void canResolveArgument() {
            // given
            AccessTokenContent expectedTokenContent = new AccessTokenContent(1L, Role.ROLE_ADMIN, "회원");
            String accessToken = jwtTokenProvider.createAccessToken(expectedTokenContent);

            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", accessToken);
            when(servletRequest.getCookies()).thenReturn(new Cookie[]{cookie});

            MethodParameter parameter = mock(MethodParameter.class);
            ModelAndViewContainer modelAndViewContainer = mock(ModelAndViewContainer.class);
            NativeWebRequest request = mock(NativeWebRequest.class);
            when(request.getNativeRequest(HttpServletRequest.class)).thenReturn(servletRequest);
            WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);

            // when
            AccessTokenContent actualTokenContent =
                    argumentResolver.resolveArgument(parameter, modelAndViewContainer, request, binderFactory);

            // then
            assertThat(actualTokenContent).isEqualTo(expectedTokenContent);
        }

        @DisplayName("엑세스 토큰이 담긴 쿠키가 없는 경우 예외를 발생시킨다.")
        @Test
        void cannotResolveArgumentByNoneCookie() {
            // given
            HttpServletRequest servletRequest = mock(HttpServletRequest.class);

            MethodParameter parameter = mock(MethodParameter.class);
            ModelAndViewContainer modelAndViewContainer = mock(ModelAndViewContainer.class);
            NativeWebRequest request = mock(NativeWebRequest.class);
            when(request.getNativeRequest(HttpServletRequest.class)).thenReturn(servletRequest);
            WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);

            // when & then
            assertThatThrownBy(
                    () -> argumentResolver.resolveArgument(parameter, modelAndViewContainer, request, binderFactory))
                    .isInstanceOf(NotFoundCookieException.class)
                    .hasMessage("쿠키가 존재하지 않습니다.");
        }

        @DisplayName("엑세스 토큰이 올바르지 않는 경우 예외를 발생시킨다.")
        @Test
        void cannotResolveArgumentByWrongAccessToken() {
            AccessTokenContent expectedTokenContent = new AccessTokenContent(1L, Role.ROLE_ADMIN, "회원");
            String accessToken = jwtTokenProvider.createAccessToken(expectedTokenContent);
            String damagedAccessToken = accessToken + "damaged";

            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", damagedAccessToken);
            when(servletRequest.getCookies()).thenReturn(new Cookie[]{cookie});

            MethodParameter parameter = mock(MethodParameter.class);
            ModelAndViewContainer modelAndViewContainer = mock(ModelAndViewContainer.class);
            NativeWebRequest request = mock(NativeWebRequest.class);
            when(request.getNativeRequest(HttpServletRequest.class)).thenReturn(servletRequest);
            WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);

            // when & then
            assertThatThrownBy(
                    () -> argumentResolver.resolveArgument(parameter, modelAndViewContainer, request, binderFactory))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("토큰 파싱 실패");
        }
    }
}
