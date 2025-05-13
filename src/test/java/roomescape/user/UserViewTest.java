package roomescape.user;


import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserViewTest {

    private final int port;

    public UserViewTest(
            @LocalServerPort final int port
    ) {
        this.port = port;
    }

    @DisplayName("/로 요청이 들어오면 웰컴 페이지를 응답한다.")
    @Test
    void welcome() {
        RestAssured.given().port(port).log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/reservation로 요청이 들어오면 예약 페이지를 응답한다.")
    @Test
    void reservation() {
        RestAssured.given().port(port).log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/login으로 요청이 들어오면 로그인 페이지를 응답한다.")
    @Test
    void login() {
        RestAssured.given().port(port).log().all()
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/signup으로 요청이 들어오면 회원가입 페이지를 응답한다.")
    @Test
    void signup() {
        RestAssured.given().port(port).log().all()
                .when().get("/signup")
                .then().log().all()
                .statusCode(200);
    }
}
