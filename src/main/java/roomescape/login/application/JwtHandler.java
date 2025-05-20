package roomescape.login.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.common.exception.impl.UnauthorizedException;
import roomescape.login.application.dto.Token;
import roomescape.member.domain.Member;

@Component
public class JwtHandler {

    public static final String CLAIM_ID_KEY = "id";
    public static final String CLAIM_ROLE_KEY = "role";

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.access.expire-length}")
    private long accessTokenExpireTime;

    public Token createToken(final Member member) {
        final Date date = new Date();
        final Date accessTokenExpiredAt = new Date(date.getTime() + accessTokenExpireTime);

        final String accessToken = Jwts.builder()
                .claim(CLAIM_ID_KEY, member.getId())
                .claim(CLAIM_ROLE_KEY, member.getRole().toString())
                .setIssuedAt(date)
                .setExpiration(accessTokenExpiredAt)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return new Token(accessToken);
    }

    public Map<String, String> decode(final String token) {
        final Claims claims = parseJwt(token);

        return Map.of(
                CLAIM_ID_KEY, claims.get(CLAIM_ID_KEY).toString(),
                CLAIM_ROLE_KEY, claims.get(CLAIM_ROLE_KEY).toString()
        );
    }

    public String decode(final String token, final String key) {
        return parseJwt(token)
                .get(key)
                .toString();
    }

    private Claims parseJwt(final String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (final ExpiredJwtException e) {
            throw new UnauthorizedException("로그인 정보가 만료되었습니다.");
        } catch (final UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            throw new UnauthorizedException("로그인 정보가 유효하지 않습니다.");
        }
    }
}
