package roomescape.common.security.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import roomescape.common.config.JwtProperties;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.common.security.exception.ForbiddenException;
import roomescape.common.security.exception.UnAuthorizedException;
import roomescape.common.security.infrastructure.AuthorizationExtractor;
import roomescape.common.security.infrastructure.JwtProvider;
import roomescape.member.domain.MemberRole;

class RoleInterceptorTest {

    private static final String SECRET_KEY = "test-secret-key";
    private static final long VALIDITY_IN_MILLISECONDS = 1000L;
    private static final String ADMIN_PATH = "/admin/path";

    private RoleInterceptor roleInterceptor;
    private JwtProvider jwtProvider;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey(SECRET_KEY);
        jwtProperties.setExpireLength(VALIDITY_IN_MILLISECONDS);

        jwtProvider = new JwtProvider(jwtProperties);
        AuthorizationExtractor authorizationExtractor = new AuthorizationExtractor();
        roleInterceptor = new RoleInterceptor(authorizationExtractor, jwtProvider);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handlerMethod = mock(HandlerMethod.class);
    }

    @Test
    @DisplayName("토큰이 없는 경우 UnAuthorizedException이 발생한다.")
    void preHandle_NoToken() {
        // given
        when(request.getRequestURI()).thenReturn(ADMIN_PATH);
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getHeaders("Cookie")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> roleInterceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessage("토큰이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("ADMIN 경로에 REGULAR 권한으로 접근하면 ForbiddenException이 발생한다.")
    void preHandle_RegularUserAccessingAdminPath() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getRequestURI()).thenReturn("/admin/test");
        String token = jwtProvider.createToken(new MemberInfo(1L, MemberRole.REGULAR));
        when(request.getHeaders("Cookie")).thenReturn(
                java.util.Collections.enumeration(java.util.List.of("token=" + token)));

        // when & then
        assertThatThrownBy(() -> roleInterceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("접근할 수 없습니다.");
    }

    @Test
    @DisplayName("ADMIN 권한으로 ADMIN 경로에 접근하면 성공한다.")
    void preHandle_AdminUserAccessingAdminPath() throws Exception {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getRequestURI()).thenReturn("/admin/test");
        String token = jwtProvider.createToken(new MemberInfo(1L, MemberRole.ADMIN));
        when(request.getHeaders("Cookie")).thenReturn(
                java.util.Collections.enumeration(java.util.List.of("token=" + token)));

        // when
        boolean result = roleInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
    }
} 
