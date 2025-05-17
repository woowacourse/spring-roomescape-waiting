package roomescape.admin;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminIntegrationTest {

    @DisplayName("Admin이 아닌 상태로 /admin/** 요청 시 401 응답을 준다.")
    @Test
    void when_admin_request() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }
}
