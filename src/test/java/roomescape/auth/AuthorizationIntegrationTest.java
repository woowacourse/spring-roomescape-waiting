package roomescape.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthorizationIntegrationTest extends AuthApiTestSupport{

    @Test
    @DisplayName("일반 유저가 매니저권한 url(/api/manager/times)에 접근하면 403예외가 발생한다.")
    void 인가_테스트_1() {
        RestAssured.given()
                .header("Authorization", bearer(loginUserToken()))
                .contentType(ContentType.JSON)
                .when().get("/api/manager/times")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("일반 유저가 매니저권한 url(/api/manager/schedules)에 접근하면 403예외가 발생한다.")
    void 인가_테스트_2() {
        RestAssured.given()
                .header("Authorization", bearer(loginUserToken()))
                .contentType(ContentType.JSON)
                .when().get("/api/manager/schedules")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("매니저 유저는 매니저권한 url(/api/manager/schedules)에 접근할 수 있다.")
    void 인가_테스트_3() {
        RestAssured.given()
                .header("Authorization", bearer(loginAdminToken()))
                .contentType(ContentType.JSON)
                .when().get("/api/manager/schedules")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("매니저 유저는 매니저권한 url(/api/manager/times)에 접근할 수 있다.")
    void 인가_테스트_4() {
        RestAssured.given()
                .header("Authorization", bearer(loginAdminToken()))
                .contentType(ContentType.JSON)
                .when().get("/api/manager/times")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("비회원은 비회원 권한 url(/api/themes/popular)에 접근할 수 있다.")
    void 인가_테스트_5() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/api/themes/popular")
                .then().log().all()
                .statusCode(200);
    }
}
