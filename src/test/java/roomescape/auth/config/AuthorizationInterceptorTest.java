package roomescape.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
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
import roomescape.auth.AuthToken;
import roomescape.auth.LoginInfo;
import roomescape.auth.Role;
import roomescape.auth.config.AuthorizationInterceptor;
import roomescape.auth.jwt.JwtUtil;
import roomescape.business.model.vo.UserRole;
import roomescape.exception.auth.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AuthorizationInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthorizationInterceptor sut;

    @Test
    void 핸들러_메서드가_아닌_경우_true를_반환한다() throws Exception {
        // given
        Object handler = new Object();

        // when
        boolean result = sut.preHandle(request, response, handler);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void Role_어노테이션이_없는_경우_true를_반환한다() throws Exception {
        // given
        given(handlerMethod.getMethodAnnotation(Role.class)).willReturn(null);

        // when
        boolean result = sut.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 허용된_역할의_사용자는_true를_반환한다() throws Exception {
        // given
        String token = "valid.value";
        Cookie cookie = new Cookie("authToken", token);
        Role role = createRoleAnnotation(UserRole.ADMIN);
        LoginInfo loginInfo = new LoginInfo("test@test.com", UserRole.ADMIN);

        given(handlerMethod.getMethodAnnotation(Role.class)).willReturn(role);
        given(request.getCookies()).willReturn(new Cookie[]{cookie});
        given(jwtUtil.validateAndResolveToken(new AuthToken(token))).willReturn(loginInfo);

        // when
        boolean result = sut.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        verify(request).setAttribute("authorization", loginInfo);
    }

    @Test
    void 허용되지_않은_역할의_사용자는_예외를_던진다() {
        // given
        String token = "valid.value";
        Cookie cookie = new Cookie("authToken", token);
        Role role = createRoleAnnotation(UserRole.ADMIN);
        LoginInfo loginInfo = new LoginInfo("test@test.com", UserRole.USER);

        given(handlerMethod.getMethodAnnotation(Role.class)).willReturn(role);
        given(request.getCookies()).willReturn(new Cookie[]{cookie});
        given(jwtUtil.validateAndResolveToken(new AuthToken(token))).willReturn(loginInfo);

        // when, then
        assertThatThrownBy(() -> sut.preHandle(request, response, handlerMethod))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 쿠키가_없는_경우_예외를_던진다() {
        // given
        Role role = createRoleAnnotation(UserRole.ADMIN);
        given(handlerMethod.getMethodAnnotation(Role.class)).willReturn(role);
        given(request.getCookies()).willReturn(null);

        // when, then
        assertThatThrownBy(() -> sut.preHandle(request, response, handlerMethod))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authToken_쿠키가_없는_경우_예외를_던진다() {
        // given
        Role role = createRoleAnnotation(UserRole.ADMIN);
        Cookie cookie = new Cookie("otherCookie", "value");

        given(handlerMethod.getMethodAnnotation(Role.class)).willReturn(role);
        given(request.getCookies()).willReturn(new Cookie[]{cookie});

        // when, then
        assertThatThrownBy(() -> sut.preHandle(request, response, handlerMethod))
                .isInstanceOf(AuthenticationException.class);
    }

    private Role createRoleAnnotation(UserRole... roles) {
        return new Role() {
            @Override
            public Class<Role> annotationType() {
                return Role.class;
            }

            @Override
            public UserRole[] value() {
                return roles;
            }
        };
    }
}
