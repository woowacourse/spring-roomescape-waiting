package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.common.RestAssuredTestBase;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.integration.api.RestLoginMember;
import roomescape.integration.fixture.MemberDbFixture;

class LoginRestTest extends RestAssuredTestBase {

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private DataSource dataSource;

    @Test
    void 로그인을_한다() {
        // given
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        Member member = memberRepository.save(new Member(
                null,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword(encoder.encode("gustn111!!")),
                MemberRole.MEMBER
        ));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "leehyeonsu4888@gmail.com",
                        "password", "gustn111!!"
                ))
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 현재_로그인된_멤버가_누구인지_조회한다() {
        // given
        RestLoginMember restLoginMember = generateLoginMember();

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .body("name", is(restLoginMember.member().getName().name()));
    }
}
