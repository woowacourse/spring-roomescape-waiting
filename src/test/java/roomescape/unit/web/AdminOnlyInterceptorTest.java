package roomescape.unit.web;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.global.AdminOnlyInterceptor;
import roomescape.global.dto.SessionMember;
import roomescape.global.exception.AccessDeniedException;
import roomescape.global.exception.AuthenticationException;

class AdminOnlyInterceptorTest {

    private final AdminOnlyInterceptor interceptor = new AdminOnlyInterceptor();

    @Test
    void 관리자가_아니면_403_반환() throws Exception {
        // given
        SessionMember sessionMember = new SessionMember(1L, new MemberName("한스"), MemberRole.MEMBER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        request.getSession().setAttribute("LOGIN_MEMBER", sessionMember);

        MockHttpServletResponse response = new MockHttpServletResponse();
        Member member = new Member(
                1L,
                new MemberName("한스"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("das"),
                MemberRole.MEMBER
        );

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("어드민이 아닙니다.");
    }

    @Test
    void 관리자는_true_반환() throws Exception {
        // given
        SessionMember sessionMember = new SessionMember(1L, new MemberName("한스"), MemberRole.ADMIN);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        request.getSession().setAttribute("LOGIN_MEMBER", sessionMember);

        MockHttpServletResponse response = new MockHttpServletResponse();
        Member member = new Member(
                1L,
                new MemberName("한스"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("das"),
                MemberRole.ADMIN
        );

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 로그인_정보가_없으면_예외_발생() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("로그인이 필요합니다.");
    }
}
