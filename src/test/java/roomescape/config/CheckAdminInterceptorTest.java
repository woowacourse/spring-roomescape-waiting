package roomescape.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import roomescape.exception.AuthorizationException;
import roomescape.service.auth.AuthService;
import roomescape.service.dto.AuthInfo;

class CheckAdminInterceptorTest {

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

    @DisplayName("어드민 유저가 아닌 경우 예외가 발생된다.")
    @Test
    void preHandle() {
        // given
        CheckAdminInterceptor checkAdminInterceptor = new CheckAdminInterceptor(authService);

        // when
        when(authService.getAuthInfo(any())).thenReturn(new AuthInfo(1L, "test", Role.MEMBER));

        // then
        assertThatThrownBy(() -> checkAdminInterceptor.preHandle(request, response, handler))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("관리자 권한이 없습니다. ID = 1, 이름 = test, 현재 권한 = MEMBER");
    }
}
