package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.global.exception.AuthorizationException;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.service.MemberService;

@ExtendWith(MockitoExtension.class)
class AdminHandlerInterceptorTest {

    @InjectMocks
    private AdminHandlerInterceptor adminHandlerInterceptor;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MemberService memberService;

    @DisplayName("관리자 권한이 아닌 경우 인터셉터가 예외를 발생시킨다")
    @Test
    void should_throw_exception_when_accessor_is_not_admin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Cookie[] cookies = {new Cookie("token", "mockvalue")};
        request.setCookies(cookies);
        when(memberService.findById(any(Long.class))).thenReturn(MemberFixture.NON_ADMIN_MEMBER);
        assertThatThrownBy(() -> adminHandlerInterceptor.preHandle(request, response, null))
                .isInstanceOf(AuthorizationException.class);
    }

    @DisplayName("관리자 권한인 경우 인터셉터가 true를 반환한다")
    @Test
    void should_return_true_when_accessor_is_admin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Cookie[] cookies = {new Cookie("token", "mockvalue")};
        request.setCookies(cookies);
        when(memberService.findById(any(Long.class))).thenReturn(MemberFixture.ADMIN_MEMBER);
        assertThat(adminHandlerInterceptor.preHandle(request, response, null)).isTrue();
    }
}
