package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.dto.TokenRequest;

public class LoginTokenProvider {
    public static String login(String email, String password, int expectedHttpCode) {
        TokenRequest tokenRequest = new TokenRequest(email, password);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().cookies()
                .statusCode(expectedHttpCode)
                .extract().cookie("token");
    }
}
