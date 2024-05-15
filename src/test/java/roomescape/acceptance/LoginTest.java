package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.service.dto.request.TokenRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LoginTest extends AcceptanceTest {

    private String accessToken;

    @BeforeEach
    void setAccessToken() {
        TokenRequest tokenRequest = new TokenRequest("password", "admin@email.com");
        accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");
    }

    @DisplayName("로그인된 회원 정보를 가져온다.")
    @Test
    void login() {
        TokenRequest request = new TokenRequest("password", "admin@email.com");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(request)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("ADMIN 권한을 가진 회원만 admin 페이지에 접근 가능하다.")
    @Test
    void roleTest() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);

        TokenRequest request = new TokenRequest("password", "asd@email.com");
        String userToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", userToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }
}
