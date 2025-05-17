package roomescape.presentation.controller.auth;

import static org.hamcrest.Matchers.containsString;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.infrastructure.jwt.JwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogoutControllerIntTest {

    @LocalServerPort
    int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void request_logout() {
        String tokenForAdmin = jwtTokenProvider.createToken(new MemberIdDto(MEMBER1_ADMIN.getId()));

        RestAssured.given()
                .redirects()
                .follow(false)
                .log().all()
                .cookie("token", tokenForAdmin)
                .contentType(ContentType.JSON)
                .when().post("/logout")
                .then().log().all()
                .statusCode(303)
                .header("Location", "/")
                .header("Set-Cookie", containsString("token=null"));
    }
}
