package roomescape.auth.login.infrastructure.argumentresolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.exception.ForbiddenException;
import roomescape.auth.token.JwtTokenManager;
import roomescape.auth.token.TokenExtractor;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.auth.login.presentation.dto.annotation.LoginMember;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String MEMBER_STRING = "MEMBER";

    private final JwtTokenManager jwtTokenManager;

    public LoginMemberArgumentResolver(JwtTokenManager jwtTokenManager) {
        this.jwtTokenManager = jwtTokenManager;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = TokenExtractor.extract(request);

        String role = jwtTokenManager.getRole(token);
        validateRoleIsMember(role);

        return new LoginMemberInfo(jwtTokenManager.getId(token));
    }

    private static void validateRoleIsMember(String role) {
        if (!role.equals(MEMBER_STRING)) {
            throw new ForbiddenException("멤버가 아닙니다.");
        }
    }
}
