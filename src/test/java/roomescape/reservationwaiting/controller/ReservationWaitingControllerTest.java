package roomescape.reservationwaiting.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationWaitingControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약 대기 생성 성공")
    void 예약_대기_생성_성공() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "reservationId", 12))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", equalTo(4))
                .body("name", equalTo("현미밥"))
                .body("reservationId", equalTo(12));
    }

    @Test
    @DisplayName("예약 대기 삭제 성공")
    void 예약_대기_삭제_성공() {
        RestAssured.given().log().all()
                .when().delete("/waitings/2")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("이름으로 예약 대기 조회")
    void 예약_대기_조회_성공() {
        RestAssured.given().log().all()
                .queryParam("name", "user3")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(3))
                .body("[0].turn", equalTo(2));
    }
}

