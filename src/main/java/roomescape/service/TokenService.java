package roomescape.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private static final String MEMBER_ID = "member_id";
    private static final String secretKey = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";

    public String createToken(long id, LocalDateTime createdAt, Duration tokenLifeTime) {
        LocalDateTime rawExpiredTime = createdAt.plus(tokenLifeTime);
        Date expiredTime = localDateTimeToDate(rawExpiredTime);
        return Jwts.builder()
                .expiration(expiredTime)
                .claim(MEMBER_ID, id)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    private Date localDateTimeToDate(LocalDateTime expiredTime) {
        Instant instant = expiredTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public long findMemberIdFromToken(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token);

        Claims payload = claimsJws.getPayload();
        return payload.get(MEMBER_ID, Long.class);
    }
}
