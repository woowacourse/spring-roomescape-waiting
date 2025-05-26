package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.exception.ExceptionCause;
import roomescape.exception.UnauthorizedException;
import roomescape.jwt.CookieManager;
import roomescape.jwt.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;

    public LoginMemberArgumentResolver(TokenProvider tokenProvider, MemberService memberService) {
        this.tokenProvider = tokenProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = extractToken(request);
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        return memberService.findById(memberId);
    }

    private String extractToken(HttpServletRequest request) {
        return CookieManager.extractByName("token", request)
                .orElseThrow(() -> new UnauthorizedException(ExceptionCause.UNAUTHORIZED_LOGIN_ACCESS));
    }
}
