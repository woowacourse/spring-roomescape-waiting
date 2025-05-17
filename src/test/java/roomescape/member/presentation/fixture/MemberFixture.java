package roomescape.member.presentation.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import roomescape.member.presentation.dto.TokenRequest;

public class MemberFixture {

    private static final String USER_EMAIL = "user@user.com";
    private static final String USER_PASSWORD = "user";
    private static final String USER_NAME = "유저";
    private static final String ADMIN_EMAIL = "admin@admin.com";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN = "어드민";

    public TokenRequest createLoginRequest(String email, String password) {
        return new TokenRequest(email, password);
    }

    public Map<String, String> loginUser() {
        TokenRequest tokenRequest = createLoginRequest(USER_EMAIL, USER_PASSWORD);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().all()
                .extract().cookies();
    }

    public Map<String, String> loginAdmin() {
        TokenRequest tokenRequest = createLoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().all()
                .extract().cookies();
    }

}
