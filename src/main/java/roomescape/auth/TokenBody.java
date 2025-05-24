package roomescape.auth;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import roomescape.member.domain.MemberRole;

@AllArgsConstructor
public class TokenBody {

    private final Claims claims;

    public String email() {
        return claims.getSubject();
    }

    public MemberRole role() {
        return MemberRole.valueOf(claims.get("role", String.class));
    }

    public String name() {
        return claims.get("name", String.class);
    }
}
