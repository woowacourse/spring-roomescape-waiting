package roomescape.presentation.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import roomescape.application.dto.TokenRequest;
import roomescape.config.TestConfig;
import roomescape.config.TestDataClearExtension;
import roomescape.config.TestDataInitializer;

@ExtendWith(TestDataClearExtension.class)
@SpringBootTest(
        classes = TestConfig.class,
        webEnvironment = WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class AcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestDataInitializer testDataInitializer;

    protected String adminToken;
    protected String memberToken;

    @BeforeEach
    void initializePort() {
        RestAssured.port = port;
    }

    @BeforeEach
    void initData() throws Exception {
        testDataInitializer.run();
    }

    protected void adminTokenSetup() {
        TokenRequest tokenRequest = new TokenRequest("admin@wooteco.com", "wootecoCrew6!");
        adminToken = RestAssured.given()
                .contentType("application/json")
                .body(tokenRequest)
                .when().post("/login")
                .then().log().cookies()
                .extract()
                .cookie("token");
        System.out.println("adminToken = " + adminToken);
    }

    protected void memberTokenSetUp() {
        TokenRequest tokenRequest = new TokenRequest("member@wooteco.com", "wootecoCrew6!");
        memberToken = RestAssured.given()
                .contentType("application/json")
                .body(tokenRequest)
                .when().post("/login")
                .then()
                .statusCode(200)
                .extract()
                .cookie("token");
    }
}
