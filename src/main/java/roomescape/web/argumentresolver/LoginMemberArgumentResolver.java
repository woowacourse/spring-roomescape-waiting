package roomescape.web.argumentresolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.dto.login.LoginMember;
import roomescape.dto.token.TokenDto;
import roomescape.infrastructure.auth.AuthorizationExtractor;
import roomescape.service.AuthService;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthorizationExtractor authorizationExtractor;
    private final AuthService authService;

    public LoginMemberArgumentResolver(AuthorizationExtractor authorizationExtractor, AuthService authService) {
        this.authorizationExtractor = authorizationExtractor;
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        LoginMember loginMember = (LoginMember) request.getAttribute("loginMember");
        if (loginMember != null) {
            return loginMember;
        }

        return extractLoginMemberByRequest(request);
    }

    private LoginMember extractLoginMemberByRequest(HttpServletRequest request) throws Exception {
        String token = authorizationExtractor.extractToken(request);
        TokenDto tokenDto = new TokenDto(token);
        if (!authService.isValidateToken(tokenDto)) {
            throw new IllegalArgumentException("[ERROR] 유효한 토큰이 아닙니다.");
        }

        return authService.extractLoginMemberByToken(tokenDto);
    }
}
