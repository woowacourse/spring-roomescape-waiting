package roomescape.service.helper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.MemberRole;
import roomescape.exception.login.ExpiredTokenException;
import roomescape.exception.login.InvalidTokenException;

@Component
public class JwtTokenProvider {
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(String memberEmail, MemberRole memberRole) {
        return Jwts.builder()
                .setSubject(memberEmail)
                .claim("role", memberRole.name())
                .setExpiration(calculateExpiredAt())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    private Date calculateExpiredAt() {
        Date now = new Date();
        return new Date(now.getTime() + validityInMilliseconds);
    }

    public String getMemberEmail(String token) {
        return getClaims(token).getSubject();
    }

    public MemberRole getMemberRole(String token) {
        String role = getClaims(token).get("role", String.class);
        return MemberRole.findByName(role);
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }
}
