package roomescape.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.domain.AuthenticatedMember;
import roomescape.member.domain.Role;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";

    private final String secret;
    private final Duration accessTokenExpiration;
    private SecretKey secretKey;

    public JwtTokenProvider(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds
    ) {
        this.secret = secret;
        this.accessTokenExpiration = Duration.ofSeconds(accessTokenExpirationSeconds);
    }

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 로그인 요청이 성공했을 때 실행
    public String generateAccessToken(AuthenticatedMember member) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(member.id()))
                .claim(CLAIM_ROLE, member.role().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 검증이 끝나고 토큰에서 멤버 가져오기
    public AuthenticatedMember extractMember(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            long memberId = Long.parseLong(claims.getSubject());
            Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));

            return AuthenticatedMember.of(memberId, role);
        } catch (RuntimeException e) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
