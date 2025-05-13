package roomescape.helper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.http.HttpStatus;
import roomescape.member.dto.request.AuthRequest.LoginRequest;

public class TestHelper {

    public static String login(String email, String password) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(email, password))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response();

        return response.getCookie("token");
    }

    public static Response get(String path) {
        return RestAssured.given()
                .when()
                .get(path);
    }

    public static Response getWithToken(String path, String token) {
        return RestAssured.given()
                .cookie("token", token)
                .when()
                .get(path);
    }

    public static Response post(String path, Object body) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);
    }

    public static Response postWithToken(String path, Object body, String token) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(body)
                .when()
                .post(path);
    }

    public static Response delete(String path) {
        return RestAssured.given()
                .when()
                .delete(path);
    }

    public static Response deleteWithToken(String path, String token) {
        return RestAssured.given()
                .cookie("token", token)
                .when()
                .delete(path);
    }
}
