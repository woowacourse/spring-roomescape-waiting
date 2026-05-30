package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
public class ReservationControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation_success() {
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");

        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("브라운"));
    }

    @Test
    @DisplayName("이름으로 예약을 조회한다.")
    void readAllByName_success() {
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");

        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "브라운")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("브라운"));
    }
}
