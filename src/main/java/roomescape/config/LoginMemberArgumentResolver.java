package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.business.service.AuthService;
import roomescape.exception.UnauthorizedException;
import roomescape.presentation.dto.LoginMember;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    public LoginMemberArgumentResolver(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType()
                .equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory
    ) throws UnauthorizedException {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final String accessToken = getAccessToken(request);
        return authService.getLoginMemberByAccessToken(accessToken);
    }

    private String getAccessToken(final HttpServletRequest request) throws UnauthorizedException {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthorizedException("로그인 정보가 없습니다.");
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName()
                        .equals("token"))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("로그인 정보가 없습니다."))
                .getValue();
    }
}

