package roomescape.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;

import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    public static final String CLAIM_EMAIL_KEY = "email";
    public static final String CLAIM_ID_KEY = "id";
    public static final String CLAIM_ROLE_KEY = "role";

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expired-period}")
    private long expiredPeriod;

    public String generate(Member member) {
        long now = new Date().getTime();

        return Jwts.builder()
                .claim(CLAIM_ID_KEY, member.getId())
                .claim(CLAIM_EMAIL_KEY, member.getEmail())
                .claim(CLAIM_ROLE_KEY, member.getRole().name())
                .setExpiration(new Date(now + expiredPeriod))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public Map<String, String> decode(String token) {
        Claims claims = parseJwt(token);

        return Map.of(
                CLAIM_EMAIL_KEY, claims.get(CLAIM_EMAIL_KEY).toString(),
                CLAIM_ID_KEY, claims.get(CLAIM_ID_KEY).toString(),
                CLAIM_ROLE_KEY, claims.get(CLAIM_ROLE_KEY).toString()
        );
    }

    public String decode(String token, String key) {
        return parseJwt(token)
                .get(key)
                .toString();
    }

    private Claims parseJwt(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException exception) {
            throw new UnauthorizedException("인증 정보가 올바르지 않습니다.");
        }
    }
}
