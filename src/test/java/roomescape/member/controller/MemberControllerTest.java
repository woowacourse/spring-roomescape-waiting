package roomescape.member.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.auth.dto.request.MemberSignUpRequest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.response.MemberNameSelectResponse;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.repository.fake.FakeMemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class MemberControllerTest {
    @Autowired
    private MemberRepository memberRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MemberRepository memberRepository() {
            return new FakeMemberRepository();
        }
    }

    @Test
    void getMembersTest() {
        List<Member> savedMembers = MemberFixture.createMembers(5, MemberRole.USER).stream()
            .map(memberRepository::save).toList();

        List<MemberNameSelectResponse> expected = savedMembers.stream()
            .map(member -> new MemberNameSelectResponse(member.getId(), member.getName()))
            .toList();

        // when
        List<MemberNameSelectResponse> responses = RestAssured.given().log().all()
            .when().get("/members")
            .then().log().all()
            .statusCode(200)
            .extract().body().jsonPath().getList(".", MemberNameSelectResponse.class);

        assertThat(responses).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void signupTest() {
        MemberSignUpRequest request = new MemberSignUpRequest("test", "test@test.com", "testpassword");

        RestAssured.given().log().all()
            .contentType("application/json")
            .body(request)
            .when().post("/members")
            .then().log().all()
            .statusCode(HttpStatus.OK.value());
    }
}
