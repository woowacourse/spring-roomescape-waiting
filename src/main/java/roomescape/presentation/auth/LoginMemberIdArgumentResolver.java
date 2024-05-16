package roomescape.presentation.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.application.auth.TokenManager;
import roomescape.application.auth.dto.TokenPayload;

public class LoginMemberIdArgumentResolver implements HandlerMethodArgumentResolver {
    private final TokenManager tokenManager;
    private final CredentialContext context;

    public LoginMemberIdArgumentResolver(TokenManager tokenManager, CredentialContext context) {
        this.tokenManager = tokenManager;
        this.context = context;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMemberId.class);
    }

    @Override
    public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        if (context.hasCredential()) {
            return context.getMemberId();
        }
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = AuthInformationExtractor.extractToken(request);
        TokenPayload payload = tokenManager.extract(token);
        return payload.memberId();
    }
}
