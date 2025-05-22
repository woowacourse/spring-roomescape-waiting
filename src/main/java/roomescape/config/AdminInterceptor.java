package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Member;
import roomescape.domain.enums.Role;
import roomescape.service.member.MemberService;
import roomescape.util.CookieManager;
import roomescape.util.JwtTokenProvider;

public class AdminInterceptor implements HandlerInterceptor {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminInterceptor(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Cookie[] cookies = request.getCookies();
        String jwtToken = CookieManager.extractTokenFromCookies(cookies);
        if (jwtToken == null) {
            response.setStatus(401);
            return false;
        }
        Long id = jwtTokenProvider.findMemberIdByToken(jwtToken);
        Member member = memberService.findMemberById(id);
        if (member == null || !member.getRole().equals(Role.ADMIN)) {
            response.setStatus(401);
            return false;
        }
        return true;
    }
}
