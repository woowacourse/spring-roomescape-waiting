package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.service.dto.request.TokenRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AdminPageTest extends AcceptanceTest{

    private String accessToken;

    @BeforeEach
    void insert() {
        TokenRequest tokenRequest = new TokenRequest("password", "admin@email.com");
        accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");
    }

    @DisplayName("어드민 메인 페이지 테스트")
    @Test
    void adminMainPage() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("예약 어드민 페이지 테스트")
    @Test
    void readReservations() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("시간 추가 페이지 테스트")
    @Test
    void timePage() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }
}
