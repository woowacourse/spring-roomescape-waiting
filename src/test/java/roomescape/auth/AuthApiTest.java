package roomescape.auth;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import roomescape.auth.stub.StubTokenProvider;
import roomescape.common.CleanUp;
import roomescape.config.AuthServiceTestConfig;

@Import(AuthServiceTestConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void init() {
        RestAssured.port = port;
        cleanUp.all();
    }

    @Test
    void 관리자_페이지를_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", StubTokenProvider.ADMIN_STUB_TOKEN)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 어드민_권한이_없다면_관리자_페이지에_접근할_할_수_없다() {
        RestAssured.given().log().all()
                .cookie("token", StubTokenProvider.USER_STUB_TOKEN)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void 인증이_되지_않았다면_관리자_페이지에_접근할_할_수_없다() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }

}
