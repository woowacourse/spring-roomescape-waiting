package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import roomescape.common.RestAssuredTestBase;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;

class LoginRestTest extends RestAssuredTestBase {

    @Test
    void 로그인을_한다() {
        // given
        var encoder = new BCryptPasswordEncoder();
        var member = memberRepository.save(new Member(
                null,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword(encoder.encode("gustn111!!")),
                MemberRole.MEMBER
        ));

        var request = Map.of(
                "email", "leehyeonsu4888@gmail.com",
                "password", "gustn111!!"
        );

        // when // then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 현재_로그인된_멤버가_누구인지_조회한다() {
        // given
        var restLoginMember = generateLoginMember();

        // when // then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .body("name", is(restLoginMember.member().getName().name()));
    }
}
