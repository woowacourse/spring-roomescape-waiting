package roomescape.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.auth.exception.JwtAuthenticationException;
import roomescape.auth.exception.JwtExpiredException;

@Component
public class TokenProvider {

    private final String secretKey;

    public TokenProvider(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String createToken(Long memberId) {
        return Jwts.builder()
                .claims()
                .add("id", memberId)
                .and()
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public Long findMemberId(String token) {
        try {
            return parseId(token);
        } catch (ExpiredJwtException exception) {
            throw new JwtExpiredException();
        } catch (MalformedJwtException exception) {
            throw new JwtAuthenticationException("토큰의 형식이 잘못되었습니다.");
        } catch (InvalidClaimException exception) {
            throw new JwtAuthenticationException("필요한 정보를 포함하고 있지 않습니다.");
        } catch (JwtException exception) {
            throw new JwtAuthenticationException("해당 토큰은 잘못된 토큰입니다.");
        }
    }

    private Long parseId(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("id", Long.class);
    }
}
