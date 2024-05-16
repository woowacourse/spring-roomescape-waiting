package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.member.dto.LoginMember;
import roomescape.domain.Member;
import roomescape.service.MemberService;
import roomescape.service.exception.InvalidTokenException;
import roomescape.service.exception.MemberNotFoundException;

import java.io.IOException;
import java.util.Arrays;

public class CheckMemberInterceptor implements HandlerInterceptor {

    public static final String LOGIN_MEMBER = "loginMember";

    private final MemberService memberService;

    public CheckMemberInterceptor(final MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response, final Object handler) {
        final Cookie[] cookies = request.getCookies();
        final String token = extractTokenFromCookie(cookies);
        if (token == null) {
            return false;
        }
        try {
            final Member member = memberService.findMemberByToken(token);
            final LoginMember loginMember
                    = new LoginMember(member.getId(), member.getName(), member.getRole().name());
            request.setAttribute(LOGIN_MEMBER, loginMember);
        } catch (final InvalidTokenException | MemberNotFoundException e) {
            return false;
        }
        return true;
    }

    private String extractTokenFromCookie(final Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("token"))
                .map(Cookie::getValue)
                .findAny()
                .orElse(null);
    }
}
