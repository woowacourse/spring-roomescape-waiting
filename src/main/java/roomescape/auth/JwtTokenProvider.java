package roomescape.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.LoginMember;

import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final String ROLE_KEY = "role";
    private static final String NAME_KEY = "name";

    private final byte[] secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(@Value("${security.jwt.token.secret-key}") final String secretKey,
                            @Value("${security.jwt.token.expire-length}") final long validityInMilliseconds) {
        this.secretKey = secretKey.getBytes();
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(final Member member) {
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .claim(NAME_KEY, member.getNameString())
                .claim(ROLE_KEY, member.getRole())
                .setSubject(member.getId().toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretKey))
                .compact();
    }

    public LoginMember parse(final String accessToken) {
        final Claims payload = getPayload(accessToken);

        final long id = Long.parseLong(payload.getSubject());
        final String name = payload.get(NAME_KEY, String.class);
        final Role role = Role.valueOf(payload.get(ROLE_KEY, String.class));
        return new LoginMember(id, name, role);
    }

    private Claims getPayload(final String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey))
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }
}
