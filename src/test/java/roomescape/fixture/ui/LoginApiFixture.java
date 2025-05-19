package roomescape.fixture.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class LoginApiFixture {

    private LoginApiFixture() {
    }

    public static Map<String, String> memberLoginAndGetCookies(final Map<String, String> loginParams) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().cookies();
    }

    public static Map<String, String> adminLoginAndGetCookies() {
        final Map<String, String> loginParams = new HashMap<>();
        loginParams.put("email", "admin@admin.com");
        loginParams.put("password", "admin");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().cookies();
    }
}
