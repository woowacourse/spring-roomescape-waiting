package roomescape.unit.web;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.global.MemberArgumentResolver;
import roomescape.global.SessionMember;

class MemberArgumentResolverTest {

    private final MemberArgumentResolver resolver = new MemberArgumentResolver();

    @Test
    void 세션에서_멤버ID를_추출할_수_있다() throws Exception {
        // given
        SessionMember sessionMember = new SessionMember(1L, new MemberName("한스"), MemberRole.MEMBER);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setSession(new MockHttpSession());
        servletRequest.getSession().setAttribute("LOGIN_MEMBER", sessionMember);
        NativeWebRequest webRequest = new ServletWebRequest(servletRequest);

        Method method = DummyController.class.getMethod("dummyMethod", Long.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // when
        Object resolved = resolver.resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(resolved).isEqualTo(new SessionMember(1L, new MemberName("한스"), MemberRole.MEMBER));
    }

    @Test
    void 세션_없으면_예외() {
        NativeWebRequest webRequest = new ServletWebRequest(new MockHttpServletRequest());

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("로그인이 필요합니다.");
    }

    static class DummyController {
        public void dummyMethod(Long memberId) {}
    }
}
