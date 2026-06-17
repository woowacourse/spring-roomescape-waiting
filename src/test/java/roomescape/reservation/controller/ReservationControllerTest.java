package roomescape.reservation.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

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
class ReservationControllerTest {

    @LocalServerPort
    int port;

    String sessionId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        sessionId = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user1@test.com", "password", "1234"))
                .post("/login")
                .then()
                .extract().cookie("JSESSIONID");
    }

    @DisplayName("예약 생성 성공")
    @Test
    void 예약_생성_성공() {
        given()
                .cookie("JSESSIONID", sessionId)
                .contentType(ContentType.JSON)
                .body(Map.of("date", "2099-08-05", "timeId", 1, "themeId", 2))
                .post("/bookings")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("memberName", equalTo("user1"));
    }

    @DisplayName("내 예약 목록 조회 성공")
    @Test
    void 내_예약_조회_성공() {
        given()
                .cookie("JSESSIONID", sessionId)
                .get("/reservations-mine")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThanOrEqualTo(1));
    }

    @DisplayName("예약 삭제 성공")
    @Test
    void 예약_삭제_성공() {
        given()
                .cookie("JSESSIONID", sessionId)
                .delete("/bookings/11")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("로그인 없이 예약 생성 시 401")
    @Test
    void 비인증_예약_생성_실패() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("date", "2099-08-05", "timeId", 1, "themeId", 1))
                .post("/bookings")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
