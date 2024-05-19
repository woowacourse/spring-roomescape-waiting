package roomescape.controller.web;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import roomescape.service.dto.request.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AdminControllerTest {

    @Test
    @DisplayName("어드민 메인 페이지로 정상적으로 이동한다.")
    void moveToAdminMainPage_Success() {
        String token = RestAssured.given().log().all()
                .body(new LoginRequest("admin@naver.com", "1234"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("예약 페이지 요청이 정상적으로 수행된다.")
    void moveToAdminMainPage_Failure() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }
}
