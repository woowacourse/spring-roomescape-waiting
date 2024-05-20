package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.Member;
import roomescape.service.MemberService;
import roomescape.util.CookieUtil;
import roomescape.util.JwtProvider;

import java.util.Objects;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    public LoginMemberArgumentResolver(MemberService memberService, JwtProvider jwtProvider) {
        this.memberService = memberService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Member resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = Objects.requireNonNull(webRequest.getNativeRequest(HttpServletRequest.class));
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new TokenValidationFailureException("토큰이 존재하지 않습니다.");
        }
        String token = CookieUtil.extractToken(cookies)
                .orElseThrow(() -> new TokenValidationFailureException("토큰이 존재하지 않습니다."));
        String subject = jwtProvider.getSubject(token);
        long memberId = Long.parseLong(subject);

        return memberService.findValidatedSiteUserById(memberId);
    }
}
