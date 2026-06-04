package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.domain.Role;
import roomescape.exception.UnauthenticatedException;
import roomescape.exception.UnauthorizedException;

class AdminAuthorizationInterceptorTest {

    private static final String SECRET = "/BWxvVt/eMsTVSq+RI9kRCrZKK38KNGIWi7ilxCg9So=";

    private JwtTokenProvider jwtProvider;
    private AdminAuthorizationInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtTokenProvider(SECRET, 3600000L);
        interceptor = new AdminAuthorizationInterceptor(jwtProvider);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void MANAGER_사용자면_통과한다() {
        String token = jwtProvider.createToken(1L, "admin@test.com", Role.MANAGER);
        request.addHeader("Authorization", "Bearer " + token);

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Test
    void MEMBER_사용자면_UnauthorizedException() {
        String token = jwtProvider.createToken(2L, "brown@test.com", Role.MEMBER);
        request.addHeader("Authorization", "Bearer " + token);

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 토큰이_없으면_UnauthenticatedException() {
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(UnauthenticatedException.class);
    }
}
