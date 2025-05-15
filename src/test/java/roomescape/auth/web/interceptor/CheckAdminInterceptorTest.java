package roomescape.auth.web.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.auth.dto.AuthenticatedMember;
import roomescape.auth.application.TokenService;
import roomescape.auth.web.constant.AuthConstant;
import roomescape.auth.web.support.AuthorizationExtractor;
import roomescape.member.model.Role;
import roomescape.global.exception.AuthorizationException;

@ExtendWith(MockitoExtension.class)
class CheckAdminInterceptorTest {

    @InjectMocks
    private CheckAdminInterceptor interceptor;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthorizationExtractor<String> authorizationExtractor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @DisplayName("admin 권한을 가진 사용자가 로그인한 상태라면, true를 반환한다")
    @Test
    void adminSuccess() throws Exception {
        // given
        String authCookieKey = AuthConstant.AUTH_COOKIE_KEY;
        String token = "adminToken";
        Cookie cookie = new Cookie(authCookieKey, token);
        request.setCookies(cookie);

        AuthenticatedMember authenticatedMember = new AuthenticatedMember(1L, "email", "어드민", Role.ADMIN);
        given(authorizationExtractor.extract(request)).willReturn(token);
        given(tokenService.resolveAuthenticatedMember(token)).willReturn(authenticatedMember);

        // when
        boolean isPass = interceptor.preHandle(request, response, null);

        // then
        assertThat(isPass).isTrue();
    }

    @DisplayName("admin 권한이 없는 사용자가 접근하면 예외를 발생시킨다")
    @Test
    void userFail() throws Exception {
        // given
        String authCookieKey = AuthConstant.AUTH_COOKIE_KEY;
        String token = "userToken";
        Cookie cookie = new Cookie(authCookieKey, token);
        request.setCookies(cookie);

        AuthenticatedMember authenticatedMember = new AuthenticatedMember(1L, "email", "어드민", Role.USER);
        given(authorizationExtractor.extract(request)).willReturn(token);
        given(tokenService.resolveAuthenticatedMember(token)).willReturn(authenticatedMember);

        // when & then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, null))
                .isInstanceOf(AuthorizationException.class);
    }
}
