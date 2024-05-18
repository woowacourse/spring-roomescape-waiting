package roomescape.core.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.springframework.http.MediaType;
import roomescape.core.dto.auth.TokenRequest;

public class e2eTest {
    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "password";

    public static String getAccessToken() {
        return RestAssured
                .given().log().all()
                .body(new TokenRequest(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    public static ValidatableResponse get(String endpoint, String cookieValue) {
        return RestAssured.given().log().all()
                .cookies("token", cookieValue)
                .when().get(endpoint)
                .then().log().all();
    }

    public static ValidatableResponse get(String endpoint) {
        return RestAssured.given().log().all()
                .when().get(endpoint)
                .then().log().all();
    }

    public static <T> ValidatableResponse post(T request, String endpoint, String cookieValue) {
        return RestAssured.given().log().all()
                .cookies("token", cookieValue)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(endpoint)
                .then().log().all();
    }

    public static <T> ValidatableResponse post(T request, String endpoint) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(endpoint)
                .then().log().all();
    }

    public static ValidatableResponse post(String endpoint) {
        return RestAssured.given().log().all()
                .when().post(endpoint)
                .then().log().all();
    }

    public static ValidatableResponse delete(String endpoint, String cookieValue) {
        return RestAssured.given().log().all()
                .cookies("token", cookieValue)
                .when().delete(endpoint)
                .then().log().all();
    }
}
