package roomescape.auth;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthorizationTest extends AuthApiTestSupport {

    @Test
    @DisplayName("일반 유저가 매니저 권한 api 호출 시 403 예외가 발생한다.")
    void authorization_테스트_1(){
        RestAssured.given().log().all()
                .header("Authorization", bearer(loginUserToken()))
                .when().get("/api/manager/**")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("매니저는 매니저 권한 api 호출 시 예외가 발생하지 않는다.")
    void authorization_테스트_2(){
        RestAssured.given().log().all()
                .header("Authorization", bearer(loginManagerToken()))
                .when().get("/api/manager/times")
                .then().log().all()
                .statusCode(200);
    }
}
