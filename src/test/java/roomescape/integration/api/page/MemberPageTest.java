package roomescape.integration.api.page;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.RestAssuredTestBase;
import roomescape.domain.member.Member;
import roomescape.integration.api.RestLoginMember;

class MemberPageTest extends RestAssuredTestBase {

    private Member member;
    private RestLoginMember restLoginMember;

    @BeforeEach
    void setUp() {
        member = memberDbFixture.leehyeonsu48888_지메일_gustn111느낌표두개_어드민();
        restLoginMember = generateLoginMember(member);
    }


    @Test
    void 유저_예약하기_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 내_예약_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/reservation-mine")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 기본페이지인_인기_테마_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }
}
