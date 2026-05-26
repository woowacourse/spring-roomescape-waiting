package roomescape.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestTimeConfig.class)
@ActiveProfiles("test")
abstract class AuthApiTestSupport {

    protected String loginToken(String name, String password) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "password", password, "storeId", 1L))
                .when().post("/api/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("data.accessToken");
    }

    protected String loginUserToken() {
        return loginToken("a", "test1");
    }

    protected String loginAdminToken() {
        return loginToken("testAdmin", "test2");
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
