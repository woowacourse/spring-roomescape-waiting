package roomescape.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.LoginMemberInToken;

@Component
public class TokenProvider {

    private final String secretKey;
    private final long expirationTime;

    public TokenProvider(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration-time}") long expirationTime
    ) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    public String createToken(Member member) {
        Date now = new Date();

        return Jwts.builder()
                .claim(JwtClaimKey.MEMBER_ID.getKey(), member.getId().toString())
                .claim(JwtClaimKey.MEMBER_NAME.getKey(), member.getName())
                .claim(JwtClaimKey.MEMBER_EMAIL.getKey(), member.getEmail())
                .claim(JwtClaimKey.ROLE.getKey(), member.getRole())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public LoginMemberInToken getLoginMember(String accessToken) {
        Claims claims = getClaims(accessToken);
        validateExpiredToken(claims);

        return new LoginMemberInToken(
                Long.valueOf(claims.get(JwtClaimKey.MEMBER_ID.getKey()).toString()),
                Role.from(claims.get(JwtClaimKey.ROLE.getKey()).toString()),
                claims.get(JwtClaimKey.MEMBER_NAME.getKey()).toString(),
                claims.get(JwtClaimKey.MEMBER_EMAIL.getKey()).toString()
        );
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }

    private void validateExpiredToken(Claims claims) {
        if (claims.getExpiration().before(new Date())) {
            throw new JwtException("만료시간이 지난 토큰 입니다.");
        }
    }
}
