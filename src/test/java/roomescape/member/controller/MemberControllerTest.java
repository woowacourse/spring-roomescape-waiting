package roomescape.member.controller;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.support.fixture.AuthFixture;
import roomescape.support.model.TokenCookieDto;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MemberControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthFixture authFixture;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("/members 으로 GET 요청을 보내면 회원 정보와 200 OK 를 받는다.")
    void getAdminPage() {
        // given
        TokenCookieDto adminTokenCookieDto = authFixture.saveAdminAndGetTokenCookies("admin@admin.com", "12341234", port);

        memberRepository.save(new Member("이름1", "test@test.com", "password", Role.MEMBER));
        memberRepository.save(new Member("이름2", "test@test.com", "password", Role.MEMBER));
        memberRepository.save(new Member("이름3", "test@test.com", "password", Role.MEMBER));
        memberRepository.save(new Member("이름4", "test@test.com", "password", Role.MEMBER));

        // when & then
        RestAssured.given().log().all()
                .port(port)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(200)
                .body("data.members.size()", is(5));
    }
}
