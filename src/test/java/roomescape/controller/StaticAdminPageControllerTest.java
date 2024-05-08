package roomescape.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.request.UserLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class StaticAdminPageControllerTest {

    @DisplayName("관리자 권한이 아니면 403을 반환한다.")
    @Test
    void should_status_403_when_not_admin_role() {
        UserLoginRequest loginRequest = new UserLoginRequest("1111", "dmsgml@email.com");

        String cookie = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Set-Cookie");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("관리자 권한으로 요청하면 200을 응답한다.")
    @Test
    void should_status_200_when_admin_role() {
        UserLoginRequest loginRequest = new UserLoginRequest("2222", "pobi@email.com");

        String cookie = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Set-Cookie");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }
}
