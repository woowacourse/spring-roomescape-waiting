package roomescape.common.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.common.exception.LoginException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@Component
public class JwtTokenContainer {

    private static final int TOKEN_EXPIRATION_MINUTES = 30;

    private final SecretKey secretKey;

    public JwtTokenContainer(@Value("${security.jwt.token.secret-key}") String key) {
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes());
    }

    public String createJwtToken(Member member, LocalDateTime now) {
        Date expirationDate = createExpirationDate(now);

        return Jwts.builder()
                .subject(member.getId().toString())
                .claim("role", member.getRole())
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new LoginException("만료된 토큰입니다.");
        } catch (JwtException e) {
            throw new LoginException("올바르지 않은 토큰 형태입니다.");
        }
    }

    public Long getMemberId(String token) {
        try {
            String id = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Long.parseLong(id);
        } catch (JwtException e) {
            throw new LoginException("올바르지 않은 토큰 형태입니다.");
        }
    }

    public Role getMemberRole(String token) {
        try {
            String role = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role").toString();
            return Role.findRole(role);
        } catch (JwtException e) {
            throw new LoginException("올바르지 않은 토큰 형태입니다.");
        }
    }

    private Date createExpirationDate(LocalDateTime now) {
        LocalDateTime expirationTime = now.plusMinutes(TOKEN_EXPIRATION_MINUTES);
        return Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
