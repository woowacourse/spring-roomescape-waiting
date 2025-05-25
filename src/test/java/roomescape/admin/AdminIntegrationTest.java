package roomescape.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminIntegrationTest {

    @DisplayName("어드민이 아닌 상태로 어드민 관련 페이지 요청 시 401 응답을 준다.")
    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/reservation-page", "/admin/reservations", "/admin/times"})
    void not_access_if_not_admin(final String url) {
        RestAssured.given().log().all()
                .when().get(url)
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("어드민 로그인 후 어드민 페이지 접근이 가능하다.")
    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/reservation-page", "/admin/reservations", "/admin/times"})
    void can_access_after_admin_login(final String url) {
        Map<String, Object> loginParam = new HashMap<>();
        loginParam.put("email", "admin@email.com");
        loginParam.put("password", "password");

        String token = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(loginParam)
            .when().post("/admin/login")
            .then().log().all()
            .extract().cookie("token");

        RestAssured.given().log().all()
            .header("Cookie", "token=" + token)
            .when().get(url)
            .then().log().all()
            .statusCode(200);
    }
}
