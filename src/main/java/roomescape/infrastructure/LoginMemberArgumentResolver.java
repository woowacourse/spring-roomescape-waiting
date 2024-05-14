package roomescape.infrastructure;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.dto.LoginMember;
import roomescape.service.MemberService;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final MemberService memberService;
    private final TokenGenerator tokenGenerator;

    public LoginMemberArgumentResolver(final MemberService memberService, final TokenGenerator tokenGenerator) {
        this.memberService = memberService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final String token = tokenGenerator.getTokenFromCookies(request);

        try {
            return memberService.findLoginMemberByToken(token);
        } catch (JwtException e) {
            throw new JwtException("로그인이 필요합니다.");
        }
    }
}
