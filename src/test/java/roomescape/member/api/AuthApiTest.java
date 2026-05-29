package roomescape.member.api;

import static roomescape.member.fixture.MemberApiFixture.registerMember;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.AcceptanceTest;

class AuthApiTest extends AcceptanceTest {

    @Test
    @DisplayName("로그인을 수행한다")
    void login() {
        String name = "송송송";
        String password = "1234";
        registerMember(name, password);

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("password", password);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/login")
            .then().log().all()
            .statusCode(200)
            .header("Authorization", Matchers.notNullValue())
            .body(Matchers.emptyOrNullString());
    }

    @Test
    @DisplayName("회원가입을 하지 않은 이용자는 로그인을 할 수 없다")
    void loginFailed() {
        String name = "wrongName";
        String password = "1234";

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("password", password);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/login")
            .then().log().all()
            .statusCode(400);
    }
}
