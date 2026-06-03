package roomescape.infrastructure;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import roomescape.domain.Role;

class AuthInterceptorTest {

    private static final String SECRET = "/BWxvVt/eMsTVSq+RI9kRCrZKK38KNGIWi7ilxCg9So=";

    private JwtTokenProvider jwtProvider;
    private AuthInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtTokenProvider(SECRET, 3600000L);
        interceptor = new AuthInterceptor(jwtProvider);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private HandlerMethod handlerOf(String methodName) {
        try {
            Method method = TestController.class.getDeclaredMethod(methodName);
            return new HandlerMethod(new TestController(), method);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private void authenticateAs(Long userId, Role role) {
        String token = jwtProvider.createToken(userId, "user@test.com", role);
        request.addHeader("Authorization", "Bearer " + token);
    }

    @Test
    void 어노테이션이_없으면_토큰_없이도_통과한다() {
        boolean result = interceptor.preHandle(request, response, handlerOf("noAuth"));

        assertThat(result).isTrue();
    }

    @Test
    void HandlerMethod가_아니면_통과한다() {
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Nested
    class LoginRequired_핸들러 {

        @Test
        void 유효한_토큰이면_통과하고_userId와_role을_저장한다() {
            authenticateAs(1L, Role.MEMBER);

            boolean result = interceptor.preHandle(request, response, handlerOf("loginRequired"));

            assertThat(result).isTrue();
            assertThat(request.getAttribute(AuthContext.LOGIN_USER_ID)).isEqualTo(1L);
            assertThat(request.getAttribute(AuthContext.LOGIN_USER_ROLE)).isEqualTo(Role.MEMBER);
        }

        @Test
        void 역할은_상관없이_MANAGER도_통과한다() {
            authenticateAs(1L, Role.MANAGER);

            assertThat(interceptor.preHandle(request, response, handlerOf("loginRequired"))).isTrue();
        }

        @Test
        void 토큰이_없으면_UnauthenticatedException() {
            assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerOf("loginRequired")))
                    .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.UNAUTHENTICATED);
        }

        @Test
        void 토큰이_유효하지_않으면_UnauthenticatedException() {
            request.addHeader("Authorization", "Bearer bad-token");

            assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerOf("loginRequired")))
                    .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.UNAUTHENTICATED);
        }
    }

    @Nested
    class AdminOnly_핸들러 {

        @Test
        void 메타_어노테이션이_해석되어_MANAGER는_통과한다() {
            authenticateAs(1L, Role.MANAGER);

            boolean result = interceptor.preHandle(request, response, handlerOf("adminOnly"));

            assertThat(result).isTrue();
            assertThat(request.getAttribute(AuthContext.LOGIN_USER_ID)).isEqualTo(1L);
        }

        @Test
        void MEMBER면_UnauthorizedException() {
            authenticateAs(2L, Role.MEMBER);

            assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerOf("adminOnly")))
                    .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @Test
        void 토큰이_없으면_UnauthenticatedException() {
            assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerOf("adminOnly")))
                    .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.UNAUTHENTICATED);
        }
    }

    @Nested
    class 클래스_레벨_어노테이션 {

        @Test
        void 클래스에_붙은_AdminOnly도_해석된다() {
            authenticateAs(1L, Role.MANAGER);
            Method method;
            try {
                method = AdminController.class.getDeclaredMethod("handle");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
            HandlerMethod handler = new HandlerMethod(new AdminController(), method);

            assertThat(interceptor.preHandle(request, response, handler)).isTrue();
        }

        @Test
        void 클래스에_붙은_AdminOnly에서_MEMBER는_거부된다() {
            authenticateAs(2L, Role.MEMBER);
            Method method;
            try {
                method = AdminController.class.getDeclaredMethod("handle");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
            HandlerMethod handler = new HandlerMethod(new AdminController(), method);

            assertThatThrownBy(() -> interceptor.preHandle(request, response, handler))
                    .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.UNAUTHORIZED);
        }
    }

    static class TestController {

        public void noAuth() {
        }

        @LoginRequired
        public void loginRequired() {
        }

        @AdminOnly
        public void adminOnly() {
        }
    }

    @AdminOnly
    static class AdminController {

        public void handle() {
        }
    }
}