package roomescape.config.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.auth.controller.TokenCookieManager;
import roomescape.auth.dto.LoggedInMember;
import roomescape.auth.exception.AdminAuthorizationException;
import roomescape.auth.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AdminAuthorizationInterceptorTest {
    @Mock
    private TokenCookieManager tokenCookieManager;
    @Mock
    private AuthService authService;
    @InjectMocks
    private AdminAuthorizationInterceptor adminAuthorizationInterceptor;

    @DisplayName("관리자는 접근이 가능하다.")
    @Test
    void preHandleTest_whenMemberIsAdmin() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        LoggedInMember member = new LoggedInMember(1L, "관리자", "admin@abc.com", true);
        given(tokenCookieManager.getToken(any())).willReturn(null);
        given(authService.findLoggedInMember(any())).willReturn(member);

        boolean actual = adminAuthorizationInterceptor.preHandle(request, response, null);

        assertThat(actual).isTrue();
    }

    @DisplayName("일반 유저는 접근이 가능하다.")
    @Test
    void preHandleTest_whenMemberIsUser() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        LoggedInMember member = new LoggedInMember(1L, "브리", "bri@abc.com", false);
        given(tokenCookieManager.getToken(any())).willReturn(null);
        given(authService.findLoggedInMember(any())).willReturn(member);

        assertThatThrownBy(() -> adminAuthorizationInterceptor.preHandle(request, response, null))
                .isInstanceOf(AdminAuthorizationException.class);
    }
}
