package roomescape.reservationtime.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("시간 생성 성공")
    @Test
    void 시간_생성_성공() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "20:00", "finishAt", "21:00"))
                .post("/times")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("startAt", equalTo("20:00:00"));
    }

    @DisplayName("전체 시간 조회 성공")
    @Test
    void 전체_시간_조회_성공() {
        given()
                .get("/times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(3));
    }

    @DisplayName("시간 삭제 성공")
    @Test
    void 시간_삭제_성공() {
        int id = given()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "20:00", "finishAt", "21:00"))
                .post("/times")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        given()
                .delete("/times/" + id)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}