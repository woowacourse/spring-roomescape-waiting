package roomescape.controller.admin;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.AuthorizationExtractor;
import roomescape.auth.JwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdminMemberTest {
    private static final String ADMIN_USER = "wedge@test.com";

    @LocalServerPort
    int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String generateToken(String email) {
        return jwtTokenProvider.createToken(email);
    }

    @DisplayName("회원 정보들을 반환한다.")
    @Test
    void given_when_getMembers_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(200);
    }
}
