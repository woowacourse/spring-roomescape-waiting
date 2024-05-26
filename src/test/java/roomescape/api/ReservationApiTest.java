package roomescape.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static roomescape.LoginTestSetting.getCookieByLogin;

import java.time.LocalDate;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.UserReservationRequest;
import roomescape.dto.waiting.UserWaitingRequest;
import roomescape.infrastructure.auth.JwtProvider;

@Sql("/reservation-api-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationApiTest {

    @Autowired
    JwtProvider jwtProvider;

    @LocalServerPort
    int port;

    @Test
    void 사용자_예약_추가() {
        Cookie cookieByUserLogin = getCookieByLogin(port, "test@email.com", "123456");
        String userAccessToken = cookieByUserLogin.getValue();
        String userId = jwtProvider.getSubject(userAccessToken);

        UserReservationRequest userReservationRequest = createUserReservationRequest();

        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie(cookieByUserLogin)
                .body(userReservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations/1")
                .body("id", equalTo(1))
                .body("date", equalTo(userReservationRequest.date().toString()))
                .body("time.id", equalTo(userReservationRequest.timeId().intValue()))
                .body("theme.id", equalTo(userReservationRequest.themeId().intValue()))
                .body("member.id", equalTo(Integer.parseInt(userId)));
    }

    @Test
    void 관리자_예약_추가() {
        Cookie cookieByAdminLogin = getCookieByLogin(port, "admin@email.com", "123456");
        ReservationRequest reservationRequest = createReservationRequest(2L, 1L, 1L);

        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie(cookieByAdminLogin)
                .body(reservationRequest)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations/1")
                .body("id", equalTo(1))
                .body("date", equalTo(reservationRequest.date().toString()))
                .body("time.id", equalTo(reservationRequest.timeId().intValue()))
                .body("theme.id", equalTo(reservationRequest.themeId().intValue()))
                .body("member.id", equalTo(reservationRequest.memberId().intValue()));
    }

    @Test
    void 예약_단일_조회() {
        ReservationRequest reservationRequest = createReservationRequest(2L, 1L, 1L);
        addReservation(reservationRequest);

        RestAssured.given().log().all()
                .port(port)
                .when().get("/reservations/1")
                .then().log().all()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("date", equalTo(reservationRequest.date().toString()))
                .body("time.id", equalTo((reservationRequest.timeId().intValue())))
                .body("theme.id", equalTo(reservationRequest.themeId().intValue()))
                .body("member.id", equalTo(reservationRequest.memberId().intValue()));
    }

    @Test
    void 예약_전체_조회() {
        ReservationRequest reservationRequest = createReservationRequest(2L, 1L, 1L);
        addReservation(reservationRequest);

        RestAssured.given().log().all()
                .port(port)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 사용자_예약_전체_조회() {
        ReservationRequest otherUserReservationRequest = createReservationRequest(2L, 1L, 1L);
        ReservationRequest userReservationRequest = createReservationRequest(3L, 2L, 1L);
        addReservation(otherUserReservationRequest);
        addReservation(userReservationRequest);

        Cookie cookieByUserLogin = getCookieByLogin(port, "atom@email.com", "123456");

        RestAssured.given().log().all()
                .port(port)
                .cookie(cookieByUserLogin)
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 사용자_예약_및_예약_대기_전체_조회() {
        ReservationRequest otherUserReservationRequest = createReservationRequest(2L, 1L, 1L);
        ReservationRequest userReservationRequest = createReservationRequest(3L, 2L, 1L);
        UserWaitingRequest userWaitingRequest = createUserWaitingRequest(1L, 1L);

        addReservation(otherUserReservationRequest);
        addReservation(userReservationRequest);
        addWaiting(userWaitingRequest);

        Cookie cookieByUserLogin = getCookieByLogin(port, "atom@email.com", "123456");

        RestAssured.given().log().all()
                .port(port)
                .cookie(cookieByUserLogin)
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("status", contains("예약", "1번째 예약 대기"));
    }

    @Sql("/reservation-filter-api-test-data.sql")
    @Test
    void 예약_조회시_조회필터_적용하여_조회() {
        RestAssured.given().log().all()
                .port(port)
                .when().get("/reservations?member=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("member.id", everyItem(is(1)));
    }

    @Test
    void 예약_삭제() {
        ReservationRequest reservationRequest = createReservationRequest(2L, 1L, 1L);
        addReservation(reservationRequest);

        RestAssured.given().log().all()
                .port(port)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    private UserReservationRequest createUserReservationRequest() {
        return new UserReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
    }

    private ReservationRequest createReservationRequest(Long memberId, Long timeId, Long themeId) {
        return new ReservationRequest(LocalDate.now().plusDays(1), timeId, themeId, memberId);
    }

    private UserWaitingRequest createUserWaitingRequest(Long timeId, Long themeId) {
        return new UserWaitingRequest(LocalDate.now().plusDays(1), timeId, themeId);
    }

    private void addReservation(ReservationRequest reservationRequest) {
        Cookie cookieByAdminLogin = getCookieByLogin(port, "admin@email.com", "123456");

        RestAssured.given().log().all()
                .port(port)
                .cookie(cookieByAdminLogin)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/admin/reservations");
    }

    private void addWaiting(UserWaitingRequest waitingRequest) {
        Cookie cookieByUserLogin = getCookieByLogin(port, "atom@email.com", "123456");

        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie(cookieByUserLogin)
                .body(waitingRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);
    }
}
