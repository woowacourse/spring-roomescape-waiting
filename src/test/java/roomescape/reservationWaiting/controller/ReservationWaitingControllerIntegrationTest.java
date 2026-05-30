package roomescape.reservationWaiting.controller;

import static roomescape.testSupport.RestAssuredTestHelper.createReservation;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationWaiting;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservationWaiting.controller.dto.ReservationWaitingRequest;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
class ReservationWaitingControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("예약 대기 신청에 성공하면 201을 반환한다.")
    void createReservationWaitingTest() {
        //given
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");

        createReservation("브라운", LocalDate.now().plusDays(7), 1L, 1L);

        ReservationWaitingRequest body = new ReservationWaitingRequest(
                "포비",
                LocalDate.now().plusDays(7),
                1L,
                1L
        );

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations-waitings")
                .then().statusCode(201);
    }

    @Test
    @DisplayName("예약 대기 삭제에 성공하면 204을 반환한다.")
    void deleteReservationWaitingTest() {
        //given
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");

        createReservation("brown", LocalDate.now().plusDays(7), 1L, 1L);
        createReservationWaiting("gump", LocalDate.now().plusDays(7), 1L, 1L);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "gump")
                .when().delete("/reservations-waitings/1")
                .then().statusCode(204);
    }
}
