package roomescape.argumentresolver;

import static roomescape.exception.ExceptionType.INVALID_TOKEN;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.annotation.Auth;
import roomescape.exception.RoomescapeException;
import roomescape.service.TokenService;

@Component
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {
    private final TokenService tokenService;

    public AuthArgumentResolver(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest nativeRequest = (HttpServletRequest) webRequest.getNativeRequest();
        String token = Arrays.stream(nativeRequest.getCookies())
                .filter(cookie -> cookie.getName().equals("token"))
                .limit(1)
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RoomescapeException(INVALID_TOKEN));
        return tokenService.findUserIdFromToken(token);
    }
}
