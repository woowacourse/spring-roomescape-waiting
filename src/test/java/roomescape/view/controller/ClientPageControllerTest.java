package roomescape.view.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ClientPageControllerTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("/ 으로 GET 요청을 보내면 index 페이지와 200 OK 를 받는다.")
    void getMainPage() {
        RestAssured.given().log().all()
                .port(port)
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("/reservation 으로 GET 요청을 보내면 방탈출 예약 페이지와 200 OK 를 받는다.")
    void getAdminPage() {
        RestAssured.given().log().all()
                .port(port)
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }
}
