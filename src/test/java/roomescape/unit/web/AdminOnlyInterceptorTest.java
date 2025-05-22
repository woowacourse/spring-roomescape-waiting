package roomescape.unit.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.global.AdminOnlyInterceptor;
import roomescape.global.dto.SessionMember;
import roomescape.global.exception.AccessDeniedException;
import roomescape.global.exception.AuthenticationException;

class AdminOnlyInterceptorTest {

    private final AdminOnlyInterceptor interceptor = new AdminOnlyInterceptor();

    @Test
    void 관리자가_아니면_403_반환() {
        // given
        var sessionMember = new SessionMember(1L, new MemberName("한스"), MemberRole.MEMBER);
        var request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        request.getSession().setAttribute("LOGIN_MEMBER", sessionMember);

        var response = new MockHttpServletResponse();

        // when // then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("어드민이 아닙니다.");
    }

    @Test
    void 관리자는_true_반환() {
        // given
        var sessionMember = new SessionMember(1L, new MemberName("한스"), MemberRole.ADMIN);
        var request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        request.getSession().setAttribute("LOGIN_MEMBER", sessionMember);

        var response = new MockHttpServletResponse();

        // when
        var result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 로그인_정보가_없으면_예외_발생() {
        // given
        var request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        var response = new MockHttpServletResponse();

        // when // then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("로그인이 필요합니다.");
    }
}
