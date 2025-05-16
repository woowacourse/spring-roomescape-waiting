package roomescape.presentation.support.methodresolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.application.auth.dto.JwtPayload;
import roomescape.infrastructure.error.exception.JwtExtractException;
import roomescape.infrastructure.security.JwtProvider;
import roomescape.infrastructure.error.exception.AuthInfoResolveException;
import roomescape.presentation.support.JwtTokenExtractor;

@Component
public class AuthInfoArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtProvider jwtProvider;

    public AuthInfoArgumentResolver(JwtTokenExtractor jwtTokenExtractor, JwtProvider jwtProvider) {
        this.jwtTokenExtractor = jwtTokenExtractor;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthInfo.class) &&
                parameter.hasParameterAnnotation(AuthPrincipal.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        JwtPayload jwtPayload = getJwtPayload(request);
        return new AuthInfo(jwtPayload.memberId(), jwtPayload.name(), jwtPayload.role());
    }

    private JwtPayload getJwtPayload(HttpServletRequest request) {
        try {
            String jwtToken = jwtTokenExtractor.extract(request);
            return jwtProvider.extractPayload(jwtToken);
        } catch (JwtExtractException e) {
            throw new AuthInfoResolveException("인증 정보를 확인할 수 없어 AuthInfo를 만들 수 없습니다.", e);
        }
    }
}
