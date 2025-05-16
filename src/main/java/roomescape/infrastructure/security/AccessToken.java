package roomescape.infrastructure.security;

import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;

public record AccessToken(
        String value
) {

    public static AccessToken create(TokenIssueRequest tokenIssueRequest) {
        return new AccessToken(Jwts.builder()
                .issuedAt(tokenIssueRequest.issuedAt())
                .expiration(tokenIssueRequest.expiration())
                .subject(tokenIssueRequest.identifier().toString())
                .signWith(tokenIssueRequest.secretKey())
                .compact()
        );
    }

    public static AccessToken of(String value) {
        return new AccessToken(value);
    }

    public Long extractMemberId(SecretKey secretKey) {
        return Long.valueOf(Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(value)
                .getPayload()
                .getSubject()
        );
    }
}
