package roomescape.auth.controller;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import roomescape.auth.service.AuthService;
import roomescape.auth.service.JwtTokenHandler;
import roomescape.member.domain.Member;

@Component
public class MemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenHandler jwtTokenHandler;
    private final AuthService authService;

    public MemberArgumentResolver(final JwtTokenHandler jwtTokenHandler, final AuthService authService) {
        this.jwtTokenHandler = jwtTokenHandler;
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Member resolveArgument(
            final MethodParameter parameter, final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = jwtTokenHandler.extractTokenValue(request);
        long memberId = Long.parseLong(jwtTokenHandler.getMemberIdFromToken(token));

        return authService.findById(memberId);
    }
}
