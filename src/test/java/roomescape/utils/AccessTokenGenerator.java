package roomescape.utils;

import io.restassured.RestAssured;
import org.springframework.http.MediaType;
import roomescape.core.dto.auth.TokenRequest;

public class AccessTokenGenerator {
    private static final String ADMIN_EMAIL = TestFixture.getAdminEmail();
    private static final String MEMBER_EMAIL = TestFixture.getMemberEmail();
    private static final String PASSWORD = TestFixture.getPassword();

    public static String adminTokenGenerate() {
        return RestAssured
                .given().log().all()
                .body(new TokenRequest(ADMIN_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    public static String memberTokenGenerate() {
        return RestAssured
                .given().log().all()
                .body(new TokenRequest(MEMBER_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }
}
