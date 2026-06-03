package roomescape.infrastructure;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("어노테이션이 없으면 토큰 없이도 통과한다")
    void passesWithoutTokenWhenNoAnnotation() {
        boolean result = interceptor.preHandle(request, response, handlerOf("noAuth"));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("HandlerMethod가 아니면 통과한다")
    void passesWhenNotHandlerMethod() {
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Nested
    @DisplayName("LoginRequired 핸들러")
    class LoginRequiredHandler {

        @Test
        @DisplayName("유효한 토큰이면 통과하고 userId와 role을 저장한다")
        void passesAndStoresUserIdAndRoleWhenTokenIsValid() {
            authenticateAs(1L, Role.MEMBER);

            boolean result = interceptor.preHandle(request, response, handlerOf("loginRequired"));

            assertThat(result).isTrue();
            assertThat(request.getAttribute(AuthContext.LOGIN_USER_ID)).isEqualTo(1L);
            assertThat(request.getAttribute(AuthContext.LOGIN_USER_ROLE)).isEqualTo(Role.MEMBER);
        }

        @Test
        @DisplayName("역할은 상관없이 MANAGER도 통과한다")
        void passesForManagerRegardlessOfRole() {
            authenticateAs(1L, Role.MANAGER);

            assertThat(interceptor.preHandle(request, response, handlerOf("loginRequired"))).isTrue();
        }

        @Test
        @DisplayName("토큰이 없으면 UnauthenticatedException")
        void throwsUnauthenticatedExceptionWhenTokenIsMissing() {
            assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerOf("loginRequired")))
                    .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.UNAUTHENTICATED);
        }

        @Test
        @DisplayName("토큰이 유효하지 않으면 UnauthenticatedException")
        void throwsUnauthenticatedExceptionWhenTokenIsInvalid() {
            request.addHeader("Authorization", "Bearer bad-token");

            assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerOf("loginRequired")))
                    .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.UNAUTHENTICATED);
        }
    }

    @Nested
    @DisplayName("AdminOnly 핸들러")
    class AdminOnlyHandler {

        @Test
        @DisplayName("메타 어노테이션이 해석되어 MANAGER는 통과한다")
        void passesForManagerWhenMetaAnnotationIsResolved() {
            authenticateAs(1L, Role.MANAGER);

            boolean result = interceptor.preHandle(request, response, handlerOf("adminOnly"));

            assertThat(result).isTrue();
            assertThat(request.getAttribute(AuthContext.LOGIN_USER_ID)).isEqualTo(1L);
        }

        @Test
        @DisplayName("MEMBER면 UnauthorizedException")
        void throwsUnauthorizedExceptionWhenMember() {
            authenticateAs(2L, Role.MEMBER);

            assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerOf("adminOnly")))
                    .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @Test
        @DisplayName("토큰이 없으면 UnauthenticatedException")
        void throwsUnauthenticatedExceptionWhenTokenIsMissing() {
            assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerOf("adminOnly")))
                    .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.UNAUTHENTICATED);
        }
    }

    @Nested
    @DisplayName("클래스 레벨 어노테이션")
    class ClassLevelAnnotation {

        @Test
        @DisplayName("클래스에 붙은 AdminOnly도 해석된다")
        void resolvesClassLevelAdminOnly() {
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
        @DisplayName("클래스에 붙은 AdminOnly에서 MEMBER는 거부된다")
        void rejectsMemberOnClassLevelAdminOnly() {
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
