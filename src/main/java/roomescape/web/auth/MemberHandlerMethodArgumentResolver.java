package roomescape.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.service.MemberAuthService;
import roomescape.service.response.MemberAppResponse;
import roomescape.web.controller.request.LoginMemberInformation;

@Component
public class MemberHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberAuthService memberAuthService;
    private final JwtProvider jwtProvider;

    public MemberHandlerMethodArgumentResolver(MemberAuthService memberAuthService, JwtProvider jwtProvider) {
        this.memberAuthService = memberAuthService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new IllegalArgumentException("요청이 없습니다. 다시 로그인 해주세요.");
        }
        if (request.getCookies() == null) {
            throw new IllegalArgumentException("쿠키가 없습니다. 다시 로그인 해주세요.");
        }
        String token = CookieHandler.extractTokenFromCookies(request.getCookies());
        String email = jwtProvider.getPayload(token);
        MemberAppResponse appResponse = memberAuthService.findMemberByEmail(email);

        return new LoginMemberInformation(appResponse.id(), appResponse.name(), appResponse.role());
    }
}