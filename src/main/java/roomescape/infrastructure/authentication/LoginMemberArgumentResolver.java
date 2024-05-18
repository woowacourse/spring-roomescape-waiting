package roomescape.infrastructure.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.application.AuthenticationService;
import roomescape.application.dto.LoginMember;
import roomescape.application.dto.MemberResponse;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticationService authenticationService;

    public LoginMemberArgumentResolver(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public LoginMember resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new RoomescapeException(RoomescapeErrorCode.UNAUTHORIZED);
        }
        String token = authenticationService.extractToken(request.getCookies());
        MemberResponse response = authenticationService.findMemberByToken(token);
        return new LoginMember(response.id(), response.name(), response.email(), response.role().name());
    }

}
