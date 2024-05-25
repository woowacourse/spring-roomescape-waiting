package roomescape.controller.support;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.security.Accessor;
import roomescape.security.TokenProvider;
import roomescape.security.exception.UnauthorizedException;
import roomescape.service.MemberService;
import roomescape.service.dto.response.MemberResponse;
import roomescape.util.CookieUtil;

@Component
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    public AuthArgumentResolver(MemberService memberService, TokenProvider tokenProvider) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class);
    }

    @Override
    public Accessor resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String token = CookieUtil.extractTokenFromCookie(webRequest)
                .orElseThrow(UnauthorizedException::new);

        Long id = tokenProvider.getMemberId(token);
        MemberResponse memberResponse = memberService.getById(id);

        return new Accessor(
                memberResponse.id(),
                memberResponse.name(),
                memberResponse.role()
        );
    }
}
