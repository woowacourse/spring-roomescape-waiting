package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.InitialMemberFixture.COMMON_PASSWORD;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialReservationFixture.INITIAL_RESERVATION_COUNT;
import static roomescape.InitialReservationFixture.RESERVATION_2;
import static roomescape.InitialReservationFixture.RESERVATION_4;
import static roomescape.InitialWaitingFixture.INITIAL_WAITING_COUNT;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.login.dto.LoginRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.WaitingRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/initial_test_data.sql")
class ReservationControllerTest {

    @Test
    @DisplayName("저장된 reservation을 모두 반환한다.")
    void getReservations() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(INITIAL_RESERVATION_COUNT));
    }

    @Test
    @DisplayName("로그인 후 Reservation을 추가한다.")
    void addReservation() {
        //given
        LoginRequest loginRequest = new LoginRequest(COMMON_PASSWORD.password(), MEMBER_1.getEmail().email());

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(INITIAL_RESERVATION_COUNT + 1))
                .header("Location", String.format("/reservations/%d", INITIAL_RESERVATION_COUNT + 1));
    }

    @Test
    @DisplayName("로그인 후 Waiting을 추가한다.")
    void addWaiting() {
        //given
        LoginRequest loginRequest = new LoginRequest(COMMON_PASSWORD.password(), MEMBER_1.getEmail().email());

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        WaitingRequest waitingRequest = new WaitingRequest(
                RESERVATION_4.getDate(),
                RESERVATION_4.getReservationTime().getId(),
                RESERVATION_4.getTheme().getId()
        );

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/reservations/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", is(INITIAL_WAITING_COUNT + 1))
                .header("Location", String.format("/reservations/waitings/%d", INITIAL_WAITING_COUNT + 1));
    }

    @Test
    @DisplayName("Reservation을 삭제한다.")
    void deleteReservation() {
        //given
        LoginRequest loginRequest = new LoginRequest(COMMON_PASSWORD.password(), MEMBER_1.getEmail().email());

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        //when
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/reservations/" + RESERVATION_2.getId())
                .then().log().all()
                .statusCode(204);

        //then
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(INITIAL_RESERVATION_COUNT - 1));
    }

    @Test
    @DisplayName("Waiting을 삭제한다.")
    void deleteWaiting() {
        //given
        LoginRequest loginRequest = new LoginRequest(COMMON_PASSWORD.password(), MEMBER_2.getEmail().email());
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/reservations/waitings/1")
                .then().log().all()
                .statusCode(204);
    }
}
