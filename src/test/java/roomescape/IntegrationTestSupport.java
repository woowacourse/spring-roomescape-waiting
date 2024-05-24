package roomescape;

import io.restassured.RestAssured;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import roomescape.controller.member.dto.MemberLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    protected static final String ADMIN_EMAIL = "redddy@gmail.com";
    protected static final String ADMIN_PASSWORD = "0000";
    protected static final String ADMIN_NAME = "레디";
    protected static final String USER_EMAIL = "jinwuo0925@gmail.com";
    protected static final String USER_PASSWORD = "1111";
    protected static final String USER_NAME = "제제";

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
