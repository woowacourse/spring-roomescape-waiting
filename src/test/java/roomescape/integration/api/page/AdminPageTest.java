package roomescape.integration.api.page;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.RestAssuredTestBase;
import roomescape.domain.member.Member;
import roomescape.integration.api.RestLoginMember;

class AdminPageTest extends RestAssuredTestBase {

    private Member member;
    private RestLoginMember restLoginMember;

    @BeforeEach
    void setUp() {
        member = memberDbFixture.leehyeonsu48888_지메일_gustn111느낌표두개_어드민();
        restLoginMember = generateLoginMember(member);
    }

    @Test
    void 어드민_예약_추가_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 어드민_예약_시간_관리_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 어드민_테마_관리_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }
}
