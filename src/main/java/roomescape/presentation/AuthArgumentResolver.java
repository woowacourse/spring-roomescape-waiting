package roomescape.presentation;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.application.AuthService;
import roomescape.application.AuthorizationExtractor;
import roomescape.application.MemberService;
import roomescape.dto.Accessor;
import roomescape.dto.response.MemberResponse;
import roomescape.exception.UnauthorizedException;

@Component
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;
    private final MemberService memberService;
    private final AuthorizationExtractor<String> authorizationExtractor;

    public AuthArgumentResolver(
            AuthService authService,
            MemberService memberService,
            AuthorizationExtractor<String> authorizationExtractor
    ) {
        this.authService = authService;
        this.memberService = memberService;
        this.authorizationExtractor = authorizationExtractor;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class);
    }

    @Override
    public Accessor resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        String token = authorizationExtractor.extract(webRequest)
                .orElseThrow(UnauthorizedException::new);

        Long id = authService.getMemberIdByToken(token);
        MemberResponse memberResponse = memberService.getById(id);

        return new Accessor(
                memberResponse.id(),
                memberResponse.name(),
                memberResponse.email(),
                memberResponse.role()
        );
    }
}
