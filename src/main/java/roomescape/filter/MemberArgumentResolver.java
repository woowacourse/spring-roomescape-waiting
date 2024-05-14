package roomescape.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.AuthorizationExtractor;
import roomescape.auth.CookieAuthorizationExtractor;
import roomescape.domain.Member;
import roomescape.service.MemberService;

@Component
public class MemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final MemberService memberService;
    private final AuthorizationExtractor<String> authorizationExtractor;

    public MemberArgumentResolver(final MemberService memberService) {
        this.memberService = memberService;
        this.authorizationExtractor = new CookieAuthorizationExtractor();
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String accessToken = authorizationExtractor.extract(request);
        return memberService.getMemberInfo(accessToken);
    }
}
