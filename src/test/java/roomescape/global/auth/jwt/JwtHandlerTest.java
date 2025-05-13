package roomescape.global.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.impl.UnauthorizedException;
import roomescape.member.business.domain.Member;
import roomescape.member.business.domain.Role;

class JwtHandlerTest {

    private JwtHandler jwtHandler;

    private final String secretKey = "my-secret-key";
    private final long expireLength = 1000 * 60 * 60; // 1시간

    @BeforeEach
    void setUp() {
        jwtHandler = new JwtHandler();

        setField(jwtHandler, "secretKey", secretKey);
        setField(jwtHandler, "accessTokenExpireTime", expireLength);
    }

    @Test
    void 토큰을_생성하고_디코드할_수_있다() {
        final Member member = new Member(1L, "엠제이", "test1@test.com", "1234", Role.ADMIN);

        final Token token = jwtHandler.createToken(member);
        final Map<String, String> decoded = jwtHandler.decode(token.accessToken());

        assertThat(decoded.get(JwtHandler.CLAIM_ID_KEY)).isEqualTo("1");
        assertThat(decoded.get(JwtHandler.CLAIM_ROLE_KEY)).isEqualTo("ADMIN");
    }

    @Test
    void 지원하지_않는_형식의_토큰은_예외를_던진다() {
        // JWT 형식이 아닌 임의의 문자열
        final String unsupportedToken = "not-a-jwt-token";

        assertThatThrownBy(() -> jwtHandler.decode(unsupportedToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인 정보가 유효하지 않습니다.");
    }

    @Test
    void 서명이_틀린_토큰은_예외를_던진다() {
        final String wrongSignedToken = Jwts.builder()
                .claim(JwtHandler.CLAIM_ID_KEY, 1L)
                .claim(JwtHandler.CLAIM_ROLE_KEY, "ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(SignatureAlgorithm.HS256, "wrong-secret-key")
                .compact();

        assertThatThrownBy(() -> jwtHandler.decode(wrongSignedToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인 정보가 유효하지 않습니다.");
    }

    @Test
    void 만료된_토큰은_예외를_던진다() {
        final String expiredToken = Jwts.builder()
                .claim(JwtHandler.CLAIM_ID_KEY, 1L)
                .claim(JwtHandler.CLAIM_ROLE_KEY, "ADMIN")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        assertThatThrownBy(() -> jwtHandler.decode(expiredToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인 정보가 만료되었습니다.");
    }

    @Test
    void 지원하지_않는_JWT_형식은_예외를_던진다() {
        final String unsupportedJwt = Jwts.builder()
                .setPayload("unsupportedJwtPayload")
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        assertThatThrownBy(() -> jwtHandler.decode(unsupportedJwt))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인 정보가 유효하지 않습니다.");
    }

    @Test
    void 잘못된_형식의_토큰은_예외를_던진다() {
        final String malformedToken = "eyJhbGciOiJIUzI1NiJ9.e30"; // payload나 서명이 없음

        assertThatThrownBy(() -> jwtHandler.decode(malformedToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인 정보가 유효하지 않습니다.");
    }


    private void setField(final Object target, final String fieldName, final Object value) {
        try {
            final var field = JwtHandler.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
