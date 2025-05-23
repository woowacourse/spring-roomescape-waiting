//package roomescape.member.login.authorization;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import roomescape.auth.application.TokenProvider;
//import roomescape.auth.ui.AdminAuthorizationInterceptor;
//import roomescape.common.exception.AuthorizationException;
//import roomescape.common.security.TokenAuthorizationHandler;
//
//class AdminAuthorizationInterceptorTest {
//
//    @DisplayName("관리자 역할의 유효한 토큰이 주어지면 preHandle은 true를 반환한다")
//    @Test
//    void preHandle_returnsTrue_whenAdminTokenProvided() {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        TokenAuthorizationHandler tokenHandler = mock(TokenAuthorizationHandler.class);
//        TokenProvider jwtProvider = mock(TokenProvider.class);
//
//        when(tokenHandler.extractToken(request)).thenReturn("valid-token");
//        when(jwtProvider.getPayloadRole("valid-token")).thenReturn("admin");
//
//        AdminAuthorizationInterceptor interceptor = new AdminAuthorizationInterceptor(tokenHandler, jwtProvider);
//
//        boolean result = interceptor.preHandle(request, response, new Object());
//
//        assertThat(result).isTrue();
//    }
//
//    @DisplayName("토큰이 null이면 AuthorizationException이 발생한다")
//    @Test
//    void preHandle_throwsException_whenTokenIsNull() {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        TokenAuthorizationHandler tokenHandler = mock(TokenAuthorizationHandler.class);
//        TokenProvider jwtProvider = mock(TokenProvider.class);
//
//        when(tokenHandler.extractToken(request)).thenReturn(null);
//
//        AdminAuthorizationInterceptor interceptor = new AdminAuthorizationInterceptor(tokenHandler, jwtProvider);
//
//        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
//                .isInstanceOf(AuthorizationException.class)
//                .hasMessageContaining("토큰이 존재하지 않습니다");
//    }
//
//    @DisplayName("관리자가 아닌 역할의 토큰이 주어지면 preHandle은 false를 반환한다")
//    @Test
//    void preHandle_returnsFalse_whenNotAdminRole() {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        TokenAuthorizationHandler tokenHandler = mock(TokenAuthorizationHandler.class);
//        TokenProvider jwtProvider = mock(TokenProvider.class);
//
//        when(tokenHandler.extractToken(request)).thenReturn("valid-token");
//        when(jwtProvider.getPayloadRole("valid-token")).thenReturn("user");
//
//        AdminAuthorizationInterceptor interceptor = new AdminAuthorizationInterceptor(tokenHandler, jwtProvider);
//
//        boolean result = interceptor.preHandle(request, response, new Object());
//
//        assertThat(result).isFalse();
//    }
//}
