package roomescape.config.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.Role;
import roomescape.dto.business.AccessTokenContent;
import roomescape.exception.global.ForbiddenException;
import roomescape.utility.CookieUtility;
import roomescape.utility.JwtTokenProvider;

class CheckAdminInterceptorTest {

    private CookieUtility cookieUtility = new CookieUtility();
    private JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test_secret_key_test_secret_key_test_secret_key_", 60000);
    private CheckAdminInterceptor interceptor = new CheckAdminInterceptor(cookieUtility, jwtTokenProvider);

    @Nested
    @DisplayName("어드민 요청에 대해 어드민이 아닌 경우 블록할 수 있다.")
    public class perHandle {

        @DisplayName("어드민 요청인데 일반 회원이 접속한 경우 블록")
        @Test
        void blockWithAdminRequestAndMemberToken() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.ROLE_MEMBER, "회원");
            String memberToken = jwtTokenProvider.createAccessToken(memberTokenContent);

            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", memberToken);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            when(request.getRequestURI()).thenReturn("/admin/test");

            HttpServletResponse response = mock(HttpServletResponse.class);
            Object handler = mock(Object.class);

            // when & then
            assertThatThrownBy(() -> interceptor.preHandle(request, response, handler))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("접근 권한이 없습니다.");
        }

        @DisplayName("어드민 요청인데 어드민이 접속한 경우 통과")
        @Test
        void passWithAdminRequestAndAdminToken() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.ROLE_ADMIN, "회원");
            String memberToken = jwtTokenProvider.createAccessToken(memberTokenContent);

            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", memberToken);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            when(request.getRequestURI()).thenReturn("/admin/test");

            HttpServletResponse response = mock(HttpServletResponse.class);
            Object handler = mock(Object.class);

            // when
            boolean isPassed = interceptor.preHandle(request, response, handler);

            // then
            assertThat(isPassed).isTrue();
        }

        @DisplayName("일반 요청인 경우 통과")
        @Test
        void passWithMemberRequest() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.ROLE_MEMBER, "회원");
            String memberToken = jwtTokenProvider.createAccessToken(memberTokenContent);

            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", memberToken);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            when(request.getRequestURI()).thenReturn("/member");

            HttpServletResponse response = mock(HttpServletResponse.class);
            Object handler = mock(Object.class);

            // when
            boolean isPassed = interceptor.preHandle(request, response, handler);

            // then
            assertThat(isPassed).isTrue();
        }
    }
}
