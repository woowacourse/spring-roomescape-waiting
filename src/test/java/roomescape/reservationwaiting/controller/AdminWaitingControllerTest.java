package roomescape.reservationwaiting.controller;

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
class AdminWaitingControllerTest {

    @LocalServerPort
    int port;

    String adminSession;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        adminSession = login("user1@test.com");
    }

    private String login(String email) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", "1234"))
                .post("/login")
                .then()
                .extract().cookie("JSESSIONID");
    }

    @DisplayName("어드민이 전체 대기 목록을 조회한다")
    @Test
    void 어드민_대기_전체_조회() {
        given()
                .cookie("JSESSIONID", adminSession)
                .get("/admin/waitings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(3));
    }

    @DisplayName("어드민이 대기를 취소한다")
    @Test
    void 어드민_대기_취소() {
        given()
                .cookie("JSESSIONID", adminSession)
                .delete("/admin/waitings/1")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("일반 회원은 어드민 대기 API에 접근할 수 없다")
    @Test
    void 일반회원_접근_거부() {
        String userSession = login("user2@test.com");

        given()
                .cookie("JSESSIONID", userSession)
                .get("/admin/waitings")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}