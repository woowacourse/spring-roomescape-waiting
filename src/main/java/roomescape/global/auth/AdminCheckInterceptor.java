package roomescape.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.exception.custom.ForbiddenException;
import roomescape.global.exception.custom.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;

public class AdminCheckInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminCheckInterceptor(final MemberRepository memberRepository,
                                 final JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response, final Object handler
    ) throws Exception {
        final String token = CookieUtil.parseCookie(request.getCookies());
        final long id = jwtTokenProvider.getId(token);
        final Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("확인할 수 없는 사용자입니다."));
        if (!member.hasRole(Role.ADMIN)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        return true;
    }
}
