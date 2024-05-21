package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import org.springframework.http.MediaType;
import roomescape.dto.login.LoginRequest;

public class LoginTestSetting {

    public static Cookie getCookieByLogin(int port, String email, String password) {
        return RestAssured
                .given().log().all()
                .port(port)
                .body(new LoginRequest(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .getDetailedCookie("token");
    }
}
