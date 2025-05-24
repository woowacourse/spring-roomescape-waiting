package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RestAssuredTestBase;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWait;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.integration.api.RestLoginMember;
import roomescape.integration.fixture.ReservationDateFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationScheduleDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ReservationWaitDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.repository.ReservationRepository;

class ReservationWaitRestTest extends RestAssuredTestBase {

    @Autowired
    private ReservationWaitDbFixture reservationWaitDbFixture;

    @Autowired
    private ReservationRepository reservationRepository;

    private RestLoginMember restLoginMember;
    private ReservationSchedule schedule;
    private Reservation reservation;

    @BeforeEach
    void setUp(
            @Autowired ReservationScheduleDbFixture reservationScheduleDbFixture,
            @Autowired ReservationTimeDbFixture reservationTimeDbFixture,
            @Autowired ThemeDbFixture themeDbFixture,
            @Autowired ReservationDbFixture reservationDbFixture
    ) {
        restLoginMember = generateLoginMember();
        Theme theme = themeDbFixture.공포();
        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
        schedule = reservationScheduleDbFixture.createSchedule(ReservationDateFixture.예약날짜_오늘, time, theme);
        reservation = reservationDbFixture.예약_생성(schedule, restLoginMember.member());
    }

    @Test
    void 예약_대기를_한다() {
        Map<String, Object> request = Map.of(
                "date", schedule.getDate(),
                "time", schedule.getReservationTime().getId(),
                "theme", schedule.getTheme().getId()
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .body(request)
                .when().post("/reservations/waits")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is(restLoginMember.member().getName().name()))
                .body("date", is(schedule.getDate().toString()))
                .body("time.startAt", is(schedule.getReservationTime().getStartAt().toString()))
                .body("theme.name", is(schedule.getTheme().getName().name()));
    }

    @Test
    void 예약_대기를_승인한다() {
        ReservationWait reservationWait = reservationWaitDbFixture.createReservationWait(
                this.schedule,
                restLoginMember.member()
        );
        ReservationSchedule schedule = reservationWait.getSchedule();
        reservationRepository.deleteById(reservation.getId());
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().post("/reservations/waits/{id}", reservationWait.getId())
                .then().log().all()
                .statusCode(201)
                .body("name", is(restLoginMember.member().getName().name()))
                .body("date", is(schedule.getDate().toString()))
                .body("time.startAt", is(schedule.getReservationTime().getStartAt().toString()))
                .body("theme.name", is(schedule.getTheme().getName().name()));
    }

    @Test
    void 예약_대기를_취소한다() {
        ReservationWait reservationWait = reservationWaitDbFixture.createReservationWait(
                schedule,
                restLoginMember.member()
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().delete("/reservations/waits/{id}", reservationWait.getId())
                .then().log().all()
                .statusCode(204);
    }


    @Test
    void 내_예약_대기를_조회한다() {
        ReservationWait reservationWait = reservationWaitDbFixture.createReservationWait(
                schedule,
                restLoginMember.member()
        );
        ReservationSchedule schedule = reservationWait.getSchedule();
        Theme theme = schedule.getTheme();
        ReservationTime reservationTime = schedule.getReservationTime();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/reservations/waits/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(reservationWait.getId().intValue()))
                .body("[0].theme", is(theme.getName().name()))
                .body("[0].date", is(schedule.getDate().toString()))
                .body("[0].time", is(reservationTime.getStartAt().toString()));

    }
}
