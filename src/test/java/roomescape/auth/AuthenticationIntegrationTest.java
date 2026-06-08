package roomescape.auth;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.ControllerTestSupport;

public class AuthenticationIntegrationTest extends ControllerTestSupport {

    @Test
    @DisplayName("헤더에 인증된 토큰이 있다면 보호된 url에 접근할 수 있다.")
    void authenticated_token_allows_access_to_protected_url() {
        RestAssured.given().log().all()
                .header("Authorization", bearer(loginUserToken()))
                .contentType(ContentType.JSON)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("헤더에 토큰이 없다면 401 예외가 발생한다.")
    void missing_token_header_returns_unauthorized() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("Authorization 필드에 Bearer 접두사가 없다면 401 예외가 발생한다.")
    void authorization_header_without_bearer_prefix_returns_unauthorized() {
        RestAssured.given().log().all()
                .header("Authorization", loginUserToken())
                .contentType(ContentType.JSON)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("비회원은 보호된 url에 접근하면 401이 발생한다.")
    void non_member_token_returns_unauthorized_for_protected_url() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("일반 사용자가 관리자 API에 접근하면 403 예외가 발생한다.")
    void user_role_returns_forbidden_for_admin_api() {
        RestAssured.given().log().all()
                .header("Authorization", bearer(loginUserToken()))
                .contentType(ContentType.JSON)
                .when().get("/api/manager/reservations")
                .then().log().all()
                .statusCode(403)
                .body("success", is(false))
                .body("error.code", is("FORBIDDEN_403"));
    }
}
