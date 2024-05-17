package roomescape.infrastructure;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import roomescape.dto.LoginMember;
import roomescape.service.MemberService;

@Component
public class AuthenticationExtractor {

    private static final String TOKEN_KEY = "token";
    private static final String UNAUTHORIZED_MESSAGE = "접근 권한이 없는 요청입니다.";

    private final MemberService memberService;

    public AuthenticationExtractor(MemberService memberService) {
        this.memberService = memberService;
    }

    public LoginMember getTokenFromCookies(HttpServletRequest request) {
        String token = getTokenFromCookie(request);
        return memberService.findLoginMemberByToken(token);
    }

    public void validateTokenRole(HttpServletRequest request) {
        String token = getTokenFromCookie(request);
        if (!memberService.hasAdminRole(token)) {
            throw new SecurityException(UNAUTHORIZED_MESSAGE);
        }
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new SecurityException(UNAUTHORIZED_MESSAGE);
        }
        return Arrays.stream(cookies)
                .filter(cookie -> TOKEN_KEY.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new SecurityException(UNAUTHORIZED_MESSAGE));
    }
}
