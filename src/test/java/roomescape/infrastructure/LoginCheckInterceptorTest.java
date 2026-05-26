package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.domain.Role;
import roomescape.exception.UnauthenticatedException;

class LoginCheckInterceptorTest {

    private static final String SECRET = "/BWxvVt/eMsTVSq+RI9kRCrZKK38KNGIWi7ilxCg9So=";

    private JwtTokenProvider jwtProvider;
    private LoginCheckInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtTokenProvider(SECRET, 3600000L);
        interceptor = new LoginCheckInterceptor(jwtProvider);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void 유효한_토큰이면_통과하고_userId를_request에_저장한다() {
        String token = jwtProvider.createToken(1L, "brown@test.com", Role.MEMBER);
        request.addHeader("Authorization", "Bearer " + token);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(request.getAttribute(LoginCheckInterceptor.LOGIN_USER_ID)).isEqualTo(1L);
    }

    @Test
    void 토큰이_없으면_UnauthenticatedException() {
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(UnauthenticatedException.class);
    }

    @Test
    void 토큰이_유효하지_않으면_UnauthenticatedException() {
        request.addHeader("Authorization", "Bearer bad-token");

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(UnauthenticatedException.class);
    }
}
