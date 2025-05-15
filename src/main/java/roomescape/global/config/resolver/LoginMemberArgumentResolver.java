package roomescape.global.config.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.auth.service.AuthService;
import roomescape.user.domain.User;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String TOKEN_NAME_FIELD = "token";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    public LoginMemberArgumentResolver(JwtTokenProvider jwtTokenProvider, AuthService authService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().equals(User.class);
    }

    @Override
    public User resolveArgument(MethodParameter methodParameter,
                                ModelAndViewContainer mavContainer,
                                NativeWebRequest nativeWebRequest,
                                WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();

        String token = extractTokenFromCookie(request);

        String payload = jwtTokenProvider.getPayload(token);
        return authService.findMember(payload);
    }


    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (TOKEN_NAME_FIELD.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
