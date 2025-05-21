package roomescape.auth.infrastructure;

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
import roomescape.auth.config.JwtProperties;
import roomescape.auth.dto.MemberInfo;
import roomescape.auth.exception.ForbiddenException;
import roomescape.auth.exception.UnAuthorizedException;
import roomescape.auth.interceptor.RoleInterceptor;
import roomescape.member.domain.MemberRole;

class RoleInterceptorTest {

    private static final String SECRET_KEY = "test-secret-key";
    private static final long VALIDITY_IN_MILLISECONDS = 1000L;
    private static final String MEMBER_PATH = "/path";
    private static final String ADMIN_PATH = "/admin/path";

    private RoleInterceptor roleInterceptor;
    private JwtProvider jwtProvider;
    private HttpServletRequest request;
    private HttpServletResponse response;

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
    }

    @Test
    @DisplayName("OPTIONS 요청은 항상 true를 반환한다.")
    void preHandle_OptionsRequest() throws Exception {
        // given
        when(request.getRequestURI()).thenReturn(MEMBER_PATH);
        when(request.getMethod()).thenReturn(HttpMethod.OPTIONS.name());

        // when
        boolean result = roleInterceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("토큰이 없는 경우 UnAuthorizedException이 발생한다.")
    void preHandle_NoToken() {
        // given
        when(request.getRequestURI()).thenReturn(ADMIN_PATH);
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getHeaders("Cookie")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> roleInterceptor.preHandle(request, response, null))
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
        assertThatThrownBy(() -> roleInterceptor.preHandle(request, response, null))
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
        boolean result = roleInterceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("REGULAR 권한으로 REGULAR 경로에 접근하면 성공한다.")
    void preHandle_RegularAccessingAdminPath() throws Exception {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getRequestURI()).thenReturn("/test");
        String token = jwtProvider.createToken(new MemberInfo(1L, MemberRole.REGULAR));
        when(request.getHeaders("Cookie")).thenReturn(
                java.util.Collections.enumeration(java.util.List.of("token=" + token)));

        // when
        boolean result = roleInterceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
    }
} 
