package roomescape.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.exception.ExpiredTokenException;
import roomescape.exception.UnauthenticatedUserException;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginMember;

@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final long expirationTime;

    public JwtTokenProvider(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration-time}") long expirationTime
    ) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    public String generateToken(Member member) {
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

    public LoginMember getMember(String token) {
        Claims claims = getClaims(token);

        return new LoginMember(
                Long.valueOf(claims.get(JwtClaimKey.MEMBER_ID.getKey()).toString()),
                Role.from(claims.get(JwtClaimKey.ROLE.getKey()).toString()),
                claims.get(JwtClaimKey.MEMBER_NAME.getKey()).toString(),
                claims.get(JwtClaimKey.MEMBER_EMAIL.getKey()).toString()
        );
    }

    private Claims getClaims(String token) {
        validateTokenIsNull(token);
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException jwtException) {
            throw new ExpiredTokenException("만료시간이 지난 토큰 입니다.");
        } catch (SignatureException signatureException) {
            throw new UnauthenticatedUserException("토큰의 서명이 유효하지 않습니다.");
        }
    }

    private void validateTokenIsNull(String token) {
        if (token == null) {
            throw new UnauthenticatedUserException("미인증 사용자 입니다.");
        }
    }
}
