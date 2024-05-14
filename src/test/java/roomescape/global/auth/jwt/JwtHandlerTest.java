package roomescape.global.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.UnauthorizedException;

import java.util.Date;

import static roomescape.global.exception.error.ErrorType.EXPIRED_TOKEN;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JwtHandlerTest {

    @Autowired
    private JwtHandler jwtHandler;

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("토큰이 만료되면 401 Unauthorized 를 발생시킨다.")
    void jwtExpired() {
        //given
        Date date = new Date();
        Date expiredAt = new Date(date.getTime() - 1);

        String accessToken = Jwts.builder()
                .claim("memberId", 1L)
                .setIssuedAt(date)
                .setExpiration(expiredAt)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();

        // when & then
        Assertions.assertThatThrownBy(() -> jwtHandler.getMemberIdFromTokenWithValidate(accessToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(EXPIRED_TOKEN.getDescription());
    }

    @Test
    @DisplayName("지원하지 않는 토큰이면 401 Unauthorized 를 발생시킨다.")
    void jwtMalformed() {
        // given
        Date date = new Date();
        Date expiredAt = new Date(date.getTime() + 100000);

        String accessToken = Jwts.builder()
                .claim("memberId", 1L)
                .setIssuedAt(date)
                .setExpiration(expiredAt)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();

        String[] splitAccessToken = accessToken.split("\\.");
        String unsupportedAccessToken = splitAccessToken[0] + "." + splitAccessToken[1];

        // when & then
        Assertions.assertThatThrownBy(() -> jwtHandler.getMemberIdFromTokenWithValidate(unsupportedAccessToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(ErrorType.MALFORMED_TOKEN.getDescription());
    }

    @Test
    @DisplayName("토큰의 Signature 가 잘못되었다면 401 Unauthorized 를 발생시킨다.")
    void jwtInvalidSignature() {
        // given
        Date date = new Date();
        Date expiredAt = new Date(date.getTime() + 100000);

        String invalidSecretKey = secretKey.substring(1);

        String accessToken = Jwts.builder()
                .claim("memberId", 1L)
                .setIssuedAt(date)
                .setExpiration(expiredAt)
                .signWith(SignatureAlgorithm.HS256, invalidSecretKey.getBytes()) // 기존은 HS256 알고리즘
                .compact();

        // when & then
        Assertions.assertThatThrownBy(() -> jwtHandler.getMemberIdFromTokenWithValidate(accessToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(ErrorType.INVALID_SIGNATURE_TOKEN.getDescription());
    }

    @Test
    @DisplayName("토큰이 공백값이라면 401 Unauthorized 를 발생시킨다.")
    void jwtIllegal() {
        // given
        String accessToken = "";

        // when & then
        Assertions.assertThatThrownBy(() -> jwtHandler.getMemberIdFromTokenWithValidate(accessToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(ErrorType.ILLEGAL_TOKEN.getDescription());
    }
}
