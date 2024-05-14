package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberPageControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("Reservation Page 접근 성공 테스트")
    @Test
    void responseReservationPage() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("메인 페이지 접근 성공 테스트")
    @Test
    void responseMainPage() {
        RestAssured.given().log().all()
                .when().get("")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }
}
