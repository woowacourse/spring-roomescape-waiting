package roomescape.member.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

public class MemberApiFixture {

    private MemberApiFixture() {
    }

    public static Integer registerMember(String name, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("password", password);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/members")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("id");
    }

}
