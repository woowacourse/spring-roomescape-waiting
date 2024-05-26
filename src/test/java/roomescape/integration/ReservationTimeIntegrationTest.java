package roomescape.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.service.reservationtime.dto.ReservationTimeAvailableListResponse;

class ReservationTimeIntegrationTest extends IntegrationTest {
    @Nested
    @DisplayName("시간 목록 조회 API")
    class FindAllReservationTime {
        @Test
        void 시간_목록을_조회할_수_있다() {
            memberFixture.createAdminMember();
            timeFixture.createFutureTime();

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("times.size()", is(1));
        }
    }

    @Nested
    @DisplayName("예약 가능 시간 목록 조회 API")
    class FindAllAvailableReservationTime {
        Reservation reservation;

        @BeforeEach
        void setUp() {
            ReservationTime time = timeFixture.createFutureTime();
            Theme theme = themeFixture.createFirstTheme();
            Member member = memberFixture.createUserMember();
            reservation = reservationFixture.createFutureReservation(time, theme, member);
        }

        @Test
        void 예약이_가능한_시간을_필터링해_조회할_수_있다() {
            RestAssured.given().log().all()
                    .when().get("/times/available?date=2024-10-05&themeId=1")
                    .then().log().all()
                    .statusCode(200)
                    .body("times.size()", is(1));

            ReservationTimeAvailableListResponse response = RestAssured.get(
                            "/times/available?date=2024-10-05&themeId=1")
                    .as(ReservationTimeAvailableListResponse.class);
            Assertions.assertThat(response.getTimes().get(0).getAlreadyBooked()).isFalse();
        }

        @Test
        void 예약이_불가한_시간을_필터링해_조회할_수_있다() {
            String date = reservation.getDate().toString();

            RestAssured.given().log().all()
                    .when().get("/times/available?date=" + date + "&themeId=1")
                    .then().log().all()
                    .statusCode(200)
                    .body("times.size()", is(1));

            ReservationTimeAvailableListResponse response = RestAssured.get(
                            "/times/available?date=" + date + "&themeId=1")
                    .as(ReservationTimeAvailableListResponse.class);
            Assertions.assertThat(response.getTimes().get(0).getAlreadyBooked()).isTrue();
        }
    }

    @Nested
    @DisplayName("시간 추가 API")
    class SaveReservationTime {
        @BeforeEach
        void setUp() {
            memberFixture.createAdminMember();
        }

        @Test
        void 시간을_추가할_수_있다() {
            Map<String, String> params = new HashMap<>();
            params.put("startAt", "11:00");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/times/1")
                    .body("id", is(1));
        }

        @Test
        void 시작_시간이_빈_값이면_시간을_추가할_수_없다() {
            Map<String, String> params = new HashMap<>();
            params.put("startAt", null);

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 시작_시간의_형식이_다르면_시간을_추가할_수_없다() {
            Map<String, String> params = new HashMap<>();
            params.put("startAt", "25:00");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 중복된_시간은_추가할_수_없다() {
            ReservationTime time = timeFixture.createFutureTime();
            Map<String, String> params = new HashMap<>();
            params.put("startAt", time.getStartAt().toString());

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(409);
        }
    }

    @Nested
    @DisplayName("시간 삭제 API")
    class DeleteReservationTime {
        ReservationTime time;
        Member member;

        @BeforeEach
        void setUp() {
            time = timeFixture.createFutureTime();
            member = memberFixture.createAdminMember();
        }

        @Test
        void 시간을_삭제할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        void 존재하지_않는_시간은_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/times/13")
                    .then().log().all()
                    .statusCode(404);
        }

        @Test
        void 예약이_존재하는_시간은_삭제할_수_없다() {
            Theme theme = themeFixture.createFirstTheme();
            reservationFixture.createFutureReservation(time, theme, member);

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(400);
        }
    }
}
