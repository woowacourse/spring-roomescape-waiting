package roomescape.member;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.login.service.LoginService;
import roomescape.member.dto.MemberRequest;
import roomescape.util.TokenExtractor;

public class MemberRequestArgumentResolver implements HandlerMethodArgumentResolver {

    private final LoginService loginService;

    public MemberRequestArgumentResolver(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(MemberRequest.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = TokenExtractor.extractTokenFromCookie(servletRequest.getCookies());

        return loginService.getMemberRequestByToken(token);
    }
}
