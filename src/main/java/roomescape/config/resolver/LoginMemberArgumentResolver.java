package roomescape.config.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.User;
import roomescape.dto.business.AccessTokenContent;
import roomescape.exception.local.NotFoundCookieException;
import roomescape.service.UserService;
import roomescape.utility.JwtTokenProvider;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String TOKEN_NAME_FIELD = "token";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public LoginMemberArgumentResolver(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().equals(User.class);
    }

    @Override
    public User resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_NAME_FIELD.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    AccessTokenContent accessTokenContent = jwtTokenProvider.parseAccessToken(token);
                    return userService.getUserById(accessTokenContent.id());
                }
            }
        }

        throw new NotFoundCookieException();
    }
}
