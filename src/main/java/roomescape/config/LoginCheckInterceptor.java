package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import roomescape.exception.BaseException;
import roomescape.service.MemberService;
import roomescape.util.CookieUtil;
import roomescape.util.JwtProvider;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;
    private final MemberService memberService;

    public LoginCheckInterceptor(JwtProvider jwtProvider, MemberService memberService) {
        this.jwtProvider = jwtProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {
        try {
            String token = CookieUtil.extractToken(request.getCookies());
            long memberId = jwtProvider.getMemberIdFrom(token);
            memberService.getMemberById(memberId);
            return true;
        } catch (BaseException e) {
            response.sendRedirect("/login");
            return false;
        }
    }
}
