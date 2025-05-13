package roomescape.auth.jwt.generator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.auth.jwt.domain.Jwt;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.jwt.parser.JwtParser;
import roomescape.user.domain.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HmacJwtGeneratorTest {

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private JwtParser jwtParser;

    private static final SecretKey secretKey =
            Keys.hmacShaKeyFor("ZMQ5kFGf1MTmnkhik/7rOcmT6OPvlz5Z+4aP4pAfNtk=".getBytes(StandardCharsets.UTF_8));

    @Test
    @DisplayName("JWT가 정상적으로 생성된다")
    void jwtIsGeneratedSuccessfully() {
        // given
        final Claims claims = Jwts.claims()
                .add(User.Fields.id, "1")
                .add(User.Fields.name, "강산")
                .add(User.Fields.role, "ADMIN")
                .build();

        final TokenType type = TokenType.ACCESS;
        final Instant now = Instant.now();
        final Instant expiration = now.plusMillis(type.getPeriodInMillis());

        // when
        final Jwt jwt = jwtGenerator.execute(claims, now, expiration, secretKey);

        // then
        assertThat(jwt).isNotNull();
        assertThat(Jwt.from(jwt.toString())).isNotNull();
    }

    @Test
    @DisplayName("Claims에 추가된 값이 JWT에 포함된다")
    void claimsShouldBeIncludedInJwt() {
        // given
        final Claims claims = Jwts.claims()
                .add(User.Fields.id, "1")
                .add(User.Fields.name, "강산")
                .add(User.Fields.role, "ADMIN")
                .build();

        final TokenType type = TokenType.ACCESS;
        final Instant now = Instant.now();
        final Instant expiration = now.plusMillis(type.getPeriodInMillis());

        // when
        final Jwt jwt = jwtGenerator.execute(claims, now, expiration, secretKey);

        // then
        final Claims parsedClaims = jwtParser.execute(jwt, secretKey);
        assertThat(parsedClaims.get(User.Fields.id)).isEqualTo("1");
        assertThat(parsedClaims.get(User.Fields.name)).isEqualTo("강산");
        assertThat(parsedClaims.get(User.Fields.role)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("토큰 타입에 따라 유효 기간이 올바르게 설정된다")
    void tokenTypeSetsCorrectExpiration() {
        // given
        final Claims claims = Jwts.claims()
                .add(User.Fields.id, "1")
                .add(User.Fields.name, "강산")
                .add(User.Fields.role, "ADMIN")
                .build();

        final TokenType type = TokenType.ACCESS;
        final Instant now = Instant.now();
        final Instant expiration = now.plusMillis(type.getPeriodInMillis());

        // when
        final Jwt jwt = jwtGenerator.execute(claims, now, expiration, secretKey);

        // then
        assertThat(jwt).isNotNull();
        final Claims parsedClaims = jwtParser.execute(jwt, secretKey);
        final Date parsedExpiration = parsedClaims.getExpiration();

        assertThat(Duration.between(parsedExpiration.toInstant(), expiration).abs().toSeconds()).isLessThan(1);
    }
}
