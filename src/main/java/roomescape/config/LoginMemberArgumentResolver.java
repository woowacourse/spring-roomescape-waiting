package roomescape.config;

import java.util.Arrays;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import lombok.RequiredArgsConstructor;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.service.dto.request.member.Credential;
import roomescape.service.security.JwtProvider;

@RequiredArgsConstructor
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtProvider jwtProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Credential.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = extractCookie(request.getCookies(), "token");
        return new Credential(jwtProvider.extractId(token));
    }

    private String extractCookie(Cookie[] cookies, String targetCookie) {
        if (cookies == null || cookies.length == 0) {
            return "";
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(targetCookie))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(AuthenticationFailureException::new);
    }
}
