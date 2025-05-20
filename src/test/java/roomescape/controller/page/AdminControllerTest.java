package roomescape.controller.page;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminControllerTest {

    String token;

    @BeforeEach
    void createToken() {
        token = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithMember())
            .when().post("/login")
            .then().log().all()
            .extract().response().getCookie("token");
    }

    @Test
    @DisplayName("/admin 요청시 어드민 페이지 응답")
    void mainPage() {
        RestAssured.given().log().all()
            .cookie("token", token)
            .when().get("/admin")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("/admin/reservation 요청시 예약 관리 페이지 응답")
    void reservationPage() {
        RestAssured.given().log().all()
            .cookie("token", token)
            .when().get("/admin/reservation")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("/admin/time 요청시 시간 관리 페이지 응답")
    void timePage() {
        RestAssured.given().log().all()
            .cookie("token", token)
            .when().get("/admin/time")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("/admin/theme 요청시 테마 관리 페이지 응답")
    void themePage() {
        RestAssured.given().log().all()
            .cookie("token", token)
            .when().get("/admin/theme")
            .then().log().all()
            .statusCode(200);
    }

    private Map<String, String> getTestParamsWithMember() {
        return Map.of(
            "email", "admin",
            "password", "1234"
        );
    }
}
