package roomescape.member.security.service;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import jakarta.servlet.http.Cookie;

import org.springframework.stereotype.Service;

import roomescape.exception.AuthorizationMismatchExpiredException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.security.crypto.PasswordEncoder;
import roomescape.member.security.crypto.TokenProvider;

@Service
public class MemberAuthService {
    public static final String TOKEN_NAME = "token";
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public MemberAuthService(PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public void validateAuthentication(Member member, MemberLoginRequest memberLoginRequest)
            throws AuthorizationMismatchExpiredException {
        if (!passwordEncoder.matches(memberLoginRequest.password(), member.getPassword())) {
            throw new AuthorizationMismatchExpiredException();
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

    public String extractNameFromPayload(Cookie[] cookies) {
        String token = extractTokenFromCookie(cookies);
        Map<String, String> tokenPayload = tokenProvider.getPayload(token);
        return tokenPayload.get("name");
    }

    public boolean isLoginMember(Cookie[] cookies) {
        String token = extractTokenFromCookie(cookies);
        return tokenProvider.validateToken(token);
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> TOKEN_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(AuthorizationMismatchExpiredException::new);
    }

    public boolean isAdmin(Member member) {
        return member.isAdmin();
    }
}
