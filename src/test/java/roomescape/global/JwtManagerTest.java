package roomescape.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.restassured.RestAssured;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mock.web.MockHttpServletRequest;
import roomescape.domain.member.Member;
import roomescape.service.exception.UnauthorizedException;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class JwtManagerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtManager jwtManager;

    private final Member member1 = new Member(1L, "t1@t1.com", "123", "러너덕", "MEMBER");

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("회원 정보로 JWT 토큰을 생성한다")
    @Test
    void generate_token() {
        String token = jwtManager.generateToken(member1);

        assertNotNull(token);
    }

    @DisplayName("HTTP 요청에 쿠키가 존재하지 않으면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_request_cookies_null() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> jwtManager.extractToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("권한이 없습니다. 로그인을 다시 시도해주세요.");
    }

    @DisplayName("HTTP 요청에 토큰이 존재하지 않으면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_request_not_contains_token() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("notToken", "notToken"));

        assertThatThrownBy(() -> jwtManager.extractToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("권한이 없습니다. 로그인을 다시 시도해주세요.");
    }

    @DisplayName("HTTP 요청에 토큰이 존재하면 정상적으로 토큰 value를 반환한다.")
    @Test
    void return_token_value() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie("token", "tokenValue");
        request.setCookies(cookie);

        String token = jwtManager.extractToken(request);

        assertThat(token).isEqualTo(cookie.getValue());
    }

    @DisplayName("토큰 검증에 실패하면 예외를 발생시킨다.")
    @Test
    void throw_signature_exception_when_invalid_token() {
        String token = jwtManager.generateToken(member1);
        token += "invalid";

        String testToken = token;
        assertThatThrownBy(() -> jwtManager.verifyToken(testToken))
                .isInstanceOf(JwtException.class)
                .hasMessage("JWT 토큰 검증에 실패하였습니다.");
    }

    @DisplayName("토큰이 유효하지 않으면 예외를 발생시킨다.")
    @Test
    void throw_malformed_jwt_exception_when_invalid_token() {
        String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IkpvYnMgRG9lIiwicm9sZSI6IlVTRVIifQ";

        assertThatThrownBy(() -> jwtManager.verifyToken(testToken))
                .isInstanceOf(JwtException.class)
                .hasMessage("JWT 토큰 구성이 올바르지 않습니다.");
    }

    @DisplayName("토큰 검증에 성공하면 payload 가 일치한다.")
    @Test
    void success_verify_token() {
        String token = jwtManager.generateToken(member1);

        Claims claims = jwtManager.verifyToken(token);
        String subject = claims.getSubject();
        String name = claims.get("name", String.class);
        String role = claims.get("role", String.class);

        assertAll(
                () -> assertThat(subject).isEqualTo(member1.getId().toString()),
                () -> assertThat(name).isEqualTo(member1.getName()),
                () -> assertThat(role).isEqualTo(member1.getRole().name())
        );
    }
}
