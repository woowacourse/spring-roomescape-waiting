package roomescape.domain.login.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.member.service.MemberService;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.exception.AuthorizationException;

@Component
public class MemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberService memberService;

    private final JwtTokenProvider jwtTokenProvider;

    public MemberArgumentResolver(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MemberResolver.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        Cookie[] cookie = request.getCookies();
        if (cookie == null) {
            throw new AuthorizationException("로그인 해야 합니다.");
        }
        String token = jwtTokenProvider.extractTokenFromCookie(request.getCookies());
        Long memberId = jwtTokenProvider.validateAndGetLongSubject(token);
        return memberService.findMemberById(memberId);
    }
}
