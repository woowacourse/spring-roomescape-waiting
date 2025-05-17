package roomescape.user;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class UserViewControllerTest {

    @DisplayName("사용자 인기 테마 메인 페이지를 출력한다")
    @Test
    void checkUserDisplay_Main_RankedTheme() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("사용자 예약 내역 페이지를 출력한다")
    @Test
    void checkUserDisplay_Reservation() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("사용자 로그인 페이지를 출력한다")
    @Test
    void checkUserDisplay_Login() {
        RestAssured.given().log().all()
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("사용자 회원 가입 페이지를 출력한다")
    @Test
    void checkUserDisplay_Signup() {
        RestAssured.given().log().all()
                .when().get("/signup")
                .then().log().all()
                .statusCode(200);
    }
}
