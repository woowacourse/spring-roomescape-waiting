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
import org.springframework.lang.NonNull;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservationWaiting.controller.dto.ReservationWaitingRequest;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.theme.controller.dto.ThemeRequest;
import roomescape.time.controller.dto.ReservationTimeRequest;

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

        // 5. 브라운이 예약된 시간을 다시 조회하면 예약 여부가 alreadyBooked: true 로 나온다.
        RestAssured.given()
                .queryParam("date", LocalDate.now().plusDays(1).toString())
                .queryParam("themeId", themeId)
                .when().get("/times/available-times")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].alreadyBooked", is(true));

        // 6. 포비가 동일한 시간에 예약 대기를 신청한다.
        ReservationWaitingRequest waitingRequest = new ReservationWaitingRequest("pobi", LocalDate.now().plusDays(1),
                timeId, themeId);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/reservations-waitings")
                .then().statusCode(201);

        // 7. 포비가 자신의 예약(대기 포함)을 조회하여 상태가 'WAITING'인지 확인한다.
        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("name", "pobi")
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("waiting"));

        // 8. 브라운이 자신의 예약을 취소한다.
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "brown")
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        // 9. 포비가 자신의 예약(대기 포함)을 다시 조회하여 상태가 'RESERVED'가 되었는지 확인한다. (대기 자동 승급)
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "pobi")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("reserved"));
    }

    @NonNull
    private static Long createReservationByBrown(Long timeId, Long themeId) {
        ReservationRequest reservationRequest = new ReservationRequest("brown", LocalDate.now().plusDays(1), timeId,
                themeId);
        Long reservationId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
        return reservationId;
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

    @NonNull
    private static Long createReservationTimeByAdmin() {
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        Long timeId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
        return timeId;
    }

    @NonNull
    private static Long createThemeByAdmin() {
        ThemeRequest themeRequest = new ThemeRequest("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");
        Long themeId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
        return themeId;
    }
}
