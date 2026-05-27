package roomescape.member.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.common.AcceptanceTest;

class MemberControllerTest extends AcceptanceTest {


    @Nested
    @DisplayName("register 메서드는")
    class RegisterTest {


        @Test
        @DisplayName("회원가입을 수행한다")
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
}
