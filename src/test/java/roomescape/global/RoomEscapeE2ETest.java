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
        // 1. 관리자가 테마를 생성한다.
        ThemeRequest themeRequest = new ThemeRequest("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");
        Long themeId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // 2. 관리자가 예약 시간을 생성한다.
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        Long timeId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // 3. 브라운이 예약 가능한 시간을 조회한다.
        RestAssured.given()
                .queryParam("date", LocalDate.now().plusDays(1).toString())
                .queryParam("themeId", themeId)
                .when().get("/times/available-times")
                .then().statusCode(200)
                .body("size()", is(1));

        // 4. 브라운이 해당 시간에 예약을 생성한다.
        ReservationRequest reservationRequest = new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, themeId);
        Long reservationId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // 5. 브라운이 예약된 시간을 다시 조회하면 빈 리스트가 나온다. (예약 불가)
        RestAssured.given()
                .queryParam("date", LocalDate.now().plusDays(1).toString())
                .queryParam("themeId", themeId)
                .when().get("/times/available-times")
                .then().statusCode(200)
                .body("size()", is(0));

        // 6. 포비가 동일한 시간에 예약 대기를 신청한다.
        ReservationWaitingRequest waitingRequest = new ReservationWaitingRequest("포비", LocalDate.now().plusDays(1), timeId, themeId);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/reservations-waitings")
                .then().statusCode(201);

        // 7. 포비가 자신의 예약(대기 포함)을 조회하여 상태가 'WAITING'인지 확인한다.
        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("name", "포비")
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("waiting"));

        // 8. 브라운이 자신의 예약을 취소한다.
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "브라운")
                .when().delete("/reservations/" + reservationId)
                .then().statusCode(204);

        // 9. 포비가 자신의 예약(대기 포함)을 다시 조회하여 상태가 여전히 'WAITING'인지 확인한다. (아직 승급 로직 미구현으로 가정)
        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("name", "포비")
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("waiting"));
    }
}
