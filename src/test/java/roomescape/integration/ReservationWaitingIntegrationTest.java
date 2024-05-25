package roomescape.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class ReservationWaitingIntegrationTest extends IntegrationTest {
    @Nested
    @DisplayName("예약 대기 목록 조회 API")
    class FindAllReservationWaiting {
        @Test
        void 예약_대기_목록을_조회할_수_있다() {
            ReservationTime time = timeFixture.createFutureTime();
            Theme theme = themeFixture.createFirstTheme();
            Member member = memberFixture.createUserMember();
            Reservation reservation = reservationFixture.createFutureReservation(time, theme, member);
            waitingFixture.createWaiting(reservation, member);
            memberFixture.createAdminMember();

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().get("/reservations/waitings")
                    .then().log().all()
                    .statusCode(200)
                    .body("waitings.size()", is(1));
        }
    }

    @Nested
    @DisplayName("예약 대기 추가 API")
    class SaveReservationWaiting {
        ReservationTime time;
        Theme theme;
        Member user;
        Member admin;
        Reservation reservation;
        Map<String, String> params;

        @BeforeEach
        void setUp() {
            time = timeFixture.createFutureTime();
            theme = themeFixture.createFirstTheme();
            admin = memberFixture.createAdminMember();
            reservation = reservationFixture.createFutureReservation(time, theme, admin);
            user = memberFixture.createUserMember();
            params = new HashMap<>();
            params.put("themeId", theme.getId().toString());
            params.put("timeId", time.getId().toString());
        }

        @Test
        void 예약_대기를_추가할_수_있다() {
            params.put("date", reservation.getDate().toString());

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createUserCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/reservations/waitings/1");
        }

        @Test
        void 같은_사용자가_같은_예약에_대해선_예약_대기를_두_번_이상_추가할_수_없다() {
            waitingFixture.createWaiting(reservation, user);
            params.put("date", reservation.getDate().toString());

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createUserCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        void 본인이_예약한_건에_대해선_예약_대기를_추가할_수_없다() {
            params.put("date", reservation.getDate().toString());

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 존재하지_않는_예약에_대해선_예약_대기를_추가할_수_없다() {
            params.put("date", "2000-04-09");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createUserCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(404);
        }

        @Test
        void 지난_예약에_대해선_예약_대기를_추가할_수_없다() {
            Reservation pastReservation = reservationFixture.createPastReservation(time, theme, user);
            params.put("date", pastReservation.getDate().toString());

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createUserCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("예약 대기 삭제 API")
    class DeleteReservationWaiting {
        Reservation reservation;

        @BeforeEach
        void setUp() {
            ReservationTime time = timeFixture.createFutureTime();
            Theme theme = themeFixture.createFirstTheme();
            Member member = memberFixture.createUserMember();
            reservation = reservationFixture.createFutureReservation(time, theme, member);
            waitingFixture.createWaiting(reservation, member);
        }

        @Test
        void 예약id로_본인의_예약_대기를_삭제할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createUserCookies())
                    .when().delete("/reservations/waitings/" + reservation.getId())
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        void 예약id가_존재하지_않는_예약_대기는_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createUserCookies())
                    .when().delete("/reservations/waitings/10")
                    .then().log().all()
                    .statusCode(404);
        }

        @Test
        void 본인이_예약자로_존재하지_않는_예약_대기는_삭제할_수_없다() {
            memberFixture.createAdminMember();

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/reservations/waitings/" + reservation.getId())
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
