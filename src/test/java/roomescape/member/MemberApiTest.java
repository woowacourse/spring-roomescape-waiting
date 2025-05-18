package roomescape.member;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import roomescape.common.CleanUp;
import roomescape.config.AuthServiceTestConfig;

@Import(AuthServiceTestConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MemberApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CleanUp cleanUp;

    private Map<String, String> member;

    @BeforeEach
    void setUp() {
        member = Map.of("email", "user1@email.com", "password", "1234", "name", "브라운");

        RestAssured.port = port;
        cleanUp.all();
    }

    @Test
    void 유저를_생성한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(member)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);
    }
}
