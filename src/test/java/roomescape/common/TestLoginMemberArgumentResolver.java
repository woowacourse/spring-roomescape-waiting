package roomescape.common;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.common.auth.annotation.LoginMember;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class TestLoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final String TEST_ROLE_HEADER = "X-Test-Role";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        String role = webRequest.getHeader(TEST_ROLE_HEADER);
        if (role.equals("MANAGER")) {
            return Member.load(1L, "테스트사용자", "1234", Role.MANAGER);
        }
        return Member.load(1L, "테스트어드민", "1234", Role.MEMBER);
    }

}
