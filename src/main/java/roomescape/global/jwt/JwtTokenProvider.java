package roomescape.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@Component
public class JwtTokenProvider implements TokenProvider {
    private static final String JWT_EXCEPTION_MESSAGE = "잘못된 로그인 시도입니다. 다시 시도해 주세요.";
    private static final SecretKey SECRET_KEY = SIG.HS256.key().build();
    private static final long validityInMilliseconds = 3600000;
    private static final String PREFIX = "token=";

    @Override
    public String createToken(Member member) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return PREFIX + Jwts.builder()
                .claim("id", member.getId())
                .claim("role", member.getRole())
                .expiration(validity)
                .signWith(SECRET_KEY)
                .compact();
    }

    @Override
    public TokenInfo getInfo(String token) {
        Claims claims = validateToken(token);

        return new TokenInfo(
                claims.get("id", Long.class),
                Role.valueOf(claims.get("role", String.class))
        );
    }

    private Claims validateToken(String token) {
        Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

        if (token == null || token.trim().isEmpty()) {
            logger.warn("JWT 토큰이 존재하지 않습니다.");
            throw new JwtException(JWT_EXCEPTION_MESSAGE);
        }

        try {
            return Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
        } catch (SecurityException | MalformedJwtException e) {
            logger.warn("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
            throw new JwtException(JWT_EXCEPTION_MESSAGE);
        } catch (ExpiredJwtException e) {
            logger.warn("Expired JWT token, 만료된 JWT token 입니다.");
            throw new JwtException(JWT_EXCEPTION_MESSAGE);
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
            throw new JwtException(JWT_EXCEPTION_MESSAGE);
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
            throw new JwtException(JWT_EXCEPTION_MESSAGE);
        }
    }
}
