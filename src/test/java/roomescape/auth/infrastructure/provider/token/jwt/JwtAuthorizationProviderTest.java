package roomescape.auth.infrastructure.provider.token.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;

@SpringBootTest
class JwtAuthorizationProviderTest {

    private static final String NAME_KEY = "name";
    private static final String ROLE_KEY = "role";
    private static final Member USER = MemberFixture.createMember(MemberRole.USER);

    @Autowired
    private JwtAuthorizationProvider jwtAuthorizationProvider;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.validity-in-milliseconds}")
    private int validityInMilliseconds;

    @Test
    void jwt_를__생성한다() {
        // given
        AuthorizationPayload payload = AuthorizationPayload.fromMember(USER);

        // when
        String token = jwtAuthorizationProvider.createToken(payload);
        AuthorizationPayload expected = getPayloadFromJwt(token);

        // then
        assertThat(jwtAuthorizationProvider.getPayload(token)).isEqualTo(expected);
    }

    @Test
    void jwt_를__파싱한다() {
        // given
        String token = createJwt(AuthorizationPayload.fromMember(USER));

        // when
        AuthorizationPayload payload = jwtAuthorizationProvider.getPayload(token);

        // then
        assertThat(payload.name()).isEqualTo(USER.getName());
        assertThat(payload.role()).isEqualTo(USER.getRole());
    }

    private String createJwt(AuthorizationPayload payload) {
        Claims claims = Jwts.claims();
        claims.put(NAME_KEY, payload.name());
        claims.put(ROLE_KEY, payload.role());

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    private AuthorizationPayload getPayloadFromJwt(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody();

        String name = claims.get(NAME_KEY, String.class);
        MemberRole role = MemberRole.valueOf(claims.get(ROLE_KEY, String.class));

        return new AuthorizationPayload(name, role);
    }
}
