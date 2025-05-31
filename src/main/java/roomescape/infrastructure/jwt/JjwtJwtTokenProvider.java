package roomescape.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.application.provider.JwtTokenProvider;
import roomescape.common.exception.UnauthorizedException;

@Component
public class JjwtJwtTokenProvider implements JwtTokenProvider {

    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    @Override
    public String createToken(String payload) {
        Claims claims = Jwts.claims().setSubject(payload);
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SIGNATURE_ALGORITHM, secretKey)
                .compact();
    }

    @Override
    public String createToken(String payload, Date now) {
        Claims claims = Jwts.claims().setSubject(payload);
        Date expiredDate = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SIGNATURE_ALGORITHM, secretKey)
                .compact();
    }

    @Override
    public String getPayload(String token) {

        validateToken(token);

        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("토큰이 만료되었습니다.");
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("토큰이 비어 있습니다.");
        }
    }
}
