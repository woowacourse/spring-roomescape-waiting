package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.service.MemberService;
import roomescape.service.result.MemberResult;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final CookieProvider cookieProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    public LoginMemberArgumentResolver(final CookieProvider cookieProvider, final JwtTokenProvider jwtTokenProvider, final MemberService memberService) {
        this.cookieProvider = cookieProvider;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = cookieProvider.extractTokenFromCookies(request.getCookies());
        MemberResult memberResult = memberService.getById(jwtTokenProvider.extractIdFromToken(token));
        return LoginMemberInfo.of(memberResult.id());
    }
}
