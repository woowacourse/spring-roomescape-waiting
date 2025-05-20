package roomescape.auth.controller;

import static org.mockito.Mockito.when;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;
import roomescape.auth.service.AuthServiceFacade;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private AuthorizationProvider authorizationProvider;

    @MockitoBean
    private AuthServiceFacade authService;

    @LocalServerPort
    private int port;

    @Test
    void 로그인을_할_수_있다() {
        // given
        Member member = MemberFixture.createMember(MemberRole.USER);

        LoginRequest request = new LoginRequest(member.getEmail(), member.getPassword());

        when(authService.login(request))
            .thenReturn(new AuthorizationPrincipal("AuthorizationPrincipal"));

        // when
        RestAssured.given().log().all()
            .contentType("application/json")
            .port(port)
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(200)
            .cookie("token");
    }

    @Test
    void 로그인_정보를_통해_이름을_알_수_있다() {
        // given
        Member member = MemberFixture.createMember(MemberRole.USER);

        AuthorizationPrincipal principal = getAuthorizationPrincipal(member);

        // when
        RestAssured.given().log().all()
            .port(port)
            .cookie("token", principal.value())
            .when().get("/login/check")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    void 로그아웃을_할_수_있다() {
        // given
        Member member = MemberFixture.createMember(MemberRole.USER);

        AuthorizationPrincipal principal = getAuthorizationPrincipal(member);

        // when
        RestAssured.given().log().all()
            .port(port)
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
}
