package roomescape.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.exception.member.InvalidTokenException;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long tokenValidityTimeInMilliSeconds;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expire-length}") long tokenValidityTimeInMilliSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.tokenValidityTimeInMilliSeconds = tokenValidityTimeInMilliSeconds;
    }

    public String createToken(Member member) {
        Date expiredDateTime = new Date(new Date().getTime() + tokenValidityTimeInMilliSeconds);

        String accessToken = Jwts.builder()
                .setSubject(member.getId().toString())
                .claim("name", member.getName())
                .setExpiration(expiredDateTime)
                .signWith(secretKey)
                .compact();
        return accessToken;
    }

    public Long findMemberIdByToken(String token) {
        if (!validateExpiration(token)) {
            throw new InvalidTokenException("사용자 인증 정보가 만료되었습니다.");
        }

        Long id = Long.valueOf(Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody().getSubject());
        return id;
    }

    private boolean validateExpiration(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
