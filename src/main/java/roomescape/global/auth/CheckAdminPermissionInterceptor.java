package roomescape.global.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.service.MemberService;
import roomescape.global.exception.AuthorizationException;
import roomescape.global.exception.ForBiddenException;

@Component
public class CheckAdminPermissionInterceptor implements HandlerInterceptor {

    protected static final String SHOULD_LOGIN_ERROR_MESSAGE = "로그인 해야 합니다.";
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    public CheckAdminPermissionInterceptor(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthorizationException(SHOULD_LOGIN_ERROR_MESSAGE);
        }
        String token = jwtTokenProvider.extractTokenFromCookie(cookies);
        Long memberId = jwtTokenProvider.validateAndGetLongSubject(token);
        Member member = memberService.getMemberById(memberId);
        if (member == null || !member.isAdmin()) {
            throw new ForBiddenException();
        }
        return true;
    }
}
