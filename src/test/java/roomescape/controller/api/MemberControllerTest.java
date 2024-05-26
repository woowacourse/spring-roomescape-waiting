package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class MemberControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("성공: 모든 회원 조회 -> 200")
    @Test
    void findAll() {
        memberRepository.save(new Member("관리자", "admin@a.com", "123a!", Role.ADMIN));
        memberRepository.save(new Member("사용자", "user@a.com", "123a!", Role.USER));

        RestAssured.given().log().all()
            .when().get("/members")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2))
            .body("name", contains("관리자", "사용자"));
    }
}
