package roomescape.member.security.service;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import org.springframework.stereotype.Service;
import roomescape.exception.AuthorizationMismatchException;
import roomescape.exception.IllegalAuthorizationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.security.crypto.PasswordEncoder;
import roomescape.member.security.crypto.TokenProvider;

@Service
public class MemberAuthService {

    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public MemberAuthService(PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public void validateAuthentication(Member member, MemberLoginRequest memberLoginRequest)
            throws AuthorizationMismatchException {
        if (!passwordEncoder.matches(memberLoginRequest.password(), member.getPassword())) {
            throw new AuthorizationMismatchException("비밀번호가 일치하지 않습니다.");
        }
    }

    public String publishToken(Member member) {
        Date now = new Date();
        return tokenProvider.createToken(member, now);
    }

    public MemberProfileInfo extractPayload(Cookie[] cookies) {
        String token = extractTokenFromCookie(cookies);
        Map<String, String> payload = tokenProvider.getPayload(token);
        return new MemberProfileInfo(
                Long.valueOf(payload.get("id")),
                payload.get("name"),
                payload.get("email"));
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> "token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalAuthorizationException("잘못된 쿠키 값입니다."));
    }

    public String extractNameFromPayload(Cookie[] cookies) {
        String token = extractTokenFromCookie(cookies);
        Map<String, String> tokenPayload = tokenProvider.getPayload(token);
        return tokenPayload.get("name");
    }

    public boolean isLoginMember(Cookie[] cookies) {
        String token = extractTokenFromCookie(cookies);
        return tokenProvider.validateToken(token);
    }

    public boolean isAdmin(Member member) {
        return member.getRole() == MemberRole.ADMIN;
    }

}
