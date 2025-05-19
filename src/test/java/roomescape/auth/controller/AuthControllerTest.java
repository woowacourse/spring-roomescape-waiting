package roomescape.auth.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.repository.fake.FakeMemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthorizationProvider authorizationProvider;

    @Test
    void login() {
        // given
        Member member = MemberFixture.createMember(MemberRole.USER);

        Member savedMember = memberRepository.save(member);

        LoginRequest request = new LoginRequest(savedMember.getEmail(), savedMember.getPassword());

        // when
        RestAssured.given().log().all()
            .contentType("application/json")
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(200)
            .cookie("token");
    }

    @Test
    void loginCheck() {
        // given
        Member member = MemberFixture.createMember(MemberRole.USER);

        Member savedMember = memberRepository.save(member);

        AuthorizationPrincipal principal = getAuthorizationPrincipal(savedMember);

        // when
        RestAssured.given().log().all()
            .cookie("token", principal.value())
            .when().get("/login/check")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    void logout() {
        // given
        Member member = MemberFixture.createMember(MemberRole.USER);

        Member savedMember = memberRepository.save(member);

        AuthorizationPrincipal principal = getAuthorizationPrincipal(savedMember);

        // when
        RestAssured.given().log().all()
            .contentType("application/json")
            .cookie("token", principal.value())
            .when().post("/logout")
            .then().log().all()
            .statusCode(200)
            .cookie("token", "");
    }

    private AuthorizationPrincipal getAuthorizationPrincipal(Member member) {
        return authorizationProvider.createPrincipal(
            new AuthorizationPayload(
                member.getName(),
                member.getRole()
            )
        );
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MemberService memberService() {
            return new MemberService(memberRepository());
        }

        @Bean
        public MemberRepository memberRepository() {
            return new FakeMemberRepository();
        }
    }
}
