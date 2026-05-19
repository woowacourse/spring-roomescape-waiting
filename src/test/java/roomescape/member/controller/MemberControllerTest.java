package roomescape.member.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.AcceptanceTest;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

class MemberControllerTest extends AcceptanceTest {

    @Test
    @DisplayName("회원가입")
    void register() {
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
