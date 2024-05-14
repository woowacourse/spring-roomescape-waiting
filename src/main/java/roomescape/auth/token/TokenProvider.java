package roomescape.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import roomescape.member.model.MemberRole;

import javax.crypto.SecretKey;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

public class TokenProvider {

    private final SecretKey tokenSecretKey;
    private final long tokenExpirationPeriod;

    public TokenProvider(final SecretKey tokenSecretKey, final long tokenExpirationPeriod) {
        this.tokenSecretKey = tokenSecretKey;
        this.tokenExpirationPeriod = tokenExpirationPeriod;
    }

    public String createToken(final Long memberId, final MemberRole role) {
        final Claims claim = createClaims(memberId, role);
        return Jwts.builder()
                .claims(claim)
                .signWith(tokenSecretKey)
                .compact();
    }

    private Claims createClaims(final Long memberId, final MemberRole role) {
        return Jwts.claims()
                .subject(memberId.toString())
                .issuedAt(new Date(currentTimeMillis()))
                .expiration(createExpirationDate())
                .add("role", role.name())
                .build();
    }

    private Date createExpirationDate() {
        return new Date(new Date().getTime() + tokenExpirationPeriod);
    }

    public Claims getTokenClaims(final String token) {
        return Jwts.parser()
                .verifyWith(tokenSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
