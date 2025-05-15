package roomescape.auth.controller;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import roomescape.auth.dto.LoginMember;
import roomescape.auth.service.AuthService;
import roomescape.auth.service.JwtTokenHandler;
import roomescape.common.AuthTokenCookieProvider;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenHandler jwtTokenHandler;
    private final AuthTokenCookieProvider authTokenCookieProvider;
    private final AuthService authService;

    public LoginMemberArgumentResolver(
            final JwtTokenHandler jwtTokenHandler,
            final AuthTokenCookieProvider authTokenCookieProvider,
            final AuthService authService
    ) {
        this.jwtTokenHandler = jwtTokenHandler;
        this.authTokenCookieProvider = authTokenCookieProvider;
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public LoginMember resolveArgument(
            final MethodParameter parameter, final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = authTokenCookieProvider.extractToken(request);
        final Long memberId = jwtTokenHandler.getSubject(token);
        return authService.findLoginMemberById(memberId);
    }
}
