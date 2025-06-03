package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.is;
import static roomescape.common.Constant.FIXED_CLOCK;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RestAssuredTestBase;
import roomescape.integration.api.RestLoginMember;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.integration.fixture.WaitingDbFixture;

class AdminWaitingRestTest extends RestAssuredTestBase {

    private RestLoginMember restLoginAdmin;
    private Long waitingId;

    @Autowired
    private WaitingDbFixture waitingDbFixture;
    @Autowired
    private MemberDbFixture memberDbFixture;
    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;

    @BeforeEach
    void setUp() {
        restLoginAdmin = generateLoginAdmin();
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var time = reservationTimeDbFixture.예약시간_10시();
        var theme = themeDbFixture.공포();
        var waiting = waitingDbFixture.대기_생성(
                roomescape.integration.fixture.ReservationDateFixture.예약날짜_오늘,
                time,
                theme,
                member,
                LocalDateTime.now(FIXED_CLOCK)
        );
        waitingId = waiting.getId();
    }

    @Test
    void 어드민이_대기_목록을_조회한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginAdmin.sessionId())
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(waitingId.intValue()))
                .body("[0].name", is("한스"))
                .body("[0].date", is("2025-04-20"))
                .body("[0].time.startAt", is("10:00"))
                .body("[0].theme.name", is("공포"));
    }

    @Test
    void 어드민이_대기를_승인한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginAdmin.sessionId())
                .when().post("/admin/waitings/{id}/approve", waitingId)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 어드민이_대기를_삭제한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginAdmin.sessionId())
                .when().delete("/admin/waitings/{id}", waitingId)
                .then().log().all()
                .statusCode(200);
    }
} 
