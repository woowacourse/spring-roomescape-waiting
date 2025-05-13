package roomescape.auth;

import io.jsonwebtoken.Claims;
import roomescape.member.MemberRole;

public class TokenBody {

    private final Claims claims;

    public TokenBody(final Claims claims) {
        this.claims = claims;
    }

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
