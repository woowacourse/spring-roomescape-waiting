package roomescape.util;

import io.restassured.RestAssured;
import org.springframework.http.MediaType;
import roomescape.service.dto.request.LoginRequest;

public class TokenGenerator {

    public static String makeUserToken() {
        return RestAssured.given().log().all()
                .body(new LoginRequest("user@naver.com", "1234"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    public static String makeAdminToken() {
        return RestAssured.given().log().all()
                .body(new LoginRequest("admin@naver.com", "1234"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }
}
