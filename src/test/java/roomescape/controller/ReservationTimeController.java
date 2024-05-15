package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.web.controller.request.ReservationTimeWebRequest;

class ReservationTimeController extends ControllerTest {


    @BeforeEach
    void setInitialData() {
        reservationTimeRepository.save(VALID_RESERVATION_TIME);
    }

    @DisplayName("예약 시간을 저장한다 -> 201")
    @Test
    void create() {
        ReservationTimeWebRequest request = new ReservationTimeWebRequest("12:00");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/times")
            .then().log().all()
            .statusCode(201)
            .body("id", is(2));

    }

    @DisplayName("예약 시간을 삭제한다 -> 204")
    @Test
    void deleteBy() {
        RestAssured.given().log().all()
            .when().delete("/times/1")
            .then().log().all()
            .statusCode(204);
    }

    @DisplayName("예약 시간을 조회한다. -> 200")
    @Test
    void getReservationTimes() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/times")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1));

    }

    @DisplayName("예약 시간 포맷이 잘못될 경우 -> 400")
    @Test
    void create_IllegalTimeFormat() {
        ReservationTimeWebRequest request = new ReservationTimeWebRequest("24:00");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/times")
            .then().log().all()
            .statusCode(400);

    }

    @DisplayName("예약 시간이 중복될 경우  -> 400")
    @Test
    void create_duplicate() {
        ReservationTimeWebRequest request = new ReservationTimeWebRequest(VALID_RESERVATION_TIME.getStartAt().toString());
        System.out.println(reservationTimeRepository.count());
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/times")
            .then().log().all()
            .statusCode(400);

    }

    @DisplayName("예약이 존재하는 시간 삭제 -> 400")
    @Test
    void delete_ReservationExists() {
        themeRepository.save(VALID_THEME);
        memberRepository.save(VALID_MEMBER);
        reservationRepository.save(VALID_RESERVATION);

        RestAssured.given().log().all()
            .when().delete("/times/1")
            .then().log().all()
            .statusCode(400);

    }

    @DisplayName("요청 포맷이 잘못될 경우 -> 400")
    @Test
    void create_MethodArgNotValid() {
        ReservationTimeWebRequest request = new ReservationTimeWebRequest(null);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/times")
            .then().log().all()
            .statusCode(400);

    }
}
