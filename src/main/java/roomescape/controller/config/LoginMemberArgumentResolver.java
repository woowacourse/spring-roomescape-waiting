package roomescape.controller.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.exception.AuthorizationException;
import roomescape.model.member.LoginMember;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.util.CookieManager;
import roomescape.util.TokenManager;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = CookieManager.extractAuthCookie(request)
                .orElseThrow(AuthorizationException::new)
                .getValue();
        MemberWithoutPassword loginMember = TokenManager.parse(token);
        return new LoginMember(loginMember.getId());
    }
}
