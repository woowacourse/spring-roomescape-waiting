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
public class ReservationAdminControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("관리자가 모든 예약을 조회한다.")
    void getAllReservations_success() {
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");

        createReservation("브라운", LocalDate.now().plusDays(1), 1L, 1L);
        createReservation("포비", LocalDate.now().plusDays(2), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("관리자가 예약을 삭제한다.")
    void deleteReservation_success() {
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");

        Long id = createReservation("브라운", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/reservations/" + id)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }
}
