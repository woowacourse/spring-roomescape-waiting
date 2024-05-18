package roomescape.global.auth.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.global.auth.annotation.MemberId;
import roomescape.global.auth.jwt.JwtHandler;
import roomescape.global.auth.jwt.constant.JwtKey;

@Component
public class MemberIdResolver implements HandlerMethodArgumentResolver {
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    private final JwtHandler jwtHandler;

    public MemberIdResolver(final JwtHandler jwtHandler) {
        this.jwtHandler = jwtHandler;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MemberId.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Long memberId = (Long) httpServletRequest.getAttribute(JwtKey.MEMBER_ID.getValue());
        System.out.println("=====");
        System.out.println(memberId);

        return memberId;
    }
}
