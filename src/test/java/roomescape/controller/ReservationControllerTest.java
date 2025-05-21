package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.service.dto.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationControllerTest {

    private String sessionId;

    @BeforeEach
    void setUp() {
        final LoginRequest loginRequest = new LoginRequest("admin@email.com", "1234");
        sessionId = RestAssured.given().contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/login")
                .then()
                .extract().cookie("JSESSIONID");
    }

    @Nested
    class FailureTest {
        @DisplayName("존재하지 않는 예약을 삭제하려는 경우 404 Not Found를 던진다")
        @Test
        void reservationRemoveTest() {
            //given
            long notExistId = 999;

            //when & then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .sessionId(sessionId)
                    .when().delete("/reservations/" + notExistId)
                    .then().log().all()
                    .statusCode(404);
        }

        @DisplayName("이전 시각으로 예약을 요청하는 경우 400 Bad Request를 던진다")
        @Test
        void reservationAddBeforeCurrentDateTime() {
            Map<String, String> params = Map.of(
                    "date", LocalDate.now().minusDays(1).toString(),
                    "themeId", "1",
                    "timeId", "1"
            );

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .sessionId(sessionId)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("같은 날짜 및 시간 예약이 존재하면 400 Bad Request를 던진다")
        @Test
        void reservationAddDuplicatedTest() {
            //given
            Map<String, String> params = Map.of(
                    "date", LocalDate.now().plusDays(1).toString(),
                    "themeId", "1",
                    "timeId", "1"
            );

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .sessionId(sessionId)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201);

            Map<String, String> duplicated = Map.of(
                    "date", LocalDate.now().plusDays(1).toString(),
                    "themeId", "1",
                    "timeId", "1"
            );

            //when & then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .sessionId(sessionId)
                    .body(duplicated)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }
    }
}
