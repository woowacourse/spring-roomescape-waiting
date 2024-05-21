package roomescape.member.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberCreateRequest;
import roomescape.member.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MemberControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("성공 : 유저인 회원 정보를 얻을 수 있다.")
    void findMembers() {
        // Given
        Member member = new Member(1L, "member", "aa@naver.com", "1111", MemberRole.USER);
        Member admin = new Member(2L, "admin", "admin@naver.com", "1234", MemberRole.ADMIN);
        memberRepository.save(member);
        memberRepository.save(admin);

        // When
        int actualSize = RestAssured
                .when().get("/members")
                .then()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        // Then
        List<Member> expected = memberRepository.findAllByRole(MemberRole.USER);
        assertThat(actualSize).isEqualTo(expected.size());
    }

    @Test
    @DisplayName("성공 : 회원 정보를 만들 수 있다.")
    void createMember() {
        MemberCreateRequest params = new MemberCreateRequest("호돌", "bbb@naver.com", "2222");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/members")
                .then()
                .statusCode(201)
                .header("Location", "/members/1");

        List<Member> actual = memberRepository.findAll();

        assertThat(actual).hasSize(1);
    }
}
