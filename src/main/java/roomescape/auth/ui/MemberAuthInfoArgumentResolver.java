package roomescape.auth.ui;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.domain.AuthRole;
import roomescape.auth.domain.AuthTokenExtractor;
import roomescape.auth.domain.AuthTokenProvider;
import roomescape.auth.domain.MemberAuthInfo;
import roomescape.exception.auth.AuthenticationException;

@RequiredArgsConstructor
public class MemberAuthInfoArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthTokenExtractor<String> authTokenExtractor;
    private final AuthTokenProvider authTokenProvider;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(MemberAuthInfo.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {

        final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        final String token = authTokenExtractor.extract(request);
        if (!authTokenProvider.validateToken(token)) {
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }
        final Long id = Long.parseLong(authTokenProvider.getPrincipal(token));
        final AuthRole role = authTokenProvider.getRole(token);

        return new MemberAuthInfo(id, role);
    }
}
