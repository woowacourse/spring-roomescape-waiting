package roomescape.api;

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
import roomescape.infrastructure.JwtProvider;

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
        ReservationRequest reservationRequest = createReservationRequest();

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
        ReservationRequest reservationRequest = createReservationRequest();
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
        ReservationRequest reservationRequest = createReservationRequest();
        addReservation(reservationRequest);

        RestAssured.given().log().all()
                .port(port)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
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
        ReservationRequest reservationRequest = createReservationRequest();
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

    private ReservationRequest createReservationRequest() {
        return new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 2L);
    }

    private void addReservation(final ReservationRequest reservationRequest) {
        Cookie cookieByAdminLogin = getCookieByLogin(port, "admin@email.com", "123456");
        RestAssured.given().log().all()
                .port(port)
                .cookie(cookieByAdminLogin)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/admin/reservations");
    }
}
