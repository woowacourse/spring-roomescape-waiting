package roomescape.auth.infrastructure;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.global.exception.auth.AuthenticationExpiredException;
import roomescape.global.exception.auth.InvalidAuthenticationException;
import roomescape.member.domain.Member;

@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey,
                            @Value("${jwt.expire-length}") long validityInMilliseconds) {
        this.secretKey = secretKey;
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public Token createToken(Member member) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return new Token(Jwts.builder()
                .setSubject(member.getId().toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact());
    }

    public Long getAccessorId(String token) {
        try {
            return Long.valueOf(Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject());
        } catch (ExpiredJwtException e) {
            throw new AuthenticationExpiredException("로그인 기한이 만료되었습니다", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAuthenticationException("유효하지 않은 로그인 정보입니다", e);
        }
    }
}
