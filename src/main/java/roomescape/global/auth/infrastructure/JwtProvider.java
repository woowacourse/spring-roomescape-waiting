package roomescape.global.auth.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

@Component
public class JwtProvider {

    private static final String NAME = "name";
    private static final String ROLE = "role";

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    public String createToken(final UserInfo userInfo) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(userInfo.id().toString())
                .claim(ROLE, userInfo.memberRole().name())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims getAllClaimsFromToken(final String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getMemberId(final String token) {
        return Long.parseLong(Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }

    public String getName(final String token) {
        return (String) Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .get(NAME);
    }

    public MemberRole getRole(final String token) {
        return MemberRole.from((String) Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .get(ROLE));
    }

    public boolean isInvalidToken(final String token) {
        try {
            return getAllClaimsFromToken(token).getExpiration()
                    .before(new Date());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
