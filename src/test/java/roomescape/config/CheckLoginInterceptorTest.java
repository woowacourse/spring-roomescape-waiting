package roomescape.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import roomescape.domain.Role;
import roomescape.exception.AuthenticationException;
import roomescape.service.auth.AuthService;
import roomescape.service.dto.AuthInfo;

class CheckLoginInterceptorTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = mock(Object.class);
    }

    @DisplayName("로그인 정보가 존재하지 않으면 false를 반환한다.")
    @Test
    void preHandleWithNullAuthInfo() throws Exception {
        // given
        CheckLoginInterceptor checkLoginInterceptor = new CheckLoginInterceptor(authService);

        // when
        when(authService.getAuthInfo(any())).thenReturn(null);

        // then
        assertThat(checkLoginInterceptor.preHandle(request, response, handler)).isFalse();
    }

    @DisplayName("로그인 정보 확인 과정에서 예외가 발생되면 false 를 반환한다.")
    @Test
    void preHandleWithException() throws Exception {
        // given
        CheckLoginInterceptor checkLoginInterceptor = new CheckLoginInterceptor(authService);

        // when
        when(authService.getAuthInfo(any())).thenThrow(new AuthenticationException("test"));

        // then
        assertThat(checkLoginInterceptor.preHandle(request, response, handler)).isFalse();
    }

    @DisplayName("로그인 정보가 존재하면 true를 반환한다.")
    @Test
    void preHandleWithValidAuthInfo() throws Exception {
        // given
        CheckLoginInterceptor checkLoginInterceptor = new CheckLoginInterceptor(authService);

        // when
        when(authService.getAuthInfo(any())).thenReturn(new AuthInfo(1L, "test", Role.MEMBER));

        // then
        assertThat(checkLoginInterceptor.preHandle(request, response, handler)).isTrue();
    }
}
