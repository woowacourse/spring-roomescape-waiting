package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RestAssuredTestBase;
import roomescape.integration.api.RestLoginMember;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.integration.fixture.WaitingDbFixture;

class WaitingRestTest extends RestAssuredTestBase {

    private RestLoginMember restLoginMember;
    private Long timeId;
    private Long themeId;
    private Long waitingId;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private WaitingDbFixture waitingDbFixture;

    @BeforeEach
    void setUp() {
        restLoginMember = generateLoginMember();
        var member = restLoginMember.member();
        var time = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        timeId = time.getId();
        themeId = theme.getId();
        var waiting = waitingDbFixture.대기_25_4_22(time, theme, member);
        waitingId = waiting.getId();
    }

    @Test
    void 대기를_생성한다() {
        var request = Map.of(
                "date", roomescape.integration.fixture.ReservationDateFixture.예약날짜_오늘.date().toString(),
                "timeId", timeId,
                "themeId", themeId
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .body(request)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("name", is("홍길동"))
                .body("date", is("2025-04-20"))
                .body("time.id", is(timeId.intValue()))
                .body("theme.id", is(themeId.intValue()));
    }

    @Test
    void 대기를_삭제한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().delete("/waitings/{id}", waitingId)
                .then().log().all()
                .statusCode(204);
    }
} 
