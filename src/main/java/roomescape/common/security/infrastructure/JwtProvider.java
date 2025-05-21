package roomescape.common.security.infrastructure;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import roomescape.common.config.JwtProperties;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.common.security.exception.UnAuthorizedException;
import roomescape.member.domain.MemberRole;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtProvider {

    private static final String ROLE = "role";
    private static final int VALIDITY_IN_MILLISECONDS = 1000;

    private final JwtProperties jwtProperties;

    public JwtProvider(final JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createToken(final MemberInfo memberInfo) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validity = now.plusSeconds(jwtProperties.getExpireLength() / VALIDITY_IN_MILLISECONDS);

        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecretKey());
        return JWT.create()
                .withSubject(memberInfo.id().toString())
                .withClaim(ROLE, memberInfo.memberRole().name())
                .withIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .withExpiresAt(Date.from(validity.atZone(ZoneId.systemDefault()).toInstant()))
                .sign(algorithm);
    }

    public boolean isInvalidToken(final String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (TokenExpiredException e) {
            throw new UnAuthorizedException("만료된 토큰입니다.");
        } catch (JWTVerificationException e) {
            throw new UnAuthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    public Long getMemberId(final String token) {
        try {
            return Long.parseLong(verifyToken(token).getSubject());
        } catch (JWTVerificationException e) {
            throw new UnAuthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    public MemberRole getRole(final String token) {
        try {
            return MemberRole.valueOf(verifyToken(token).getClaim(ROLE).asString());
        } catch (JWTVerificationException e) {
            throw new UnAuthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    private DecodedJWT verifyToken(final String token) {
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecretKey());
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }
}
