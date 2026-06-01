package roomescape.member.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.AcceptanceTest;

class MemberApiTest extends AcceptanceTest {


    @Test
    @DisplayName("모든 이용자는 회원가입을 할 수 있다")
    void 성공() {
        String name = "송송";
        String password = "1234";

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("password", password);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/member/members")
            .then().log().all()
            .statusCode(200)
            .body("name", is(name))
            .body("role", is("MEMBER"));
    }

}
