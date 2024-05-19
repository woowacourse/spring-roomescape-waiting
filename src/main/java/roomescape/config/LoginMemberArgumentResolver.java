package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.JwtTokenProvider;
import roomescape.auth.dto.LoginMember;
import roomescape.auth.service.TokenCookieService;
import roomescape.exception.UnauthorizedException;

import java.util.Map;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCookieService tokenCookieService;

    public LoginMemberArgumentResolver(JwtTokenProvider jwtTokenProvider, TokenCookieService tokenCookieService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenCookieService = tokenCookieService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null || request.getCookies() == null) {
            throw new UnauthorizedException("사용자 인증 정보가 없습니다.");
        }

        String accessToken = tokenCookieService.getTokenFromCookies(request.getCookies());
        Map<String, String> decodedClaims = jwtTokenProvider.decode(accessToken);

        return LoginMember.from(decodedClaims);
    }
}
