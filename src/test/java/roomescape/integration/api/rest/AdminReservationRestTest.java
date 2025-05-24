package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.is;
import static roomescape.common.Constant.FIXED_CLOCK;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RestAssuredTestBase;
import roomescape.integration.api.RestLoginMember;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;

class AdminReservationRestTest extends RestAssuredTestBase {

    public static final LocalDate RESERVATION_DATE = LocalDate.now(FIXED_CLOCK).plusDays(1);
    private RestLoginMember restLoginAdmin;
    private Long timeId;
    private Long themeId;
    private Long memberId;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;

    @BeforeEach
    void setUp() {
        restLoginAdmin = generateLoginAdmin();
        var restLoginMember = generateLoginMember();
        var time = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var member = restLoginMember.member();
        timeId = time.getId();
        themeId = theme.getId();
        memberId = member.getId();
    }

    @Test
    void 어드민이_예약을_생성한다() {
        var request = Map.of(
                "date", RESERVATION_DATE.toString(),
                "timeId", timeId,
                "themeId", themeId,
                "memberId", memberId
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginAdmin.sessionId())
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("홍길동"))
                .body("date", is(RESERVATION_DATE.toString()))
                .body("time.startAt", is("10:00"))
                .body("theme.name", is("공포"));
    }
} 
