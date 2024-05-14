package roomescape.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.dto.member.MemberPayload;
import roomescape.util.DateUtil;

@Component
public class JwtProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    public String createToken(MemberPayload memberPayload) {
        return Jwts.builder()
                .setSubject(memberPayload.id())
                .setIssuedAt(DateUtil.getCurrentTime())
                .setExpiration(DateUtil.getAfterTenMinutes())
                .claim("name", memberPayload.name())
                .claim("email", memberPayload.email())
                .claim("role", memberPayload.role().name())
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValidateToken(String token) {
        try {
            return !Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(DateUtil.getCurrentTime());
        } catch (Exception e) {
            return false;
        }
    }
}
