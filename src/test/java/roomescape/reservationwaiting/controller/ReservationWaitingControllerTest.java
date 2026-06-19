package roomescape.reservationwaiting.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
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
class ReservationWaitingControllerTest {

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

    @DisplayName("이미 예약된 슬롯에 신청하면 대기로 생성된다")
    @Test
    void 대기_생성_성공() {
        String date = LocalDate.now().plusDays(20).toString();

        given()
                .cookie("JSESSIONID", sessionId)
                .contentType(ContentType.JSON)
                .body(Map.of("date", date, "timeId", 1, "themeId", 1))
                .post("/bookings")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("status", equalTo("PAYMENT_PENDING"));

        String user2Session = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user2@test.com", "password", "1234"))
                .post("/login")
                .then()
                .extract().cookie("JSESSIONID");

        given()
                .cookie("JSESSIONID", user2Session)
                .contentType(ContentType.JSON)
                .body(Map.of("date", date, "timeId", 1, "themeId", 1))
                .post("/bookings")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("status", equalTo("WAITING"))
                .body("memberName", equalTo("user2"));
    }

    @DisplayName("내 대기 목록 조회 성공")
    @Test
    void 내_대기_조회_성공() {
        given()
                .cookie("JSESSIONID", sessionId)
                .get("/waitings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1));
    }

    @DisplayName("대기 삭제 성공")
    @Test
    void 대기_삭제_성공() {
        given()
                .cookie("JSESSIONID", sessionId)
                .delete("/waitings/2")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("로그인 없이 예약/대기 생성 시 401")
    @Test
    void 비인증_생성_실패() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("date", "2099-08-05", "timeId", 1, "themeId", 1))
                .post("/bookings")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
