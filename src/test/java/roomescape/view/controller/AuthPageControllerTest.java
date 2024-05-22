package roomescape.view.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthPageControllerTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("/login 으로 GET 요청을 보내면 login 페이지와 200 OK 를 받는다.")
    void getMainPage() {
        RestAssured.given().log().all()
                .port(port)
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }
}
