package roomescape.auth.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.handler.RequestHandler;
import roomescape.auth.service.AuthService;
import roomescape.global.annotation.LoginUser;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final RequestHandler requestHandler;
    private final MemberService memberService;
    private final AuthService authService;

    public LoginUserArgumentResolver(RequestHandler requestHandler, MemberService memberService,
                                     AuthService authService) {
        this.requestHandler = requestHandler;
        this.memberService = memberService;
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public Member resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        return memberService.findById(authService.fetchByToken(requestHandler.extract(request)).getId());
    }
}
