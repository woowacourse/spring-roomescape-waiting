package roomescape.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.Member;
import roomescape.service.MemberService;
import roomescape.util.CookieTokenExtractor;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberService memberService;
    private final CookieTokenExtractor authorizationExtractor;

    public LoginMemberArgumentResolver(MemberService memberService) {
        this.memberService = memberService;
        this.authorizationExtractor = new CookieTokenExtractor();
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = authorizationExtractor.extract(request);
        return memberService.findMemberByToken(token);
    }
}
