package roomescape;

import io.restassured.RestAssured;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import roomescape.controller.member.dto.MemberLoginRequest;

import static roomescape.TestFixture.ADMIN_EMAIL;
import static roomescape.TestFixture.ADMIN_PASSWORD;
import static roomescape.TestFixture.USER_EMAIL;
import static roomescape.TestFixture.USER_PASSWORD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    protected static String ADMIN_TOKEN;
    protected static String USER_TOKEN;

    @LocalServerPort
    int serverPost;

    @PostConstruct
    private void initialize() {
        RestAssured.port = serverPost;

        ADMIN_TOKEN = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().cookie("token");

        USER_TOKEN = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest(USER_EMAIL, USER_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().cookie("token");
    }
}
