package roomescape.service;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import roomescape.domain.LoginMember;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.jwt.JwtTokenProvider;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String createTokenByMember(Member member) {
        return jwtTokenProvider.createTokenByMember(member);
    }

    public LoginMember getLoginMemberByToken(String token) {
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
        Long memberId = Long.valueOf(claims.getSubject());
        String name = claims.get("name", String.class);
        String role = claims.get("role", String.class);

        return new LoginMember(memberId, name, Role.valueOf(role));
    }
}
