package roomescape.global.auth.resolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.global.auth.jwt.JwtHandler;
import roomescape.global.exception.impl.UnauthorizedException;
import roomescape.login.business.service.TokenCookieService;
import roomescape.login.presentation.request.LoginCheckRequest;

@Component
public class MemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtHandler jwtHandler;
    private final TokenCookieService tokenCookieService;

    public MemberArgumentResolver(final JwtHandler jwtHandler, final TokenCookieService tokenCookieService) {
        this.jwtHandler = jwtHandler;
        this.tokenCookieService = tokenCookieService;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginCheckRequest.class);
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) {
        final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null || request.getCookies() == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        final String accessToken = tokenCookieService.getTokenFromCookies(request.getCookies());
        final Map<String, String> decodedClaims = jwtHandler.decode(accessToken);
        final Long id = Long.valueOf(decodedClaims.get(JwtHandler.CLAIM_ID_KEY));

        return LoginCheckRequest.from(id);
    }
}
