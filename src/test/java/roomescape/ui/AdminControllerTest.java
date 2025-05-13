package roomescape.ui;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.auth.jwt.domain.Jwt;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.jwt.manager.JwtManager;
import roomescape.user.domain.User;
import roomescape.user.domain.UserRole;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtManager jwtManager;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("관리자 권한이 있는 사용자가 접속하면 정상적으로 200 응답을 반환한다")
    void shouldReturn200WhenAdminAccessWithValidToken() {
        // given
        final Claims adminUserClaims = Jwts.claims()
                .add(User.Fields.id, 1)
                .add(User.Fields.name, "임시")
                .add(User.Fields.role, UserRole.ADMIN.name())
                .build();

        final Jwt adminCookie = jwtManager.generate(
                adminUserClaims,
                TokenType.ACCESS
        );

        // when
        final Response response = given()
                .cookie(TokenType.ACCESS.getDescription(), adminCookie.getValue())
                .when()
                .get("/admin");

        // then
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("관리자 권한이 없는 사용자가 접속하면 403 응답을 반환한다")
    void shouldReturn403WhenNonAdminUserAccess() {
        // given
        final Claims normalUserClaims = Jwts.claims()
                .add(User.Fields.id, 1)
                .add(User.Fields.name, "임시")
                .add(User.Fields.role, UserRole.NORMAL.name())
                .build();

        final Jwt userCookie = jwtManager.generate(
                normalUserClaims,
                TokenType.ACCESS
        );

        // when
        final Response response = given()
                .cookie(TokenType.ACCESS.getDescription(), userCookie.getValue())
                .when()
                .get("/admin");

        // then
        assertThat(response.getStatusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 접속하면 401 응답을 반환한다")
    void shouldReturn401WhenUnauthorizedUserAccess() {
        // when
        final Response response = given()
                .when()
                .get("/admin");

        // then
        assertThat(response.getStatusCode()).isEqualTo(401);
    }
}
