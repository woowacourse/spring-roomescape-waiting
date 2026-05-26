package roomescape.reservation.presentation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import roomescape.config.TestTimeConfig;
import roomescape.reservation.presentation.dto.ReservationChangeRequest;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.theme.presentation.dto.ThemeRequest;
import roomescape.time.presentation.dto.ReservationTimeRequest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestTimeConfig.class)
class ReservationControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @Autowired
    private Clock clock;

    @Test
    @DisplayName("예약이 존재할 때, 새 예약을 저장하면 대기 상태로 저장된다.")
    void pendingReservationTest() {
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(
                LocalTime.now(clock)
        );
        long timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
        ThemeRequest themeRequest = ThemeRequest.builder()
                .name("판타지")
                .durationTime(LocalTime.now(clock))
                .description("판타지래요")
                .thumbnailImageUrl("https://~~~")
                .build();
        long themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        ReservationRequest reservationRequest = new ReservationRequest(
                "포비",
                LocalDate.now(clock),
                timeId,
                themeId
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        ReservationRequest newReservationRequest = new ReservationRequest(
                "리사",
                LocalDate.now(clock),
                timeId,
                themeId
        );

        RestAssured.given().log().all()
                .when().contentType(ContentType.JSON)
                .body(newReservationRequest)
                .post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("예약이 존재할 때, 기존 예약을 수정하면 대기 상태로 저장된다.")
    void pendingExistsReservationTest() {
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(
                LocalTime.now(clock)
        );
        long timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
        ThemeRequest themeRequest = ThemeRequest.builder()
                .name("판타지")
                .durationTime(LocalTime.now(clock))
                .description("판타지래요")
                .thumbnailImageUrl("https://~~~")
                .build();
        long themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        ReservationRequest reservationRequest = new ReservationRequest(
                "포비",
                LocalDate.now(clock),
                timeId,
                themeId
        );

        long reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        ReservationRequest newReservationRequest = new ReservationRequest(
                "리사",
                LocalDate.now(clock).plusDays(1),
                timeId,
                themeId
        );

        RestAssured.given().log().all()
                .when().contentType(ContentType.JSON)
                .body(newReservationRequest)
                .post("/reservations")
                .then().log().all()
                .statusCode(201);

        ReservationChangeRequest reservationChangeRequest = new ReservationChangeRequest(reservationRequest.name(), reservationRequest.date().plusDays(1), reservationRequest.timeId(), reservationRequest.themeId());
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationChangeRequest)
                .when().patch("/reservations/" + reservationId + "/pending")
                .then().log().all()
                .statusCode(200);
    }
}
