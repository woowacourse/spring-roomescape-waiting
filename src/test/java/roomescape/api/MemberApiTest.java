package roomescape.api;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.infrastructure.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MemberApiTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 전체_회원_조회() {
        // given
        Member member = new Member(null, "name", "email1@domain.com", "password1", Role.MEMBER);
        memberRepository.save(member);
        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("api/members")
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 회원가입_성공() {
        // given
        Map<String, Object> memberRequest = new HashMap<>();
        memberRequest.put("date", "2026-08-05");
        memberRequest.put("name", "name1");
        memberRequest.put("email", "email@domain.com");
        memberRequest.put("password", "password1");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberRequest)
                .when().post("/api/members")
                .then().log().all()
                .statusCode(200)
                .body("name", equalTo("name1"));
    }
}
