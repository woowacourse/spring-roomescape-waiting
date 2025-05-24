package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static roomescape.common.Constant.FIXED_CLOCK;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RestAssuredTestBase;
import roomescape.domain.reservation.schdule.ReservationDate;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.integration.api.RestLoginMember;
import roomescape.integration.fixture.ReservationScheduleDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;

class ReservationRestTest extends RestAssuredTestBase {

    private static final LocalDate RESERVATION_DATE = LocalDate.now(FIXED_CLOCK).plusDays(1);
    private static final String THEME_NAME = "공포";
    private Integer timeId;
    private Integer themeId;
    private RestLoginMember restLoginMember;

    @Autowired
    private ReservationScheduleDbFixture reservationScheduleDbFixture;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @BeforeEach
    void setUp() {
        restLoginMember = generateLoginMember();
        timeId = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .body(Map.of("startAt", "10:00"))
                .when().post("/times")
                .then().statusCode(201)
                .extract().path("id");
        themeId = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .body(Map.of(
                        "name", THEME_NAME,
                        "description", "정글 탐험 컨셉",
                        "thumbnail", "https://example.com/adventure.jpg"
                ))
                .when().post("/themes")
                .then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void 예약을_생성한다() {
        Theme theme = themeDbFixture.공포();
        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
        ReservationDate date = new ReservationDate(RESERVATION_DATE);
        reservationScheduleDbFixture.createSchedule(date, time, theme);

        Map<String, Object> request = Map.of(
                "date", date.date().toString(),
                "time", time.getId(),
                "theme", theme.getId()
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
                .body("date", is(date.date().toString()))
                .body("time.startAt", is(time.getStartAt().toString()))
                .body("theme.name", is(theme.getName().name()));
    }

    @Test
    void 예약_목록을_조회한다() {
        예약을_생성한다();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].name", is("홍길동"))
                .body("[0].time.id", is(2))
                .body("[0].time.startAt", is("10:00"))
                .body("[0].theme.id", is(1))
                .body("[0].theme.name", is("어드벤처"))
                .body("[0].theme.description", is("정글 탐험 컨셉"))
                .body("[0].theme.thumbnail", is("https://example.com/adventure.jpg"));
    }

    @Test
    void 필터를_이용해서_예약_목록을_조회한다() {
        예약을_생성한다();
        RestAssured.given().log().all()
                .param("theme", themeId)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].name", is("홍길동"))
                .body("[0].time.id", is(1))
                .body("[0].time.startAt", is("10:00"))
                .body("[0].theme.id", is(1))
                .body("[0].theme.name", is("어드벤처"))
                .body("[0].theme.description", is("정글 탐험 컨셉"))
                .body("[0].theme.thumbnail", is("https://example.com/adventure.jpg"));
    }

    @Test
    void 예약을_삭제한다() {
        예약을_생성한다();
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().delete("/reservations/{id}", 1)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 내_예약을_조회한다() {
        예약을_생성한다();
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].reservationId", is(1))
                .body("[0].theme", is(THEME_NAME))
                .body("[0].date", is(RESERVATION_DATE.toString()))
                .body("[0].time", is("10:00"))
                .body("[0].status", is("예약"));
    }

    @Test
    void 예약_대기를_한다() {
        Map<String, Object> request = Map.of(
                "date", LocalDate.now(FIXED_CLOCK).plusDays(1).toString(),
                "time", timeId,
                "theme", themeId
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .body(request)
                .when().post("/reservations/wait")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("홍길동"))
                .body("date", is(RESERVATION_DATE.toString()))
                .body("time.startAt", is("10:00"))
                .body("theme.name", is("어드벤처"));
    }

    @Test
    void 예약_대기를_승인한다() {
        예약_대기를_한다();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().post("/reservations/wait/{id}/approve", 1L)
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("홍길동"))
                .body("date", is(RESERVATION_DATE.toString()))
                .body("time.startAt", is("10:00"))
                .body("theme.name", is("어드벤처"));
    }

    @Test
    void 예약_대기를_취소한다() {
        예약_대기를_한다();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().delete("/reservations/wait/{id}/cancel", 1L)
                .then().log().all()
                .statusCode(204);
    }


    // TODO: 응답 어떻게 구성할지 고민해보기
    @Test
    @DisplayName("예약, 예약 대기가 모두 있는 경우")
    void 내_예약을_조회한다2() {
        예약을_생성한다();
        예약_대기를_한다();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().delete("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].reservationId", is(1))
                .body("[0].theme", is(THEME_NAME))
                .body("[0].date", is(RESERVATION_DATE.toString()))
                .body("[0].time", is("10:00"))
                .body("[0].status", is("예약"))
                .body("[1].reservationId", is(1))
                .body("[1].theme", is(THEME_NAME))
                .body("[1].date", is(RESERVATION_DATE.toString()))
                .body("[1].time", is("10:00"))
                .body("[1].status", is("예약대기"));

    }


}
