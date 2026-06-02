package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testSupport.RestAssuredTestHelper.createReservation;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
class ReservationAdminControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("모든 예약을 성공적으로 조회한다.")
    void getAllReservations_Success() {
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");

        createReservation("브라운", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(1))
                .body("[0].name", is("브라운"));
    }

    @Test
    @DisplayName("예약을 성공적으로 삭제한다.")
    void deleteReservation_Success() {
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");

        Long id = createReservation("브라운", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/reservations/" + id)
                .then().log().all()
                .statusCode(204);
    }
}
