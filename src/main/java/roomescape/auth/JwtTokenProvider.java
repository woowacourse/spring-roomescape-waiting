package roomescape.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private String secretKey;
    private long validityInMilliseconds;

    public JwtTokenProvider(@Value("${security.jwt.token.secret-key}") final String secretKey,
                            @Value("${security.jwt.token.expire-length}") final long validityInMilliseconds) {
        this.secretKey = secretKey;
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(final Member member) {
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .claim("name", member.getName())
                .setSubject(member.getId().toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Long getMemberIdByToken(final String accessToken) {
        return Long.valueOf(Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(accessToken)
                .getBody()
                .getSubject());
    }
}
