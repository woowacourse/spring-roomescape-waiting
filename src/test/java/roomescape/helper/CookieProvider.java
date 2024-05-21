package roomescape.helper;

import io.restassured.RestAssured;
import io.restassured.http.Cookies;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import roomescape.service.login.dto.LoginRequest;

@Component
public class CookieProvider {
    public Cookies createCookies() { // TODO: 어노테이션으로 주입해주는 방식 고려해보기
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new LoginRequest("user@gmail.com", "1234567890"))
                .when().post("/login")
                .then().log().all()
                .extract().detailedCookies();
    }

    public Cookies createAdminCookies() {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new LoginRequest("admin@gmail.com", "1234567890"))
                .when().post("/login")
                .then().log().all()
                .extract().detailedCookies();
    }
}
