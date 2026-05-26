package roomescape.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthenticationIntegrationTest extends AuthApiTestSupport {

    @Test
    @DisplayName("헤더에 인증된 토큰이 있다면 보호된 url에 접근할 수 있다.")
    void 인증_테스트_1() {
        RestAssured.given().log().all()
                .header("Authorization", bearer(loginUserToken()))
                .contentType(ContentType.JSON)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("헤더에 토큰이 없다면 401 예외가 발생한다.")
    void 인증_테스트_2() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("Authorization 필드에 Bearer 접두사가 없다면 401 예외가 발생한다.")
    void 인증_테스트_3() {
        RestAssured.given().log().all()
                .header("Authorization", loginUserToken())
                .contentType(ContentType.JSON)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("비회원은 보호된 url에 접근하면 401이 발생한다.")
    void 인증_테스트_4() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(401);
    }
}
