package roomescape.service;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.entity.Member;
import roomescape.global.Role;
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

    public LoginMemberRequest getLoginMemberByToken(String token) {
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
        Long memberId = Long.valueOf(claims.getSubject());
        String name = claims.get("name", String.class);
        String role = claims.get("role", String.class);

        return new LoginMemberRequest(memberId, name, Role.valueOf(role));
    }
}
