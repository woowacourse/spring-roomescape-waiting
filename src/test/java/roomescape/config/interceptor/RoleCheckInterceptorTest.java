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
import org.springframework.web.method.HandlerMethod;
import roomescape.config.annotation.Authority;
import roomescape.domain.Role;
import roomescape.dto.business.AccessTokenContent;
import roomescape.exception.ForbiddenException;
import roomescape.exception.NotFoundException;
import roomescape.utility.CookieUtility;
import roomescape.utility.JwtTokenProvider;

class RoleCheckInterceptorTest {

    private CookieUtility cookieUtility = new CookieUtility();
    private JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test_secret_key_test_secret_key_test_secret_key_", 60000);
    private RoleCheckInterceptor interceptor = new RoleCheckInterceptor(cookieUtility, jwtTokenProvider);

    @Nested
    @DisplayName("어드민 요청에 대해 어드민이 아닌 경우 블록할 수 있다.")
    public class perHandle {

        @DisplayName("권한이 요구되지 않는 요청에 일반 사용자가 접속한 경우 통과")
        @Test
        void blockWithNotAuthorityRequestAndNoneToken() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/example");

            HttpServletResponse response = mock(HttpServletResponse.class);

            HandlerMethod handler = mock(HandlerMethod.class);
            when(handler.getMethodAnnotation(Authority.class)).thenReturn(null);

            // when
            boolean isPassed = interceptor.preHandle(request, response, handler);

            // then
            assertThat(isPassed).isTrue();
        }


        @DisplayName("권한이 요구되지 않는 요청에 일반 회원이 접속한 경우 통과")
        @Test
        void blockWithNotAuthorityRequestAndGeneralToken() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.GENERAL, "회원");
            String memberToken = jwtTokenProvider.createAccessToken(memberTokenContent);

            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", memberToken);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            when(request.getRequestURI()).thenReturn("/example");

            HttpServletResponse response = mock(HttpServletResponse.class);

            HandlerMethod handler = mock(HandlerMethod.class);
            when(handler.getMethodAnnotation(Authority.class)).thenReturn(null);

            // when
            boolean isPassed = interceptor.preHandle(request, response, handler);

            // then
            assertThat(isPassed).isTrue();
        }

        @DisplayName("권한이 요구되지 않는 요청에 어드민이 접속한 경우 통과")
        @Test
        void blockWithNotAuthorityRequestAndAdminToken() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.ADMIN, "회원");
            String memberToken = jwtTokenProvider.createAccessToken(memberTokenContent);

            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", memberToken);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            when(request.getRequestURI()).thenReturn("/example");

            HttpServletResponse response = mock(HttpServletResponse.class);

            HandlerMethod handler = mock(HandlerMethod.class);
            when(handler.getMethodAnnotation(Authority.class)).thenReturn(null);

            // when
            boolean isPassed = interceptor.preHandle(request, response, handler);

            // then
            assertThat(isPassed).isTrue();
        }


        @DisplayName("어드민 요청인데 일반 사용자가 접속한 경우 블록")
        @Test
        void blockWithAdminRequestAndNoneToken() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/example");

            HttpServletResponse response = mock(HttpServletResponse.class);

            HandlerMethod handler = mock(HandlerMethod.class);
            Authority authority = mock(Authority.class);
            when(authority.value()).thenReturn(Role.ADMIN);
            when(handler.getMethodAnnotation(Authority.class)).thenReturn(authority);

            // when & then
            assertThatThrownBy(() -> interceptor.preHandle(request, response, handler))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("쿠키가 존재하지 않습니다.");
        }

        @DisplayName("어드민 요청인데 일반 회원이 접속한 경우 블록")
        @Test
        void blockWithAdminRequestAndMemberToken() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.GENERAL, "회원");
            String memberToken = jwtTokenProvider.createAccessToken(memberTokenContent);

            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", memberToken);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            when(request.getRequestURI()).thenReturn("/example");

            HttpServletResponse response = mock(HttpServletResponse.class);

            HandlerMethod handler = mock(HandlerMethod.class);
            Authority authority = mock(Authority.class);
            when(authority.value()).thenReturn(Role.ADMIN);
            when(handler.getMethodAnnotation(Authority.class)).thenReturn(authority);

            // when & then
            assertThatThrownBy(() -> interceptor.preHandle(request, response, handler))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("권한이 존재하지 않습니다.");
        }

        @DisplayName("어드민 요청인데 어드민이 접속한 경우 통과")
        @Test
        void passWithAdminRequestAndAdminToken() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.ADMIN, "회원");
            String memberToken = jwtTokenProvider.createAccessToken(memberTokenContent);

            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", memberToken);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            when(request.getRequestURI()).thenReturn("/admin/test");

            HttpServletResponse response = mock(HttpServletResponse.class);

            HandlerMethod handler = mock(HandlerMethod.class);
            Authority authority = mock(Authority.class);
            when(authority.value()).thenReturn(Role.ADMIN);
            when(handler.getMethodAnnotation(Authority.class)).thenReturn(authority);

            // when
            boolean isPassed = interceptor.preHandle(request, response, handler);

            // then
            assertThat(isPassed).isTrue();
        }

        @DisplayName("일반 회원 요청인데 일반 사용자가 접속한 경우 블록")
        @Test
        void passWithMemberRequestAndNoneToken() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/example");

            HttpServletResponse response = mock(HttpServletResponse.class);

            HandlerMethod handler = mock(HandlerMethod.class);
            Authority authority = mock(Authority.class);
            when(authority.value()).thenReturn(Role.GENERAL);
            when(handler.getMethodAnnotation(Authority.class)).thenReturn(authority);

            // when & then
            assertThatThrownBy(() -> interceptor.preHandle(request, response, handler))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("쿠키가 존재하지 않습니다.");
        }

        @DisplayName("일반 회원 요청인데 일반 회원이 접속한 경우 통과")
        @Test
        void passWithMemberRequestAndGeneralToken() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.GENERAL, "회원");
            String memberToken = jwtTokenProvider.createAccessToken(memberTokenContent);

            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", memberToken);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            when(request.getRequestURI()).thenReturn("/example");

            HttpServletResponse response = mock(HttpServletResponse.class);

            HandlerMethod handler = mock(HandlerMethod.class);
            Authority authority = mock(Authority.class);
            when(authority.value()).thenReturn(Role.GENERAL);
            when(handler.getMethodAnnotation(Authority.class)).thenReturn(authority);

            // when
            boolean isPassed = interceptor.preHandle(request, response, handler);

            // then
            assertThat(isPassed).isTrue();
        }

        @DisplayName("일반 회원 요청인데 어드민이 접속한 경우 통과")
        @Test
        void passWithMemberRequestAndAdminToken() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.ADMIN, "회원");
            String memberToken = jwtTokenProvider.createAccessToken(memberTokenContent);

            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("access", memberToken);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            when(request.getRequestURI()).thenReturn("/example");

            HttpServletResponse response = mock(HttpServletResponse.class);

            HandlerMethod handler = mock(HandlerMethod.class);
            Authority authority = mock(Authority.class);
            when(authority.value()).thenReturn(Role.GENERAL);
            when(handler.getMethodAnnotation(Authority.class)).thenReturn(authority);

            // when
            boolean isPassed = interceptor.preHandle(request, response, handler);

            // then
            assertThat(isPassed).isTrue();
        }
    }
}
