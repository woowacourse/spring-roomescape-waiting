package roomescape.auth.jwt.parser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.auth.jwt.domain.Jwt;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JwtParserImpl implements JwtParser {

    @Override
    public Claims execute(final Jwt token, final SecretKey secretKey) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token.getValue())
                    .getPayload();
        } catch (final SignatureException e) {
            throw new ParseTokenException("Invalid signature", e);
        } catch (final ExpiredJwtException e) {
            throw new ParseTokenException("Token expired", e);
        } catch (final Exception e) {
            throw new ParseTokenException("Unknown token error", e);
        }
    }
}
