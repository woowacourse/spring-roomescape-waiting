package roomescape.member.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.AcceptanceTest;

import java.util.HashMap;
import java.util.Map;

import static roomescape.member.fixture.MemberApiFixture.registerMember;

class AuthControllerTest extends AcceptanceTest {

    @Test
    @DisplayName("로그인하면 헤더에 토큰이 발급된다.")
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

}
