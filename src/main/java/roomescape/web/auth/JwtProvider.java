package roomescape.web.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import roomescape.web.exception.AuthorizationException;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtProvider {

    private final JwtProperties jwtProperties;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }


    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.expireLength());

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, jwtProperties.secretKey())
            .compact();
    }

    public String createExpiredToken(String token) {
        if (validateToken(token)) {
            Claims claims = getClaims(token);

            return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(claims.getIssuedAt())
                .setExpiration(new Date(claims.getIssuedAt().getTime() - jwtProperties.expireLength()))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secretKey())
                .compact();
        }
        throw new AuthorizationException("토큰이 만료되었습니다. 다시 로그인해주세요.");
    }

    public String getPayload(String token) {
        if (validateToken(token)) {
            return getClaims(token)
                .getSubject();
        }
        throw new AuthorizationException("토큰이 만료되었습니다. 다시 로그인해주세요.");
    }

    private boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                .setSigningKey(jwtProperties.secretKey())
                .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .setSigningKey(jwtProperties.secretKey())
            .parseClaimsJws(token)
            .getBody();
    }
}
