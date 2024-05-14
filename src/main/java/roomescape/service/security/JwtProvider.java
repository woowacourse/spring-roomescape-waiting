package roomescape.service.security;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.exception.member.AuthenticationFailureException;

@Component
public class JwtProvider {
    private static final String SECRET_KEY = "hellowootecoworldhihowareyouiamfinethankyouandyou";
    private static final String ROLE_CLAIM_KEY = "role";
    private static final String NAME_CLAIM_KEY = "name";
    
    public String encode(Member user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(ROLE_CLAIM_KEY, user.getRoleAsString())
                .claim(NAME_CLAIM_KEY, user.getName())
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();
    }

    public Long extractId(String token) {
        return Long.valueOf(extractPayload(token).getSubject());
    }

    public Role extractRole(String token) {
        return Role.of(extractPayload(token).get(ROLE_CLAIM_KEY, String.class));
    }

    public String extractName(String token) {
        return extractPayload(token).get(NAME_CLAIM_KEY, String.class);
    }

    private Claims extractPayload(String token) {
        try {
            token = token.replace("token=", "");

            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new AuthenticationFailureException();
        }
    }

}
