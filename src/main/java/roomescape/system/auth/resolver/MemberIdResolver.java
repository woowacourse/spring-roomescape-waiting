package roomescape.system.auth.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.system.auth.annotation.MemberId;
import roomescape.system.auth.jwt.JwtHandler;
import roomescape.system.exception.error.ErrorType;
import roomescape.system.exception.model.UnauthorizedException;

@Component
public class MemberIdResolver implements HandlerMethodArgumentResolver {
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    private final JwtHandler jwtHandler;

    public MemberIdResolver(final JwtHandler jwtHandler) {
        this.jwtHandler = jwtHandler;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MemberId.class);
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) throws Exception {
        final Cookie[] cookies = webRequest.getNativeRequest(HttpServletRequest.class).getCookies();
        if (cookies == null) {
            throw new UnauthorizedException(ErrorType.INVALID_TOKEN, "쿠키가 존재하지 않습니다");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(ACCESS_TOKEN_COOKIE_NAME))
                .findAny()
                .map(cookie -> jwtHandler.getMemberIdFromToken(cookie.getValue()))
                .orElseThrow(() -> new UnauthorizedException(ErrorType.INVALID_TOKEN, "JWT 토큰이 존재하지 않거나 유효하지 않습니다."));
    }
}
