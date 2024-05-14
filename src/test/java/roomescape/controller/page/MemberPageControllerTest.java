package roomescape.controller.page;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

//@formatter:off
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MemberPageControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("/reservation 요청시 사용자 예약 페이지를 응답한다.")
    void response_user_reservation_page() {
        RestAssured.given().contentType(ContentType.JSON)
                   .when().get("/reservation")
                   .then().statusCode(200);
    }


    @Test
    @DisplayName("/ 요청시 인기 테마 페이지를 응답한다.")
    void response_popular_theme_page() {
        RestAssured.given().contentType(ContentType.JSON)
                   .when().get("/")
                   .then().statusCode(200);
    }
    @Test
    @DisplayName("/login 요청시 로그인 페이지를 응답한다.")
    void response_user_login_page() {
        RestAssured.given().contentType(ContentType.JSON)
                .when().get("/login")
                .then().statusCode(200);
    }
    @Test
    @DisplayName("/signup 요청시 회원가입 페이지를 응답한다.")
    void response_user_signup_page() {
        RestAssured.given().contentType(ContentType.JSON)
                .when().get("/signup")
                .then().statusCode(200);
    }
}
