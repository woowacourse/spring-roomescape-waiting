package roomescape.member.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.service.dto.TokenInfo;

@Component
public class JwtProvider implements TokenProvider {
    private static final SignatureAlgorithm SIGN_ALGORITHM = SignatureAlgorithm.HS256;

    @Value("${security.jwt.token.secret_key}")
    private String SECRET_KEY;
    @Value("${security.jwt.token.expiration_term}")
    private long EXPIRATION_TERM;

    @Override
    public String createToken(final Member member) {
        final Date now = new Date();
        final Date expirationDate = new Date(now.getTime() + EXPIRATION_TERM);
        return Jwts.builder()
                .setSubject(String.valueOf(member.getId()))
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .claim("role", member.getRole().name())
                .signWith(SIGN_ALGORITHM, SECRET_KEY)
                .compact();
    }

    @Override
    public TokenInfo parsePayload(final String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        return new TokenInfo(Long.valueOf(claims.getSubject()), MemberRole.valueOf(claims.get("role", String.class)));
    }
}
