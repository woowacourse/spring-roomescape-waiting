package roomescape.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.principal.AuthenticatedMember;
import roomescape.auth.service.AuthService;
import roomescape.member.model.Member;
import roomescape.util.CookieParser;

import java.util.Objects;

public class AuthenticatedMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    public AuthenticatedMemberArgumentResolver(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {

        final boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Authenticated.class);
        final boolean hasAuthenticatedMemberPrincipal = AuthenticatedMember.class
                .isAssignableFrom(parameter.getParameterType());

        return hasLoginAnnotation && hasAuthenticatedMemberPrincipal;
    }

    @Override
    public AuthenticatedMember resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final String accessToken = Objects.requireNonNull(CookieParser.findCookie(request, "token")
                .orElseThrow(() -> new IllegalArgumentException("요청에 인증 쿠키가 존재하지 않습니다."))
                .getValue());
        final Member member = authService.findAuthenticatedMember(accessToken);

        return AuthenticatedMember.from(member);
    }
}
