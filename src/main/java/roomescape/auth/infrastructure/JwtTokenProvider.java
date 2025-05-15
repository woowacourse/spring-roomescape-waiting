package roomescape.auth.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.auth.exception.InvalidTokenException;
import roomescape.auth.model.Principal;
import roomescape.member.model.Member;

import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final String IS_ADMIN_KEY = "isAdmin";
    public static final String MEMBER_NAME_KEY = "memberName";

    @Value("${auth.jwt.secret-key}")
    private String secretKey;
    @Value("${auth.jwt.validity-ms}")
    private long validityInMilliseconds;

    public String createToken(Member member) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(member.getId()));
        claims.put(MEMBER_NAME_KEY, member.getName());
        claims.put(IS_ADMIN_KEY, member.isAdmin());
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Principal resolvePrincipalFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            Long memberId = Long.valueOf(claims.getSubject());
            String name = claims.get(MEMBER_NAME_KEY).toString();
            boolean isAdmin = (Boolean) claims.get(IS_ADMIN_KEY);
            return new Principal(memberId, name, isAdmin);
        } catch (ExpiredJwtException expiredJwtException) {
            throw new InvalidTokenException("만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException jwtException) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }
}
