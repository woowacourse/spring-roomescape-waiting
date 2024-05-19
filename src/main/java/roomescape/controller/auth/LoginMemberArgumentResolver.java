package roomescape.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.CookieProvider;
import roomescape.dto.MemberResponse;
import roomescape.dto.auth.LoginMember;
import roomescape.service.MemberService;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberService memberService;
    private final CookieProvider cookieProvider;

    @Autowired
    public LoginMemberArgumentResolver(final MemberService memberService,
                                       final CookieProvider cookieProvider) {
        this.memberService = memberService;
        this.cookieProvider = cookieProvider;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class)
                && parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public LoginMember resolveArgument(final MethodParameter parameter,
                                  final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest webRequest,
                                  final WebDataBinderFactory binderFactory) throws Exception {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final String accessToken = cookieProvider.extractToken(request.getCookies());
        final MemberResponse memberResponse = memberService.findMemberByToken(accessToken);
        return new LoginMember(memberResponse.id(), memberResponse.name(), memberResponse.email(), memberResponse.role());
    }
}
