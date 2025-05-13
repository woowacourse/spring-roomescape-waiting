package roomescape.auth.jwt.parser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.auth.jwt.domain.Jwt;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.jwt.generator.JwtGenerator;
import roomescape.user.domain.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JwtParserImplTest {

    private static final SecretKey secretKey =
            Keys.hmacShaKeyFor("ZMQ5kFGf1MTmnkhik/7rOcmT6OPvlz5Z+4aP4pAfNtk=".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private JwtParserImpl jwtParser;
    @Autowired
    private JwtGenerator jwtGenerator;

    @Test
    @DisplayName("유요한 토큰 파싱이 성공한다")
    void shouldParseValidTokenSuccessfully() {
        // given
        final TokenType type = TokenType.ACCESS;

        final Instant now = Instant.now();
        final Instant expiration = now.plusMillis(type.getPeriodInMillis());

        final Claims claims = Jwts.claims()
                .add(User.Fields.id, "1")
                .add(User.Fields.name, "강산")
                .add(User.Fields.role, "ADMIN")
                .build();
        final Jwt jwt = jwtGenerator.execute(
                claims,
                now,
                expiration,
                secretKey);

        // when
        final Claims parsedClaims = jwtParser.execute(jwt, secretKey);

        // then
        assertThat(parsedClaims.get(User.Fields.id)).isEqualTo(claims.get(User.Fields.id));
        assertThat(parsedClaims.get(User.Fields.name)).isEqualTo(claims.get(User.Fields.name));
        assertThat(parsedClaims.get(User.Fields.role)).isEqualTo(claims.get(User.Fields.role));
    }

    @Test
    @DisplayName("토큰의 서명이 유효하지 않으면 예외가 발생한다")
    void shouldThrowExceptionForInvalidSignature() {
        // given
        final SecretKey invalidKey =
                Keys.hmacShaKeyFor("ZMQ5kFGf1MTmnkhik/7rOcmT6OPvlz5Z+4aP4pAfNta=".getBytes(StandardCharsets.UTF_8));

        final TokenType type = TokenType.ACCESS;
        final Instant now = Instant.now();
        final Instant expiration = now.plusMillis(type.getPeriodInMillis());

        final Jwt jwt = jwtGenerator.execute(
                Jwts.claims()
                        .add(User.Fields.id, "1")
                        .add(User.Fields.name, "강산")
                        .add(User.Fields.role, "ADMIN")
                        .build(),
                now,
                expiration,
                secretKey);

        // when
        // then
        assertThatThrownBy(() -> jwtParser.execute(jwt, invalidKey))
                .isInstanceOf(ParseTokenException.class)
                .hasMessageContaining("Invalid signature");
    }

    @Test
    @DisplayName("토큰이 만료되면 예외가 발생한다")
    void shouldThrowExceptionForExpiredToken() {
        // given
        final Instant now = Instant.now();
        final Instant expiration = Instant.now().minusMillis(1);

        final Jwt jwt = jwtGenerator.execute(
                Jwts.claims()
                        .add(User.Fields.id, "1")
                        .add(User.Fields.name, "강산")
                        .add(User.Fields.role, "ADMIN")
                        .build(),
                now,
                expiration,
                secretKey);

        // when
        // then
        assertThatThrownBy(() -> jwtParser.execute(jwt, secretKey))
                .isInstanceOf(ParseTokenException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 예외가 발생한다")
    void shouldThrowExceptionForInvalidToken() {
        // given
        final Jwt invalidJwt = Jwt.from("invalid-token-value");

        // when
        // then
        assertThatThrownBy(() -> jwtParser.execute(invalidJwt, secretKey))
                .isInstanceOf(ParseTokenException.class)
                .hasMessageContaining("Unknown token error");
    }
}
