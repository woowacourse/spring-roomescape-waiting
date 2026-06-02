package roomescape.global;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.theme.controller.dto.ThemeRequest;
import roomescape.time.controller.dto.ReservationTimeRequest;
import roomescape.waiting.controller.dto.ReservationWaitingRequest;

@SpringWebTest
public class RoomEscapeE2ETest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("방탈출 예약 시스템 전체 시나리오를 테스트한다.")
    void roomEscapeE2EScenario() {
        Long themeId = createThemeByAdmin();
        Long timeId = createReservationTimeByAdmin();

        readAvailableTimeByBrown(themeId);
        Long reservationId = createReservationByBrown(timeId, themeId);

        readAvailableTimeAlreadyBookedByBrown(themeId);
        createReservationWaitingByPobi(timeId, themeId);
        readReservationStatusWaitingByPobi();
        deleteReservationByBrown(reservationId);
        readReservationStatusReservedByPobi();
    }

    private static Long createReservationByBrown(Long timeId, Long themeId) {
        ReservationRequest reservationRequest = new ReservationRequest("brown", LocalDate.now().plusDays(1), timeId,
                themeId);
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private static void readAvailableTimeByBrown(Long themeId) {
        RestAssured.given()
                .queryParam("date", LocalDate.now().plusDays(1).toString())
                .queryParam("themeId", themeId)
                .when().get("/times/available-times")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].alreadyBooked", is(false));
    }

    private static Long createReservationTimeByAdmin() {
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private static Long createThemeByAdmin() {
        ThemeRequest themeRequest = new ThemeRequest("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private static void readAvailableTimeAlreadyBookedByBrown(Long themeId) {
        RestAssured.given()
                .queryParam("date", LocalDate.now().plusDays(1).toString())
                .queryParam("themeId", themeId)
                .when().get("/times/available-times")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].alreadyBooked", is(true));
    }

    private static void createReservationWaitingByPobi(Long timeId, Long themeId) {
        ReservationWaitingRequest waitingRequest = new ReservationWaitingRequest("pobi", LocalDate.now().plusDays(1),
                timeId, themeId);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/reservations-waitings")
                .then().statusCode(201);
    }

    private static void readReservationStatusWaitingByPobi() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("name", "pobi")
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("waiting"));
    }

    private static void deleteReservationByBrown(Long reservationId) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "brown")
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }

    private static void readReservationStatusReservedByPobi() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "pobi")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("reserved"));
    }
}
