package roomescape.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import roomescape.auth.dto.LoginRequest;

public class CookieProvider {
    public static Cookies makeUserCookie() {
        LoginRequest request = new LoginRequest("bri@abc.com", "1234");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
    }

    public static Cookies makeAdminCookie() {
        LoginRequest request = new LoginRequest("admin@abc.com", "1234");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
    }
}
