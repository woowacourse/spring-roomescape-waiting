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
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.integration.api.RestLoginMember;
import roomescape.integration.fixture.ReservationDateFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationScheduleDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;

class ReservationRestTest extends RestAssuredTestBase {

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    private RestLoginMember restLoginMember;

    private ReservationSchedule schedule;

    @BeforeEach
    void setUp(
            @Autowired ReservationScheduleDbFixture reservationScheduleDbFixture,
            @Autowired ReservationTimeDbFixture reservationTimeDbFixture,
            @Autowired ThemeDbFixture themeDbFixture
    ) {
        restLoginMember = generateLoginMember();
        Theme theme = themeDbFixture.공포();
        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
        schedule = reservationScheduleDbFixture.createSchedule(ReservationDateFixture.예약날짜_오늘, time, theme);
    }

    @Test
    void 예약을_생성한다() {
        Map<String, Object> request = Map.of(
                "date", schedule.getDate().toString(),
                "timeId", schedule.getReservationTime().getId(),
                "themeId", schedule.getTheme().getId()
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("홍길동"))
                .body("date", is(schedule.getDate().toString()))
                .body("time.startAt", is(schedule.getReservationTime().getStartAt().toString()))
                .body("theme.name", is(schedule.getTheme().getName().name()));
    }

    @Test
    void 예약_목록을_조회한다() {
        reservationDbFixture.예약_생성(schedule, restLoginMember.member());
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("[0].name", is(restLoginMember.member().getName().name()))
                .body("[0].time.id", is(schedule.getReservationTime().getId().intValue()))
                .body("[0].time.startAt", is(schedule.getReservationTime().getStartAt().toString()))
                .body("[0].theme.id", is(schedule.getTheme().getId().intValue()))
                .body("[0].theme.name", is(schedule.getTheme().getName().name()))
                .body("[0].theme.description", is(schedule.getTheme().getDescription().description()))
                .body("[0].theme.thumbnail", is(schedule.getTheme().getThumbnail().thumbnail()));
    }

    @Test
    void 필터를_이용해서_예약_목록을_조회한다() {
        reservationDbFixture.예약_생성(schedule, restLoginMember.member());
        RestAssured.given().log().all()
                .param("theme", schedule.getTheme().getId())
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("[0].name", is(restLoginMember.member().getName().name()))
                .body("[0].time.id", is(schedule.getReservationTime().getId().intValue()))
                .body("[0].time.startAt", is(schedule.getReservationTime().getStartAt().toString()))
                .body("[0].theme.id", is(schedule.getTheme().getId().intValue()))
                .body("[0].theme.name", is(schedule.getTheme().getName().name()))
                .body("[0].theme.description", is(schedule.getTheme().getDescription().description()))
                .body("[0].theme.thumbnail", is(schedule.getTheme().getThumbnail().thumbnail()));
    }

    @Test
    void 예약을_삭제한다() {
        Reservation reservation = reservationDbFixture.예약_생성(schedule, restLoginMember.member());
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().delete("/reservations/{id}", reservation.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 내_예약을_조회한다() {
        Reservation reservation = reservationDbFixture.예약_생성(schedule, restLoginMember.member());
        ReservationSchedule schedule = reservation.getSchedule();
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].reservationId", is(reservation.getId().intValue()))
                .body("[0].theme", is(schedule.getTheme().getName().name()))
                .body("[0].date", is(schedule.getDate().toString()))
                .body("[0].time", is(schedule.getReservationTime().getStartAt().toString()));
    }
}
