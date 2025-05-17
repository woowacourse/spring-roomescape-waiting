package roomescape.member.presentation.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import roomescape.member.presentation.dto.TokenRequest;

public class MemberFixture {

    private static final String USER_EMAIL = "user@user.com";
    private static final String USER_PASSWORD = "user";
    private static final String ADMIN_EMAIL = "admin@admin.com";
    private static final String ADMIN_PASSWORD = "admin";

    public TokenRequest createLoginRequest(String email, String password) {
        return new TokenRequest(email, password);
    }

    public Map<String, String> loginUser() {
        return login(USER_EMAIL, USER_PASSWORD);
    }

    public Map<String, String> loginAdmin() {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    private Map<String, String> login(String email, String password) {
        TokenRequest tokenRequest = createLoginRequest(email, password);

        return RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().ifValidationFails()
                .extract().cookies();
    }

}
