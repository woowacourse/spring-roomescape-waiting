package roomescape.service.login;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Clock;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberRole;
import roomescape.exception.login.ExpiredTokenException;
import roomescape.exception.login.InvalidTokenException;
import roomescape.exception.member.InvalidMemberRoleException;

@Component
public class JwtTokenProvider {
    private static final String ROLE_CLAIM_NAME = "role";
    private final String secretKey;
    private final long validityInMilliseconds;
    private final Clock clock;

    public JwtTokenProvider(@Value("${security.jwt.token.secret-key}") String secretKey,
                            @Value("${security.jwt.token.expire-length}") long validityInMilliseconds,
                            Clock clock) {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
        this.clock = clock;
    }

    public String createToken(MemberEmail memberEmail, MemberRole memberRole) {
        return Jwts.builder()
                .setSubject(memberEmail.getAddress())
                .claim(ROLE_CLAIM_NAME, memberRole.name())
                .setExpiration(calculateExpiredAt())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    private Date calculateExpiredAt() {
        Date now = Date.from(clock.instant());
        return new Date(now.getTime() + validityInMilliseconds);
    }

    public MemberEmail getMemberEmail(String token) {
        String address = getClaims(token).getSubject();
        return new MemberEmail(address);
    }

    public MemberRole getMemberRole(String token) {
        String role = getClaims(token).get(ROLE_CLAIM_NAME, String.class);
        try {
            return MemberRole.findByName(role);
        } catch (InvalidMemberRoleException e) {
            throw new InvalidTokenException();
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .setClock(() -> Date.from(clock.instant()))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException();
        }
    }
}
