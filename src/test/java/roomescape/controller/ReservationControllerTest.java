package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.Fixture.COOKIE_NAME;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;
import static roomescape.Fixture.VALID_USER_EMAIL;
import static roomescape.Fixture.VALID_USER_NAME;
import static roomescape.Fixture.VALID_USER_PASSWORD;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.MemberRole;
import roomescape.web.controller.request.MemberReservationWebRequest;

class ReservationControllerTest extends ControllerTest {

    @BeforeEach
    void setInitialData() {
        reservationTimeRepository.save(VALID_RESERVATION_TIME);
        themeRepository.save(VALID_THEME);
        memberRepository.save(VALID_MEMBER);
        reservationRepository.save(VALID_RESERVATION);
        /*entityManager.persist(VALID_RESERVATION_TIME);
        entityManager.persist(VALID_THEME);
        entityManager.persist(VALID_MEMBER);
        entityManager.persist(VALID_RESERVATION);
        entityManager.flush();*/
    }

    @DisplayName("예약을 저장한다. -> 201")
    @Test
    void reserve() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2040-01-02", 1L, 1L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("name", is(VALID_USER_NAME.getName()));
    }

    @DisplayName("예약을 삭제한다. -> 204")
    @Test
    void deleteBy() {
        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(204);
    }

    @DisplayName("예약을 조회한다. -> 200")
    @Test
    void getReservations() {
        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1));
    }

    @DisplayName("실패: 예약 날짜가 잘못될 경우 -> 400")
    @Test
    void reserve_IllegalDateRequest() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2040-00-02", 1L, 1L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(COOKIE_NAME, getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("실패: 존재하지 않는 테마에 대한 예약  -> 400")
    @Test
    void reserve_NoSuchTheme() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2040-01-02", 1L, 200L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(COOKIE_NAME, getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("실패: 존재하지 않는 예약 시간에 대한 예약  -> 400")
    @Test
    void reserve_NoSuchTime() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2040-01-02", 100L, 1L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(COOKIE_NAME, getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("과거 시간에 예약을 넣을 경우 -> 400")
    @Test
    void reserve_PastTime() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2024-05-10", 100L, 1L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(COOKIE_NAME, getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }
}
