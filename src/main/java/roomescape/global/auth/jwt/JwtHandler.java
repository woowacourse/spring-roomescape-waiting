package roomescape.global.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.global.auth.jwt.dto.TokenDto;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.UnauthorizedException;

import java.util.Date;

//TODO: RefreshToken 로직 작성
@Component
public class JwtHandler {
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.access.expire-length}")
    private long accessTokenExpireTime;
    @Value("${security.jwt.token.refresh.expire-length}")
    private long refreshTokenExpireTime;

    public TokenDto createToken(Long memberId) {
        Date date = new Date();
        Date accessTokenExpiredAt = new Date(date.getTime() + accessTokenExpireTime);
        Date refreshTokenExpiredAt = new Date(date.getTime() + accessTokenExpireTime);

        String accessToken = Jwts.builder()
                .claim("memberId", memberId)
                .setIssuedAt(date)
                .setExpiration(accessTokenExpiredAt)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();

        String refreshToken = Jwts.builder()
                .setIssuedAt(date)
                .setExpiration(refreshTokenExpiredAt)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();

        return new TokenDto(accessToken, refreshToken);
    }

    public Long getMemberIdFromTokenWithValidate(String token) {
        validateToken(token);

        return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token)
                .getBody()
                .get("memberId", Long.class);
    }

    public Long getMemberIdFromTokenWithNotValidate(String token) {
        return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token)
                .getBody()
                .get("memberId", Long.class);
    }

    public void validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorType.EXPIRED_TOKEN, ErrorType.EXPIRED_TOKEN.getDescription(), e);
        } catch (UnsupportedJwtException e) {
            throw new UnauthorizedException(ErrorType.UNSUPPORTED_TOKEN, ErrorType.UNSUPPORTED_TOKEN.getDescription(), e);
        } catch (MalformedJwtException e) {
            throw new UnauthorizedException(ErrorType.MALFORMED_TOKEN, ErrorType.MALFORMED_TOKEN.getDescription(), e);
        } catch (SignatureException e) {
            throw new UnauthorizedException(ErrorType.INVALID_SIGNATURE_TOKEN, ErrorType.INVALID_SIGNATURE_TOKEN.getDescription(), e);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(ErrorType.ILLEGAL_TOKEN, ErrorType.ILLEGAL_TOKEN.getDescription(), e);
        }
    }
}
