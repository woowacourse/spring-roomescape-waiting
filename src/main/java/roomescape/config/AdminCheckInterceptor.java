package roomescape.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import roomescape.domain.Member;
import roomescape.exception.RoomescapeException;
import roomescape.service.MemberService;
import roomescape.util.CookieUtil;
import roomescape.util.JwtProvider;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;
    private final MemberService memberService;

    public AdminCheckInterceptor(JwtProvider jwtProvider, MemberService memberService) {
        this.jwtProvider = jwtProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws IOException {
        try {
            String token = CookieUtil.extractToken(request.getCookies());
            long memberId = jwtProvider.getMemberIdFrom(token);
            Member member = memberService.getMemberById(memberId);
            if (member.isNotAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            return true;
        } catch (RoomescapeException e) {
            response.sendRedirect("/");
            return false;
        }
    }
}
